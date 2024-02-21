package org.dfpl.chronograph.kairos;

import java.util.Set;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractKairosProgram<E> {
	protected Graph graph;
	protected GammaTable<String, E> gammaTable;
	protected String name;

	public AbstractKairosProgram(Graph graph, GammaTable<String, E> gammaTable, String name) {
		this.graph = graph;
		this.gammaTable = gammaTable;
		this.name = name;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setG(Graph graph) {
		this.graph = graph;
	}

	@SuppressWarnings("unchecked")
	public <T> GammaTable<String, T> getGammaTable() {
		return (GammaTable<String, T>) gammaTable;
	}

	public String getName() {
		return name;
	}

	public abstract void onInitialization(Set<Vertex> sources, Long startTime);

	public abstract void onAddVertex(Vertex addedVertex);

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
