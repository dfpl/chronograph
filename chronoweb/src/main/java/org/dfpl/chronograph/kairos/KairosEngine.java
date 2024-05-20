package org.dfpl.chronograph.kairos;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import org.bson.Document;
import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.db.ExpandableGammaTable;
import org.dfpl.chronograph.kairos.program.path_reachability.OutIsAfterPathReachability;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import io.vertx.core.eventbus.EventBus;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoEdgeEvent;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoVertex;

@SuppressWarnings("unused")
public class KairosEngine {

	private Graph graph;
	private EventBus mainEventBus;
	private HashMap<Long, HashSet<AbstractKairosProgram<?>>> kairosPrograms;
	private MongoClient gammaClient;

	public MongoClient getGammaClient() {
		return gammaClient;
	}

	public KairosEngine(Graph graph, EventBus mainEventBus, String connectionString, String gammaDBName) {
		this.graph = graph;
		this.mainEventBus = mainEventBus;
		this.kairosPrograms = new HashMap<Long, HashSet<AbstractKairosProgram<?>>>();
		this.gammaClient = MongoClients.create(connectionString);

		this.mainEventBus.consumer("addVertex", v -> {
			Server.logger.debug("kairos addVertex: " + v.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String vString = v.body().toString();
					Vertex addedVertex = graph.getVertex(vString);
					program.onAddVertex(addedVertex);
				});
			});
		});

		this.mainEventBus.consumer("addEdge", e -> {
			Server.logger.debug("kairos addEdge: " + e.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String eString = e.body().toString();
					Edge addedEdge = graph.getEdge(eString);
					program.onAddEdge(addedEdge);
				});
			});
		});

		this.mainEventBus.consumer("removeVertex", v -> {
			Server.logger.debug("kairos removeVertex: " + v.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String vString = v.body().toString();
					Vertex removedVertex = graph.getVertex(vString);
					program.onRemoveVertex(removedVertex);
				});
			});
		});

		this.mainEventBus.consumer("removeEdge", e -> {
			Server.logger.debug("kairos removeEdge: " + e.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String eString = e.body().toString();
					Edge removedEdge = graph.getEdge(eString);
					program.onRemoveEdge(removedEdge);
				});
			});
		});

		this.mainEventBus.consumer("addVertexEvent", ve -> {
			Server.logger.debug("kairos addVertexEvent: " + ve.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String veString = ve.body().toString();
					String[] arr = veString.split("_");
					Vertex v = graph.getVertex(arr[0]);
					VertexEvent addedVertexEvent = v.getEvent(Long.parseLong(arr[1]));
					program.onAddVertexEvent(addedVertexEvent);
				});
			});
		});

		this.mainEventBus.consumer("addEdgeEvent", ee -> {
			Server.logger.debug("kairos addEdgeEvent: " + ee.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String eeString = ee.body().toString();
					String[] arr = eeString.split("_");
					Edge e = graph.getEdge(arr[0]);
					EdgeEvent addedEdgeEvent = e.getEvent(Long.parseLong(arr[1]));
					program.onAddEdgeEvent(addedEdgeEvent);
				});
			});
		});

		this.mainEventBus.consumer("removeVertexEvent", ve -> {
			Server.logger.debug("kairos removeVertexEvent: " + ve.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String veString = ve.body().toString();
					String[] arr = veString.split("_");
					Vertex v = graph.getVertex(arr[0]);
					VertexEvent removedVertexEvent = v.getEvent(Long.parseLong(arr[1]));
					program.onRemoveVertexEvent(removedVertexEvent);
				});
			});
		});

		this.mainEventBus.consumer("removeEdgeEvent", ee -> {
			Server.logger.debug("kairos removeEdgeEvent: " + ee.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String eeString = ee.body().toString();
					String[] arr = eeString.split("_");
					Edge e = graph.getEdge(arr[0]);
					EdgeEvent removedEdgeEvent = null;
					if (graph instanceof MChronoGraph)
						removedEdgeEvent = new MChronoEdgeEvent(e, Long.parseLong(arr[1]));
					else if (graph instanceof PChronoGraph) {
						// TODO
					}
					program.onRemoveEdgeEvent(removedEdgeEvent);
				});
			});
		});

		this.mainEventBus.consumer("clear", e -> {
			Server.logger.debug("kairos cleared");
		});

		loadExistingSubscriptions();

	}

	public void loadExistingSubscriptions() {
		// TODO
		MongoCursor<Document> databaseCursor = this.getGammaClient().listDatabases().iterator();
		while (databaseCursor.hasNext()) {
			Document database = databaseCursor.next();
			String databaseName = database.getString("name");
			String[] arr = databaseName.split("\\_");
			if (arr.length != 3)
				continue;
			Long startTime = null;
			try {
				startTime = Long.parseLong(arr[0]);
			} catch (Exception e) {
				continue;
			}
			String edgeLabel = arr[2];
			if (arr[1].equals("OutIsAfterPathReachability")) {
				ExpandableGammaTable gammaTable = new ExpandableGammaTable(this.getGammaClient(), databaseName);
				MongoCursor<String> sourceIDCursor = this.getGammaClient().getDatabase(databaseName)
						.listCollectionNames().iterator();
				while (sourceIDCursor.hasNext()) {
					String sourceID = sourceIDCursor.next();
					Vertex v = null;
					if (Server.backendType.equals("memory"))
						v = new MChronoVertex(graph, sourceID);
					else {
						v = new PChronoVertex(graph, sourceID, ((PChronoGraph) graph).getVertexCollection());
					}
					this.addExistingSubscription(v, startTime, edgeLabel,
							new OutIsAfterPathReachability(graph, gammaTable, edgeLabel));
				}
			}
		}
	}

	public Set<Long> getTimes() {
		return kairosPrograms.keySet();
	}

	public Set<VertexEvent> getSources() {
		HashSet<VertexEvent> set = new HashSet<VertexEvent>();

		for (Entry<Long, HashSet<AbstractKairosProgram<?>>> entry : kairosPrograms.entrySet()) {
			Long t = entry.getKey();
			for (AbstractKairosProgram<?> program : entry.getValue()) {
				for (String s : program.gammaTable.getSources()) {
					set.add(graph.getVertex(s).getEvent(t));
				}
			}
		}
		return set;
	}

	public AbstractKairosProgram<?> getProgram(Long startTime, String name, String edgeLabel) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null)
			return null;
		for (AbstractKairosProgram<?> program : programs) {
			if (program.getName().equals(name) & program.getEdgeLabel().equals(edgeLabel)) {
				return program;
			}
		}
		return null;
	}

	public Set<AbstractKairosProgram<?>> getPrograms(Long startTime) {
		return kairosPrograms.get(startTime);
	}

	public void addSubscription(Vertex source, Long startTime, String edgeLabel, AbstractKairosProgram<?> program) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null) {
			programs = new HashSet<>();
			programs.add(program);
			kairosPrograms.put(startTime, programs);
		} else {
			programs.add(program);
		}
		program.onInitialization(Set.of(source), startTime, edgeLabel);
	}

	public void addExistingSubscription(Vertex source, Long startTime, String edgeLabel,
			AbstractKairosProgram<?> program) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null) {
			programs = new HashSet<>();
			programs.add(program);
			kairosPrograms.put(startTime, programs);
		} else {
			programs.add(program);
		}
	}

}
