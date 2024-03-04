package org.dfpl.chronograph.kairos;

import java.util.Set;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * An {@code AbstractKairosProgram} is a program that manages incremental
 * updates for temporal information diffusion analysis
 * 
 * @param <E> the data type of a gamma element
 */
public abstract class AbstractKairosProgram<E> {
	/**
	 * The graph associated with the {@code AbstractKairosProgram}
	 */
	protected Graph graph;

	protected GammaTable<String, E> gammaTable;
	/**
	 * The name of the program
	 */
	protected String name;

	/**
	 * Create a {@code AbstractKairosProgram} with the specified graph, gammaTable,
	 * and name
	 * 
	 * @param graph      the graph to set
	 * @param gammaTable the gamma table to set
	 * @param name       the name of the program
	 */
	public AbstractKairosProgram(Graph graph, GammaTable<String, E> gammaTable, String name) {
		this.graph = graph;
		this.gammaTable = gammaTable;
		this.name = name;
	}

	/**
	 * Return the associated graph
	 * 
	 * @return the graph
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Set the graph
	 * 
	 * @param graph the graph to set
	 */
	public void setG(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Return the Gamma Table
	 * 
	 * @param <T> the data type of the gamma element
	 * @return the Gamma Table
	 */
	@SuppressWarnings("unchecked")
	public <T> GammaTable<String, T> getGammaTable() {
		return (GammaTable<String, T>) gammaTable;
	}

	/**
	 * Return the name of the program
	 * 
	 * @return the name of the program
	 */
	public String getName() {
		return name;
	}

	/**
	 * Initialize the Gamma Table given the sources and start time
	 * 
	 * @param sources   a set of source vertices
	 * @param startTime the reference time
	 */
	public abstract void onInitialization(Set<Vertex> sources, Long startTime);

	/**
	 * Update the temporal information diffusion result after adding a vertex
	 * 
	 * @param addedVertex the vertex to add
	 */
	public abstract void onAddVertex(Vertex addedVertex);

	/**
	 * Update the temporal information diffusion result after adding an edge
	 * 
	 * @param addedEdge the edge to be added
	 */
	public abstract void onAddEdge(Edge addedEdge);

	/**
	 * Update the temporal information diffusion result after updating a vertex
	 * property
	 * 
	 * @param previous the previous vertex property
	 * @param updated  the updated vertex property
	 */
	public abstract void onUpdateVertexProperty(Document previous, Document updated);

	/**
	 * Update the temporal information diffusion result after updating an edge
	 * property
	 * 
	 * @param previous the previous edge property
	 * @param updated  the updated edge property
	 */
	public abstract void onUpdateEdgeProperty(Document previous, Document updated);

	/**
	 * Update the temporal information diffusion result after removing a vertex
	 * 
	 * @param removedVertex the vertex to be removed
	 */
	public abstract void onRemoveVertex(Vertex removedVertex);

	/**
	 * Update the temporal information diffusion result after removing an edge
	 * 
	 * @param removedEdge the edge to be removed
	 */
	public abstract void onRemoveEdge(Edge removedEdge);

	/**
	 * Update the temporal information diffusion result after adding a vertex event
	 * 
	 * @param addedVertexEvent the vertex event to be added
	 */
	public abstract void onAddVertexEvent(VertexEvent addedVertexEvent);

	/**
	 * Update the temporal information diffusion result after adding an edge event
	 * 
	 * @param addedEvent the edge event to be added
	 */
	public abstract void onAddEdgeEvent(EdgeEvent addedEvent);

	/**
	 * Update the temporal information diffusion result after updating a vertex
	 * event property
	 * 
	 * @param previous the previous vertex event property
	 * @param updated  the updated vertex event property
	 */
	public abstract void onUpdateVertexEventProperty(Document previous, Document updated);

	/**
	 * Update the temporal information diffusion result after updating an edge event
	 * property
	 * 
	 * @param previous the previous edge event property
	 * @param updated  the updated edge event property
	 */
	public abstract void onUpdateEdgeEventProperty(Document previous, Document updated);

	/**
	 * Update the temporal information diffusion result after removing a vertex
	 * event
	 * 
	 * @param removedEvent the vertex event to be removed
	 */
	public abstract void onRemoveVertexEvent(VertexEvent removedEvent);

	/**
	 * Update the temporal information diffusion result after removing an edge event
	 * 
	 * @param removedEvent the edge event to be removed
	 */
	public abstract void onRemoveEdgeEvent(EdgeEvent removedEvent);
}
