package org.dfpl.chronograph.kairos;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Graph;

@SuppressWarnings("rawtypes")
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

	public GammaTable getGammaTable() {
		return gammaTable;
	}

	public String getName() {
		return name;
	}

	public abstract void onInitialization();

	public abstract void onAddEdgeEvent(EdgeEvent newEvent);

	public abstract void onRemoveEdgeEvent(EdgeEvent eventToBeRemoved);

	public abstract void onSetEdgeEventProperty(EdgeEvent event, String key, Object value);
}
