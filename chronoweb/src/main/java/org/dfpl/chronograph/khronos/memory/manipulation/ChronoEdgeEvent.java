package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tinkerpop.blueprints.*;

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
public class ChronoEdgeEvent implements EdgeEvent, Comparable<ChronoEdgeEvent> {
	private final Edge edge;
	private final Long time;
	private HashMap<String, Object> properties;

	public ChronoEdgeEvent(Edge e, Long time) {
		this.edge = e;
		this.time = time;
		this.properties = new HashMap<String, Object>();
	}

	@Override
	public VertexEvent getVertexEvent(Direction direction) {
		return edge.getVertex(direction).getEvent(time);
	}

	/**
	 * Checks the difference of two events by comparing the element, and then the
	 * element
	 *
	 * @param event the event to be compared
	 * @return Integer difference
	 */
	@Override
	public int compareTo(ChronoEdgeEvent event) {
		int elementComparison = this.getElement().getId().compareTo(event.getElement().getId());

		if (elementComparison != 0)
			return elementComparison;

		return this.getTime().compareTo(event.getTime());
	}

	@Override
	public Long getTime() {
		return time;
	}

	@Override
	public Element getElement() {
		return edge;
	}

	@Override
	public String getId() {
		return edge.getId() + "_" + time;
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
		return properties.keySet();
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public void setProperties(JsonObject properties, boolean isSet) {
		if (!isSet)
			this.properties.clear();
		properties.stream().forEach(e -> {
			this.properties.put(e.getKey(), e.getValue());
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		return (T) properties.remove(key);
	}

	@Override
	public Vertex getVertex(Direction direction) {
		return edge.getVertex(direction);
	}

	@Override
	public String getLabel() {
		return edge.getLabel();
	}

	@Override
	public String getElementId() {
		return edge.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EdgeEvent))
			return false;
		return this.edge.equals(((Event) obj).getElement()) && this.getTime().equals(((Event) obj).getTime());
	}

	@Override
	public String toString() {
		return edge.getId() + "_" + time;
	}
	
	public JsonObject toJsonObject(boolean includeProperties) {
		JsonObject object = ((ChronoEdge) edge).toJsonObject(false);
		if (includeProperties)
			object.put("properties", new JsonObject(properties));
		return object;
	}
}
