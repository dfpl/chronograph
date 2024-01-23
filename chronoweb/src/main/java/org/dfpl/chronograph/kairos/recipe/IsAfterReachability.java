package org.dfpl.chronograph.kairos.recipe;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Graph;

public class IsAfterReachability extends AbstractKairosProgram<Integer> {

	public IsAfterReachability(Graph graph, GammaTable<String, Integer> gammaTable) {
		super(graph, gammaTable, "IsAfterReachability");
	}

	@Override
	public void onInitialization() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAddEdgeEvent(EdgeEvent newEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveEdgeEvent(EdgeEvent eventToBeRemoved) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSetEdgeEventProperty(EdgeEvent event, String key, Object value) {
		// TODO Auto-generated method stub

	}

}
