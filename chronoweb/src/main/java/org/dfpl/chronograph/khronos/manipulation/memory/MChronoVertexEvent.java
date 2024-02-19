package org.dfpl.chronograph.khronos.manipulation.memory;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

import com.tinkerpop.blueprints.*;

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
public class MChronoVertexEvent implements VertexEvent, Comparable<MChronoVertexEvent> {

	private final Vertex vertex;
	private final Long time;
	private Document properties;

	public MChronoVertexEvent(Vertex v, Long time) {
		this.vertex = v;
		this.time = time;
		this.properties = new Document();
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
	public int compareTo(MChronoVertexEvent event) {
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
	public Document getProperties() {
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

	public void setProperties(Document properties, boolean isSet) {
		if (!isSet) {
			this.properties = properties;
		} else {
			for (String key : properties.keySet()) {
				this.properties.put(key, properties.get(key));
			}
		}
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
		return vertex.getEdges(direction, List.of(label)).parallelStream().map(e -> {
			EdgeEvent neighborEe = e.getEvent(time, tr);
			if (neighborEe == null)
				return null;
			else
				return neighborEe.getVertexEvent(direction.opposite());
		}).toList();
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", vertex.getId() + "_" + time);
		object.put("_v", vertex.getId());
		object.put("_t", time);
		if (includeProperties)
			object.put("properties", properties);
		return object;
	}
}
