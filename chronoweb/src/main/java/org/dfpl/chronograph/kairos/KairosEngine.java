package org.dfpl.chronograph.kairos;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import io.vertx.core.eventbus.EventBus;

@SuppressWarnings("unused")
public class KairosEngine {

	private Graph graph;
	private EventBus mainEventBus;
	private HashMap<Long, HashSet<AbstractKairosProgram<?>>> kairosPrograms;

	public KairosEngine(Graph graph, EventBus mainEventBus) {
		this.graph = graph;
		this.mainEventBus = mainEventBus;
		this.kairosPrograms = new HashMap<Long, HashSet<AbstractKairosProgram<?>>>();

		this.mainEventBus.consumer("addVertex", v -> {
			Server.logger.debug("kairos addVertex: " + v.body());
		});

		this.mainEventBus.consumer("addEdge", e -> {
			Server.logger.debug("kairos addEdge: " + e.body());
		});

		this.mainEventBus.consumer("removeVertex", v -> {
			Server.logger.debug("kairos removeVertex: " + v.body());
		});

		this.mainEventBus.consumer("removeEdge", e -> {
			Server.logger.debug("kairos removeEdge: " + e.body());
		});

		this.mainEventBus.consumer("addVertexEvent", ve -> {
			Server.logger.debug("kairos addVertexEvent: " + ve.body());
		});

		this.mainEventBus.consumer("addEdgeEvent", ee -> {
			Server.logger.debug("kairos addEdgeEvent: " + ee.body());
			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					String eeString = ee.body().toString();
					String[] arr = eeString.split("_");
					Edge e = graph.getEdge(arr[0]);
					EdgeEvent edgeEvent = e.getEvent(Long.parseLong(arr[1]));
					program.onAddEdgeEvent(edgeEvent);
				});
			});
		});

		this.mainEventBus.consumer("removeVertexEvent", ve -> {
			Server.logger.debug("kairos removeVertexEvent: " + ve.body());
		});

		this.mainEventBus.consumer("removeEdgeEvent", ee -> {
			Server.logger.debug("kairos removeEdgeEvent: " + ee.body());
		});

		this.mainEventBus.consumer("clear", e -> {
			Server.logger.debug("kairos cleared");
		});

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

	public AbstractKairosProgram<?> getProgram(Long startTime, String name) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null)
			return null;
		for (AbstractKairosProgram<?> program : programs) {
			if (program.getName().equals(name)) {
				return program;
			}
		}
		return null;
	}

	public Set<AbstractKairosProgram<?>> getPrograms(Long startTime) {
		return kairosPrograms.get(startTime);
	}

	public void addSubscription(Vertex source, Long startTime, AbstractKairosProgram<?> program) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null) {
			programs = new HashSet<AbstractKairosProgram<?>>();
			programs.add(program);
			kairosPrograms.put(startTime, programs);
		} else {
			programs.add(program);
		}
		program.onInitialization(Set.of(source), startTime);
	}

}
