package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;

import org.dfpl.chronograph.common.TemporalRelation;

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
public class ChronoVertexEvent implements VertexEvent, Comparable<ChronoVertexEvent> {

	private final Vertex vertex;
	private final Long time;
	private HashMap<String, Object> properties;

	public ChronoVertexEvent(Vertex v, Long time) {
		this.vertex = v;
		this.time = time;
		this.properties = new HashMap<String, Object>();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Event))
			return false;
		return this.vertex.equals(((Event) obj).getElement()) && this.getTime().equals(((Event) obj).getTime());
	}

	@Override
	public String toString() {
		return vertex.getId() + "_" + time;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	/**
	 * Checks the difference of two events by comparing the element, and then the
	 * element
	 * 
	 * @param event the event to be compared
	 * @return Integer difference
	 */
	@Override
	public int compareTo(ChronoVertexEvent event) {
		int elementComparison = this.getElement().getId().compareTo(event.getElement().getId());

		if (elementComparison != 0)
			return elementComparison;

		return this.getTime().compareTo(event.getTime());
	}

	@Override
	public String getId() {
		return vertex.getId() + "_" + time;
	}

	@Override
	public String getElementId() {
		return vertex.getId();
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
	public Long getTime() {
		return time;
	}

	@Override
	public Element getElement() {
		return vertex;
	}

	@Override
	public Collection<EdgeEvent> getEdgeEvents(Direction direction, TemporalRelation tr, String label) {
		return vertex.getEdges(direction, List.of(label)).parallelStream().map(e -> e.getEvent(time, tr)).toList();
	}

	@Override
	public Collection<VertexEvent> getVertexEvents(Direction direction, TemporalRelation tr, String label) {
		return vertex.getEdges(direction, List.of(label)).parallelStream()
				.map(e -> {
					EdgeEvent neighborEe = e.getEvent(time, tr);
					if(neighborEe == null)
						return null;
					else
						return neighborEe.getVertexEvent(direction.opposite());
				} ).toList();
	}
	
	public JsonObject toJsonObject(boolean includeProperties) {
		JsonObject object = new JsonObject();
		object.put("_id", vertex.getId());
		object.put("_t", time);
		if (includeProperties)
			object.put("properties", new JsonObject(properties));
		return object;
	}

}
