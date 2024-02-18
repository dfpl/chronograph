package org.dfpl.chronograph.khronos.persistent.manipulation;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.tinkerpop.blueprints.*;

import io.vertx.core.json.JsonArray;

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
public class PChronoVertex implements Vertex {

	private PChronoGraph g;
	private String id;

	PChronoVertex(PChronoGraph g, String id) {
		this.id = id;
		this.g = g;
	}

	@Override
	public Collection<Edge> getEdges(Direction direction, List<String> labels) {

		Document query = new Document();

		if (direction.equals(Direction.OUT))
			query.append("_o", id);
		else if (direction.equals(Direction.IN))
			query.append("_i", id);

		if (labels != null && labels.size() == 1) {
			query.append("_l", labels.get(0));
		} else {
			query.append("_l", new Document("$in", labels));
		}
		HashSet<Edge> edges = new HashSet<Edge>();
		MongoCursor<Document> cursor = g.edges.find(query).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			edges.add(new PChronoEdge(g, doc.getString("_id")));
		}
		return edges;
	}

	@Override
	public Collection<Vertex> getVertices(Direction direction, List<String> labels) {
		Document query = new Document();

		if (direction.equals(Direction.OUT))
			query.append("_o", id);
		else if (direction.equals(Direction.IN))
			query.append("_i", id);

		if (labels != null && labels.size() == 1) {
			query.append("_l", labels.get(0));
		} else {
			query.append("_l", new Document("$in", labels));
		}
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		MongoCursor<Document> cursor = g.edges.find(query).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			if (direction.equals(Direction.OUT))
				vertices.add(new PChronoVertex(g, doc.getString("_i")));
			else if (direction.equals(Direction.IN))
				vertices.add(new PChronoVertex(g, doc.getString("_o")));
		}
		return vertices;
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex) {
		return g.addEdge(this, inVertex, label);
	}

	@Override
	public void remove() {
		g.removeVertex(this);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Document getProperties() {
		try {
			return g.vertices.find(new Document("_id", id)).first().get("properties", Document.class);
		} catch (Exception e) {
			return new Document();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key) {
		try {
			return (T) g.vertices.find(new Document("_id", id)).first().get("properties", Document.class).get(key);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Set<String> getPropertyKeys() {
		try {
			return g.vertices.find(new Document("_id", id)).first().get("properties", Document.class).keySet();
		} catch (Exception e) {
			return new HashSet<String>();
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		g.vertices.updateOne(new Document("_id", id), new Document("$set", new Document("properties." + key, value)),
				new UpdateOptions().upsert(true));
	}

	public void setProperties(Document properties, boolean isSet) {
		if (!isSet) {
			g.vertices.updateOne(new Document("_id", id), new Document("$set", new Document("properties", properties)),
					new UpdateOptions().upsert(true));
		} else {
			Document existingProperties = g.vertices.find(new Document("_id", id)).first().get("properties",
					Document.class);
			if (existingProperties == null) {
				existingProperties = properties;
			}else {
				for (String key : properties.keySet()) {
					existingProperties.put(key, properties.get(key));
				}
				g.vertices.updateOne(new Document("_id", id), new Document("$set", new Document("properties", properties)),
						new UpdateOptions().upsert(true));
			}
		}
	}

	@Override
	public <T> T removeProperty(String key) {
		// return (T) properties.remove(key);
		// TODO
		return null;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PChronoVertex))
			return false;
		return this.getId().equals(((PChronoVertex) obj).getId());
	}

	@Override
	public String toString() {
		return id;
	}

	public Document toDocument(boolean includeProperties) {
//		JsonObject object = new JsonObject();
//		object.put("_id", id);
//		if (includeProperties)
//			object.put("properties", new JsonObject(properties));
//		return object;
		// TODO
		return null;
	}

	public static JsonArray toJsonArrayOfIDs(Collection<Vertex> edges) {
		JsonArray array = new JsonArray();
		edges.parallelStream().forEach(v -> array.add(v.getId()));
		return array;
	}

	@Override
	public VertexEvent addEvent(long time) {
//		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
//		if (event == null) {
//			VertexEvent newVe = new MChronoVertexEvent(this, time);
//			this.events.add(newVe);
//			if (g.getEventBus() != null)
//				g.getEventBus().send("addVertexEvent", newVe.getId());
//			return newVe;
//		} else
//			return event;
		// TODO
		return null;
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
//		MChronoVertexEvent tve = new MChronoVertexEvent(this, time);
//		if (tr.equals(TemporalRelation.isAfter)) {
//			return events.higher(tve);
//		} else if (tr.equals(TemporalRelation.isBefore)) {
//			return events.lower(tve);
//		} else {
//			VertexEvent ve = events.floor(tve);
//			if (ve == null)
//				return null;
//			else if (ve.equals(tve))
//				return ve;
//			else
//				return null;
//		}
		// TODO
		return null;
	}
}
