package com.tinkerpop.blueprints;

import java.util.NavigableSet;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;

/**
 * An Edge links two vertices. Along with its key/value properties, an edge has
 * both a directionality and a label. The directionality determines which vertex
 * is the tail vertex (out vertex) and which vertex is the head vertex (in
 * vertex). The edge label determines the type of relationship that exists
 * between the two vertices. Diagrammatically, outVertex ---label---&gt;
 * inVertex.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Jaewook Byun, Ph.D., Assistant Professor, Department of Software,
 *         Sejong University (slightly modify interface)
 */
public interface Edge extends Element {

	/**
	 * Return the tail/out or head/in vertex.
	 *
	 * @param direction whether to return the tail/out or head/in vertex
	 * @return the tail/out or head/in vertex
	 * @throws IllegalArgumentException is thrown if a direction of both is provided
	 */
	public Vertex getVertex(Direction direction) throws IllegalArgumentException;

	/**
	 * Return the label associated with the edge.
	 *
	 * @return the edge label
	 */
	public String getLabel();

	/**
	 * Remove the element from the graph.
	 */
	public void remove();
	
	public Document toDocument(boolean includeProperties);
	
	/**
	 * Add an event valid at time. The caller (Element) keeps distinct events
	 * regarding their valid time.
	 * 
	 * If time is an instance of TimeInstant and a time instant t is equal to
	 * existing time instant or is in a range of existing time-period, the method
	 * fails and return null.
	 * 
	 * If time is an instance of TimeInstant and a time instant t is not equal to
	 * any existing time instant or is not in a range of any existing time-period,
	 * return a newly created event.
	 * 
	 * If time is an instance of TimePeriod, the method may return a newly created
	 * event. If the time-period p covers any time-instants, the instants are merged
	 * to p. If the time-period p is overlapped with other time-periods, p extends.
	 * If the time-period p is exactly equal to an existing time-period, the method
	 * fails and returns null.
	 * 
	 * @param time the new time to be added
	 * @return VertexEvent or EdgeEvent
	 */
	public EdgeEvent addEvent(long time);

	/**
	 * Return an event of this graph element valid at time
	 * 
	 * @param time the time to match
	 * @return EdgeEvent
	 */
	public EdgeEvent getEvent(long time);

	/**
	 * Return events of this element that are matched with tr for time
	 *
	 * @param time the time to check in the events
	 * @param temporalRelation the temporal relation of time
	 * @return NavigableSet of VertexEvent or EdgeEvent
	 */
	public NavigableSet<EdgeEvent> getEvents(long time, TemporalRelation temporalRelation);
	
	/**
	 * 
	 * @return events of this element
	 */
	public NavigableSet<EdgeEvent> getEvents();
	
	/**
	 * Return events of this element that are matched with tr for time
	 *
	 * @param time the time to check in the events
	 * @return EdgeEvent
	 */
	public EdgeEvent getEvent(long time, TemporalRelation temporalRelation);

	/**
	 * Remove all the events that are matched with temporalRelation for time
	 * 
	 * @param time the time to check
	 * @param temporalRelation the temporal relation to match
	 */
	public void removeEvents(long time, TemporalRelation temporalRelation);
}
