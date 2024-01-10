package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;

import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.TimeInstant;

import com.tinkerpop.blueprints.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * The in-memory implementation of temporal graph database.
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
public class ChronoEdge implements Edge {

	private Graph g;
	private String id;
	private Vertex out;
	private String label;
	private Vertex in;
	private HashMap<String, Object> properties;
	private NavigableSet<EdgeEvent> events;

	public ChronoEdge(Graph g, Vertex out, String label, Vertex in) {
		this.g = g;
		this.out = out;
		this.label = label;
		this.in = in;
		this.id = getEdgeID(out, in, label);
		this.properties = new HashMap<>();
		this.events = new TreeSet<EdgeEvent>();
	}

	public static String getEdgeID(Vertex out, Vertex in, String label) {
		return out.toString() + "|" + label + "|" + in.toString();
	}

	@Override
	public Vertex getVertex(Direction direction) throws IllegalArgumentException {
		if (direction.equals(Direction.OUT)) {
			return out;
		} else if (direction.equals(Direction.IN)) {
			return in;
		} else {
			throw new IllegalArgumentException("A direction should be either OUT or IN");
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void remove() {
		g.removeEdge(this);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Edge))
			return false;
		return id.equals(obj.toString());
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key) {
		return (T) properties.get(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return this.properties.keySet();
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		return (T) properties.remove(key);
	}

	public void setProperties(JsonObject properties, boolean isSet) {
		if (!isSet)
			this.properties.clear();
		properties.stream().forEach(e -> {
			this.properties.put(e.getKey(), e.getValue());
		});
	}

	public JsonObject toJsonObject(boolean includeProperties) {
		JsonObject object = new JsonObject();
		object.put("_id", id);
		object.put("_o", out.getId());
		object.put("_l", label);
		object.put("_i", in.getId());
		if (includeProperties)
			object.put("properties", new JsonObject(properties));
		return object;
	}

	public static JsonArray toJsonArrayOfIDs(Collection<Edge> edges) {
		JsonArray array = new JsonArray();
		edges.parallelStream().forEach(e -> array.add(e.getId()));
		return array;
	}

	@Override
	public EdgeEvent addEvent(long time) {
		EdgeEvent newEvent = new ChronoEdgeEvent(this, time);
		this.events.add(newEvent);
		return newEvent;
	}

	@Override
	public EdgeEvent getEvent(long time) {
		EdgeEvent event = events.floor(new ChronoEdgeEvent(this, time));
		if (event == null)
			return null;
		else if (event.getTime() != time)
			return null;
		else
			return event;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents() {
		return events;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents(long time, TemporalRelation temporalRelation) {
		NavigableSet<EdgeEvent> validEvents = new TreeSet<>();
		if (temporalRelation == null)
			return validEvents;

		for (EdgeEvent event : this.events) {
			if (TimeInstant.getTemporalRelation(time, event.getTime()).equals(temporalRelation))
				validEvents.add(event);
		}
		return validEvents;
	}

	@Override
	public EdgeEvent getEvent(long time, TemporalRelation temporalRelation) {
		if (temporalRelation.equals(TemporalRelation.isAfter)) {
			return events.higher(new ChronoEdgeEvent(this, time));
		} else if (temporalRelation.equals(TemporalRelation.isBefore)) {
			return events.lower(new ChronoEdgeEvent(this, time));
		} else {
			return getEvent(time);
		}
	}

	@Override
	public void removeEvents(long time, TemporalRelation temporalRelation) {
		this.events.removeIf(event -> TimeInstant.getTemporalRelation(time, event.getTime()).equals(temporalRelation));
	}
}
