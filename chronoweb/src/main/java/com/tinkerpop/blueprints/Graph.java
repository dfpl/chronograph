package com.tinkerpop.blueprints;

import java.util.Collection;

/**
 * A Graph is a container object for a collection of vertices and a collection
 * edges.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Jaewook Byun, Ph.D., Assistant Professor, Department of Software,
 *         Sejong University (slightly modify interface)
 */
public interface Graph {

	/**
	 * Create a new vertex, add it to the graph, and return the newly created
	 * vertex. The provided object identifier is a recommendation for the identifier
	 * to use. It is not required that the implementation use this identifier.
	 *
	 * @param id the recommended object identifier
	 * @return the newly created vertex
	 */
	public Vertex addVertex(String id);

	/**
	 * Return the vertex referenced by the provided object identifier. If no vertex
	 * is referenced by that identifier, then return null.
	 *
	 * @param id the identifier of the vertex to retrieved from the graph
	 * @return the vertex referenced by the provided identifier or null when no such
	 *         vertex exists
	 */
	public Vertex getVertex(String id);

	/**
	 * Remove the provided vertex from the graph. Upon removing the vertex, all the
	 * edges by which the vertex is connected must be removed as well.
	 *
	 * @param vertex the vertex to remove from the graph
	 */
	public void removeVertex(Vertex vertex);

	/**
	 * Return an iterable to all the vertices in the graph. If this is not possible
	 * for the implementation, then an UnsupportedOperationException can be thrown.
	 *
	 * @return an iterable reference to all vertices in the graph
	 */
	public Collection<Vertex> getVertices();

	/**
	 * Return an iterable to all the vertices in the graph that have a particular
	 * key/value property. If this is not possible for the implementation, then an
	 * UnsupportedOperationException can be thrown. The graph implementation should
	 * use indexing structures to make this efficient else a full vertex-filter scan
	 * is required.
	 *
	 * @param key   the key of vertex
	 * @param value the value of the vertex
	 * @return an iterable of vertices with provided key and value
	 */
	public Collection<Vertex> getVertices(String key, Object value);

	/**
	 * Add an edge to the graph. The added edges requires a recommended identifier,
	 * a tail vertex, an head vertex, and a label. Like adding a vertex, the
	 * provided object identifier may be ignored by the implementation.
	 *
	 * @param outVertex the vertex on the tail of the edge
	 * @param inVertex  the vertex on the head of the edge
	 * @param label     the label associated with the edge
	 * @return the newly created edge
	 * 
	 */
	public Edge addEdge(Vertex outVertex, Vertex inVertex, String label);

	/**
	 * Return the edge with the unique combination of out-going vertex, in-going
	 * vertex, and label. If no edge is referenced by that identifier, then return
	 * null.
	 * 
	 * @param outVertex out-going vertex
	 * @param inVertex  in-going vertex
	 * @param label     edge label
	 * @return
	 */
	public Edge getEdge(Vertex outVertex, Vertex inVertex, String label);

	/**
	 * Return the edge referenced by the provided object identifier. If no edge is
	 * referenced by that identifier, then return null.
	 *
	 * @param id the identifier of the edge to retrieved from the graph
	 * @return the edge referenced by the provided identifier or null when no such
	 *         edge exists
	 */
	public Edge getEdge(String id);

	/**
	 * Remove the provided edge from the graph.
	 *
	 * @param edge the edge to remove from the graph
	 */
	public void removeEdge(Edge edge);

	/**
	 * Return an iterable to all the edges in the graph. If this is not possible for
	 * the implementation, then an UnsupportedOperationException can be thrown.
	 *
	 * @return an iterable reference to all edges in the graph
	 */
	public Collection<Edge> getEdges();

	/**
	 * Return an iterable to all the edges in the graph that have a particular
	 * key/value property. If this is not possible for the implementation, then an
	 * UnsupportedOperationException can be thrown. The graph implementation should
	 * use indexing structures to make this efficient else a full edge-filter scan
	 * is required.
	 *
	 * @param key   the key of the edge
	 * @param value the value of the edge
	 * @return an iterable of edges with provided key and value
	 */
	public Collection<Edge> getEdges(String key, Object value);
	
	/**
	 * A shutdown function is required to properly close the graph. This is
	 * important for implementations that utilize disk-based serializations.
	 */
	public void shutdown();

	/**
	 * Clear the graph
	 */
	public void clear();
}
