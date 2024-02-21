package org.dfpl.chronograph.khronos.manipulation.persistent;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.Event;
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

	PChronoVertex(PChronoGraph g, String id, MongoCollection<Document> collection) {
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
			event = new PChronoVertexEvent(this, time);
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
//		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
//		if (event == null)
//			return new MChronoVertexEvent(this, time);
//		else
//			return event;
		// TODO
		return null;
	}

	@Override
	public NavigableSet<VertexEvent> getEvents(boolean awareOutEvents, boolean awareInEvents) {
//		NavigableSet<VertexEvent> resultSet = new TreeSet<>();
//
//		resultSet.addAll(events);
//
//		List<Direction> directions = new LinkedList<>();
//		if (awareOutEvents)
//			directions.add(Direction.OUT);
//		if (awareInEvents)
//			directions.add(Direction.IN);
//
//		for (Direction direction : directions) {
//			for (Edge e : this.getEdges(direction, null)) {
//				e.getEvents().stream().map(Event::getTime).forEach(t -> {
//					resultSet.add(new MChronoVertexEvent(this, t));
//				});
//			}
//		}
//		return resultSet;
		// TODO
		return null;
	}

	@Override
	public NavigableSet<VertexEvent> getEvents(long time, TemporalRelation tr, boolean awareOutEvents,
			boolean awareInEvents) {
//		NavigableSet<VertexEvent> resultSet = new TreeSet<>();
//
//		resultSet.addAll(this.getEvents(time, tr));
//
//		List<Direction> directions = new LinkedList<>();
//		if (awareOutEvents)
//			directions.add(Direction.OUT);
//		if (awareInEvents)
//			directions.add(Direction.IN);
//
//		for (Direction direction : directions) {
//			for (Edge e : this.getEdges(direction, null)) {
//				e.getEvents(time, tr).stream().map(Event::getTime).forEach(t -> {
//					resultSet.add(new MChronoVertexEvent(this, t));
//				});
//			}
//		}
//		return resultSet;
		// TODO
		return null;
	}

	@Override
	public void removeEvents(long time, TemporalRelation tr) {
//		this.events.removeIf(event -> {
//			if (TimeInstant.getTemporalRelation(time, event.getTime()).equals(tr))
//				return true;
//			else
//				return false;
//		});
		// TODO
	}

	public <T extends Event> NavigableSet<T> getEvents(long time, TemporalRelation... temporalRelations) {
//		NavigableSet<Event> validEvents = new TreeSet<>();
//		if (temporalRelations == null)
//			return (NavigableSet<T>) validEvents;
//
//		for (Event event : this.events) {
//			for (TemporalRelation tr : temporalRelations) {
//				if (TimeInstant.checkTemporalRelation(event.getTime(), time, tr))
//					validEvents.add(event);
//			}
//
//		}
//		return (NavigableSet<T>) validEvents;
		// TODO
		return null;
	}

	@Override
	public VertexEvent getEvent(long time, TemporalRelation tr) {
		PChronoGraph pg = (PChronoGraph) g;
		if (tr.equals(TemporalRelation.isAfter)) {
			Document gt = pg.vertexEvents.find(new Document("_v", id).append("$gt", new Document("_t", time))).first();
			return new PChronoVertexEvent(this, gt.getLong("_t"));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			Document lt = pg.vertexEvents.find(new Document("_v", id).append("$lt", new Document("_t", time))).first();
			return new PChronoVertexEvent(this, lt.getLong("_t"));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			Document eq = pg.vertexEvents.find(new Document("_v", id).append("_t", time)).first();
			return new PChronoVertexEvent(this, eq.getLong("_t"));
		} else {
			throw new IllegalArgumentException("Illegal temporal relation");
		}
	}
}
