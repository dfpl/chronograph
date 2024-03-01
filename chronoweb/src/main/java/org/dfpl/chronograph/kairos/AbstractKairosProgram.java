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
	 * @param sources a set of source vertices
	 * @param startTime the reference time
	 */
	public abstract void onInitialization(Set<Vertex> sources, Long startTime);
	
	/**
	 * Compute the temporal information diffusion result after adding a vertex
	 * 
	 * @param addedVertex the vertex to add
	 */
	public abstract void onAddVertex(Vertex addedVertex);
	
	/**
	 * Compute the 
	 * @param addedEdge
	 */
	public abstract void onAddEdge(Edge addedEdge);

	public abstract void onUpdateVertexProperty(Document previous, Document updated);

	public abstract void onUpdateEdgeProperty(Document previous, Document updated);

	public abstract void onRemoveVertex(Vertex removedVertex);

	public abstract void onRemoveEdge(Edge removedEdge);

	public abstract void onAddVertexEvent(VertexEvent addedVertexEvent);

	public abstract void onAddEdgeEvent(EdgeEvent addedEvent);

	public abstract void onUpdateVertexEventProperty(Document previous, Document updated);

	public abstract void onUpdateEdgeEventProperty(Document previous, Document updated);

	public abstract void onRemoveVertexEvent(VertexEvent removedVertex);

	public abstract void onRemoveEdgeEvent(EdgeEvent removedEdge);
}
