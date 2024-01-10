package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;

import org.dfpl.chronograph.common.TemporalRelation;

import com.tinkerpop.blueprints.*;

/**
 * The in-memory implementation of temporal graph database.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Master Student, DFPL, Sejong University
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020). *
 * 
 */
public class ChronoVertexEvent implements VertexEvent, Comparable<ChronoVertexEvent> {

	private final Vertex vertex;
	private final Long time;

	public ChronoVertexEvent(Vertex v, Long time) {
		this.vertex = v;
		this.time = time;
	}

	@SuppressWarnings("unchecked")
	public <T extends Event> NavigableSet<T> getVertexEvents(Direction direction, TemporalRelation tr,
			String[] labels) {
		NavigableSet<ChronoVertexEvent> validEvents = new TreeSet<>(ChronoVertexEvent::compareTo);

		Collection<Vertex> neighborVertices = ((ChronoVertex) this.getElement()).getVertices(direction, null);

		for (Iterator<Vertex> vIter = neighborVertices.iterator(); vIter.hasNext();) {
			// Vertex vertex = vIter.next();
			// Event currEvent = vertex.getEvent(getTime(), tr, false, false);
			// if (currEvent != null) {
			// validEvents.add((ChronoVertexEvent) currEvent);
			// }
		}

		return (NavigableSet<T>) validEvents;
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
		return vertex.getProperties();
	}

	@Override
	public <T> T getProperty(String key) {
		return vertex.getProperty(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return vertex.getPropertyKeys();
	}

	@Override
	public void setProperty(String key, Object value) {
		vertex.setProperty(key, value);

	}

	@Override
	public <T> T removeProperty(String key) {
		return vertex.removeProperty(key);
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
				.map(e -> e.getEvent(time, tr).getVertexEvent(direction.opposite())).toList();
	}
}
