package org.dfpl.chronograph.kairos.recipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoGraph;

import com.tinkerpop.blueprints.Graph;

public class IsAfterReachability extends AbstractKairosProgram<Integer> {

	public IsAfterReachability(Graph graph, GammaTable<String, Integer> gammaTable) {
		super(graph, gammaTable, "IsAfterReachability");
	}

	@SuppressWarnings("unused")
	@Override
	public void onInitialization() {
		Iterator<Entry<Long, HashSet<EdgeEvent>>> iter = ((ChronoGraph) graph).getEdgeEventIterator();
		while (iter.hasNext()) {
			Entry<Long, HashSet<EdgeEvent>> eventEntry = iter.next();
			Long t = eventEntry.getKey();
			HashSet<EdgeEvent> events = eventEntry.getValue();

		}
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
