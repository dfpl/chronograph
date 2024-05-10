package org.dfpl.chronograph.khronos.manipulation.persistent;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

import com.mongodb.client.MongoCollection;
import com.tinkerpop.blueprints.*;

import io.vertx.core.eventbus.EventBus;

/**
 * The persistent implementation of temporal graph database with MongoDB.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
public class PChronoVertex extends PChronoElement implements Vertex {

	public PChronoVertex(Graph g, String id, MongoCollection<Document> collection) {
		this.id = id;
		this.g = g;
		this.collection = collection;
	}

	@Override
	public Iterable<Edge> getEdges(Direction direction, List<String> labels) {

		Document query = new Document();

		if (direction.equals(Direction.OUT))
			query.append("_o", id);
		else if (direction.equals(Direction.IN))
			query.append("_i", id);

		if (labels != null && labels.isEmpty()) {

		} else if (labels != null && labels.size() == 1) {
			query.append("_l", labels.get(0));
		} else {
			query.append("_l", new Document("$in", labels));
		}

		return ((PChronoGraph) g).edges.find(query).map(doc -> {
			return new PChronoEdge((PChronoGraph) g, doc.getString("_id"), collection);
		});
	}

	@Override
	public Iterable<Vertex> getVertices(Direction direction, List<String> labels) {
		Document query = new Document();

		if (direction.equals(Direction.OUT))
			query.append("_o", id);
		else if (direction.equals(Direction.IN))
			query.append("_i", id);

		if (labels != null && labels.isEmpty()) {

		} else if (labels != null && labels.size() == 1) {
			query.append("_l", labels.get(0));
		} else {
			query.append("_l", new Document("$in", labels));
		}

		return ((PChronoGraph) g).edges.find(query).map(doc -> {
			if (direction.equals(Direction.OUT))
				return new PChronoVertex((PChronoGraph) g, doc.getString("_i"), collection);
			else if (direction.equals(Direction.IN))
				return new PChronoVertex((PChronoGraph) g, doc.getString("_o"), collection);
			return null;
		});
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex) {
		return g.addEdge(this, inVertex, label);
	}

	@Override
	public void remove() {
		g.removeVertex(this);
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		if (includeProperties) {
			object.put("properties", getProperties());
		}
		return object;
	}

	@Override
	public VertexEvent addEvent(long time) {
		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event != null) {
			return event;
		} else {
			event = new PChronoVertexEvent(g, id, time, ((PChronoGraph) g).vertexEvents);
			PChronoGraph pg = (PChronoGraph) g;
			pg.vertexEvents.insertOne(event.toDocument(false));
			EventBus eb = pg.getEventBus();
			if (eb != null)
				eb.send("addVertexEvent", event.getId());
			return event;
		}
	}

	@Override
	public VertexEvent getEvent(long time) {
		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event == null)
			return new PChronoVertexEvent(g, id, time, ((PChronoGraph) g).vertexEvents);
		else
			return event;
	}

	@Override
	public Iterable<VertexEvent> getEvents(boolean awareOutEvents, boolean awareInEvents) {

		PChronoGraph pg = (PChronoGraph) g;
		return pg.vertexEvents.find(new Document("_v", id)).sort(new Document("_t", 1)).map(doc -> {

			return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
		});
		// TODO: awareOutEvents, awareInEvents
	}

	@Override
	public Iterable<VertexEvent> getEvents(long time, TemporalRelation tr, boolean awareOutEvents,
			boolean awareInEvents) {

		Document query = new Document("_v", id);
		if (tr.equals(TemporalRelation.isAfter)) {
			query.append("_t", new Document("$gt", time));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			query.append("_t", new Document("$lt", time));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			query.append("_t", time);
		} else {
			throw new IllegalArgumentException();
		}

		PChronoGraph pg = (PChronoGraph) g;
		if (awareOutEvents == false && awareInEvents == false)
			return pg.vertexEvents.find(query).projection(new Document("_t", 1).append("_id", -1))
					.sort(new Document("_t", 1)).map(doc -> {
						return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
					});

		TreeSet<VertexEvent> set = new TreeSet<VertexEvent>(new Comparator<VertexEvent>() {
			@Override
			public int compare(VertexEvent o1, VertexEvent o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});
		pg.vertexEvents.find(query).projection(new Document("_t", 1).append("_id", -1)).map(doc -> {
			return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
		}).into(set);
		if (awareOutEvents) {
			query = new Document("_o", id);
			if (tr.equals(TemporalRelation.isAfter)) {
				query.append("_t", new Document("$gt", time));
			} else if (tr.equals(TemporalRelation.isBefore)) {
				query.append("_t", new Document("$lt", time));
			} else if (tr.equals(TemporalRelation.cotemporal)) {
				query.append("_t", time);
			} else {
				throw new IllegalArgumentException();
			}
			pg.edgeEvents.find(query).projection(new Document("_t", 1).append("_id", -1)).map(doc -> {
				return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
			}).into(set);
		}
		if (awareInEvents) {
			query = new Document("_i", id);
			if (tr.equals(TemporalRelation.isAfter)) {
				query.append("_t", new Document("$gt", time));
			} else if (tr.equals(TemporalRelation.isBefore)) {
				query.append("_t", new Document("$lt", time));
			} else if (tr.equals(TemporalRelation.cotemporal)) {
				query.append("_t", time);
			} else {
				throw new IllegalArgumentException();
			}
			pg.edgeEvents.find(query).projection(new Document("_t", 1).append("_id", -1)).map(doc -> {
				return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
			}).into(set);
		}
		return set;
	}

	@Override
	public void removeEvents(long time, TemporalRelation tr) {
		PChronoGraph pg = (PChronoGraph) g;
		if (tr.equals(TemporalRelation.isAfter)) {
			pg.vertexEvents.deleteMany(new Document("_t", new Document("$gt", time)));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			pg.vertexEvents.deleteMany(new Document("_t", new Document("$lt", time)));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			pg.vertexEvents.deleteMany(new Document("_t", time));
		} else {
			throw new IllegalArgumentException();
		}
	}

	public Iterable<VertexEvent> getEvents(long time, TemporalRelation... temporalRelations) {

		Document query = new Document("_v", id);
		Document timeQuery = new Document();
		for (TemporalRelation tr : temporalRelations) {
			if (tr.equals(TemporalRelation.isAfter)) {
				timeQuery.append("$gt", time);
			} else if (tr.equals(TemporalRelation.isBefore)) {
				timeQuery.append("$lt", time);
			} else if (tr.equals(TemporalRelation.cotemporal)) {
				timeQuery.append("$eq", time);
			} else {
				throw new IllegalArgumentException();
			}
		}
		if (!timeQuery.isEmpty()) {
			query.append("_t", timeQuery);
		}

		PChronoGraph pg = (PChronoGraph) g;
		return pg.vertexEvents.find(query).sort(new Document("_t", 1)).map(doc -> {
			return new PChronoVertexEvent(g, id, doc.getLong("_t"), ((PChronoGraph) g).vertexEvents);
		});
	}

	@Override
	public VertexEvent getEvent(long time, TemporalRelation tr) {
		PChronoGraph pg = (PChronoGraph) g;
		Document result = null;
		if (tr.equals(TemporalRelation.isAfter)) {
			result = pg.vertexEvents.find(new Document("_v", id).append("$gt", new Document("_t", time))).first();

		} else if (tr.equals(TemporalRelation.isBefore)) {
			result = pg.vertexEvents.find(new Document("_v", id).append("$lt", new Document("_t", time))).first();

		} else if (tr.equals(TemporalRelation.cotemporal)) {
			result = pg.vertexEvents.find(new Document("_v", id).append("_t", time)).first();

		} else {
			throw new IllegalArgumentException("Illegal temporal relation");
		}
		if (result == null)
			return null;
		return new PChronoVertexEvent(g, id, result.getLong("_t"), ((PChronoGraph) g).vertexEvents);
	}
}
