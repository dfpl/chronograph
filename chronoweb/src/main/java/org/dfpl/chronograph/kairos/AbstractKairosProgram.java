package org.dfpl.chronograph.kairos;

import org.dfpl.chronograph.common.EdgeEvent;

import com.tinkerpop.blueprints.Graph;

public abstract class AbstractKairosProgram {
	// Data Abstraction
	private Graph graph;
	private GammaTable gammaTable;

	public AbstractKairosProgram(Graph graph, GammaTable gammaTable) {
		this.graph = graph;
		this.gammaTable = gammaTable;
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

	public void setGammaMap(GammaTable gammaTable) {
		this.gammaTable = gammaTable;
	}

	public abstract void onInitialization();

	public abstract void onAddEdgeEvent(EdgeEvent newEvent);

	public abstract void onRemoveEdgeEvent(EdgeEvent eventToBeRemoved);

	public abstract void onSetEdgeEventProperty(EdgeEvent event, String key, Object value);
}
