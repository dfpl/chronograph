package org.dfpl.chronograph.kairos;

import java.util.Set;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

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

	public abstract void onAddEdgeEvent(EdgeEvent newEvent);

	public abstract void onRemoveEdgeEvent(EdgeEvent eventToBeRemoved);

	public abstract void onSetEdgeEventProperty(EdgeEvent event, String key, Object value);
}
