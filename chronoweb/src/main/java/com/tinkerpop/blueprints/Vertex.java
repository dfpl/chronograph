package com.tinkerpop.blueprints;

import java.util.List;

import org.bson.Document;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

/**
 * A vertex maintains pointers to both a set of incoming and outgoing edges. The
 * outgoing edges are those edges for which the vertex is the tail. The incoming
 * edges are those edges for which the vertex is the head. Diagrammatically,
 * ---inEdges---&gt; vertex ---outEdges---&gt;.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Matthias Brocheler (http://matthiasb.com)
 * @author Jaewook Byun, Ph.D., Assistant Professor, Department of Software,
 *         Sejong University (slightly modify interface)
 */
public interface Vertex extends Element {

	/**
	 * Return the edges incident to the vertex according to the provided direction
	 * and edge labels. The resulting collection would have multiple e(i|j) with
	 * multiple labels. labels of candidate edges should be included in labels if
	 * labels are empty, all the candidate edges should be returned.
	 *
	 * @param direction the direction of the edges to retrieve
	 * @param labels    the labels of the edges to retrieve
	 * @return a collection of incident edges
	 */
	public Iterable<Edge> getEdges(Direction direction, List<String> labels);

	/**
	 * Return the vertices adjacent to the vertex according to the provided
	 * direction and edge labels. The resulting collection does not have redundancy
	 * 
	 * This method would remove duplicate vertices according to the definition of
	 * Vertex.getEdges
	 *
	 * @param direction the direction of the edges of the adjacent vertices
	 * @param labels    the labels of the edges of the adjacent vertices
	 * @return a collection of adjacent vertices
	 */
	public Iterable<Vertex> getVertices(Direction direction, List<String> labels);

	/**
	 * Add a new outgoing edge from this vertex to the parameter vertex with
	 * provided edge label.
	 *
	 * @param label    the label of the edge
	 * @param inVertex the vertex to connect to with an incoming edge
	 * @return the newly created edge
	 */
	public Edge addEdge(String label, Vertex inVertex);

	/**
	 * Remove the element from the graph.
	 */
	public void remove();

	/**
	 * Convert the vertex to a document
	 * 
	 * @param includeProperties includes the properties in the return document if
	 *                          set to true
	 * @return a document representation
	 */
	public Document toDocument(boolean includeProperties);

	/**
	 * Explicitly add a vertex event of this graph element valid at time.
	 * 
	 * @param time long
	 * @return the created vertex event
	 */
	public VertexEvent addEvent(long time);

	/**
	 * Return a vertex event of this graph element valid at time.
	 * 
	 * @param time long
	 * @return VertexEvent valid at time
	 */
	public VertexEvent getEvent(long time);

	/**
	 * Return events of this element. In addition, the method includes:
	 * <ul>
	 * <li>in-going vertex event for out-going edge events if aware out events</li>
	 * <li>and out-going vertex event for in-going edge events if aware in
	 * events.</li>
	 * </ul>
	 * 
	 * @param awareOutEvents include in-going vertex events for out-going edge
	 *                       events
	 * @param awareInEvents  include out-going vertex events for in-going edge
	 *                       events
	 * @return NavigableSet of VertexEvent or EdgeEvent
	 */
	public Iterable<VertexEvent> getEvents(boolean awareOutEvents, boolean awareInEvents);

	/**
	 * Return an event of this element that are matched with temporalRelation for
	 * time
	 *
	 * @param time             the time to check in the events
	 * @param temporalRelation the temporal relation to match
	 * @return EdgeEvent
	 */
	public VertexEvent getEvent(long time, TemporalRelation temporalRelation);

	/**
	 * Return events of this element that are matched with tr for time. In addition
	 * to getEvents(time, temporalRelation), the method includes:
	 * <ul>
	 * <li>in-going vertex event for out-going edge events if awareOutEvents,
	 * and</li>
	 * <li>out-going vertex event for in-going edge events if awareInEvents.
	 * </ul>
	 * 
	 * @param time           the time to check
	 * @param tr             the temporal relation to match with time
	 * @param awareOutEvents include in-going vertex events for out-going edge
	 *                       events
	 * @param awareInEvents  include out-going vertex events for in-going edge
	 *                       events
	 * @return NavigableSet of VertexEvent or EdgeEvent
	 */
	public Iterable<VertexEvent> getEvents(long time, TemporalRelation tr, boolean awareOutEvents,
			boolean awareInEvents);

	/**
	 * Remove all the events that are matched with tr for time
	 * 
	 * @param time the time to check
	 * @param tr   the temporal relation to match with time
	 */
	public void removeEvents(long time, TemporalRelation tr);
}
