package org.dfpl.chronograph.kairos.program;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.LongGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class IsAfterReachability extends AbstractKairosProgram<Long> {

	public IsAfterReachability(Graph graph, GammaTable<String, Long> gammaTable) {
		super(graph, gammaTable, "IsAfterReachability");
	}

	Predicate<Long> sourceTest = new Predicate<Long>() {
		@Override
		public boolean test(Long t) {
			if (t.longValue() == 9187201950435737471l)
				return false;
			return true;
		}
	};

	BiPredicate<Long, Long> targetTest = new BiPredicate<Long, Long>() {
		@Override
		public boolean test(Long t, Long u) {
			if (u < t)
				return true;

			return false;
		}
	};

	@Override
	public void onInitialization(Set<Vertex> sources, Long startTime) {
		synchronized (gammaTable) {
			for(Vertex s: sources) {
				gammaTable.set(s.getId(), s.getId(), new LongGammaElement(startTime));
			}
			
			Iterator<Entry<Long, HashSet<EdgeEvent>>> iter = ((MChronoGraph) graph).getEdgeEventIterator();
			while (iter.hasNext()) {
				Entry<Long, HashSet<EdgeEvent>> eventEntry = iter.next();
				Long t = eventEntry.getKey();
				HashSet<EdgeEvent> events = eventEntry.getValue();
				for (EdgeEvent event : events) {
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new LongGammaElement(t), targetTest);
				}
			}
		}
	}

	@Override
	public void onAddEdgeEvent(EdgeEvent newEvent) {
		synchronized (gammaTable) {
			gammaTable.update(newEvent.getVertex(Direction.OUT).getId(), sourceTest,
					newEvent.getVertex(Direction.IN).getId(), new LongGammaElement(newEvent.getTime()), targetTest);
			// gammaTable.print();
		}
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
