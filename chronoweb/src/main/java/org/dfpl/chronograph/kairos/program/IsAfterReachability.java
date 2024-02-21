package org.dfpl.chronograph.kairos.program;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.LongGammaElement;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
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
			for (Vertex s : sources) {
				gammaTable.set(s.getId(), s.getId(), new LongGammaElement(startTime));
			}

			for (Edge e : graph.getEdges()) {
				for (EdgeEvent event : e.getEvents()) {
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new LongGammaElement(event.getTime()), targetTest);
				}
			}
		}
	}

	@Override
	public void onAddEdgeEvent(EdgeEvent addedEvent) {
		synchronized (gammaTable) {
			gammaTable.update(addedEvent.getVertex(Direction.OUT).getId(), sourceTest,
					addedEvent.getVertex(Direction.IN).getId(), new LongGammaElement(addedEvent.getTime()), targetTest);
		}
	}

	@Override
	public void onRemoveEdgeEvent(EdgeEvent removedEdge) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAddVertex(Vertex addedVertex) {

	}

	@Override
	public void onAddEdge(Edge addedEdge) {

	}

	@Override
	public void onUpdateVertexProperty(Document previous, Document updated) {

	}

	@Override
	public void onUpdateEdgeProperty(Document previous, Document updated) {

	}

	@Override
	public void onRemoveVertex(Vertex removedVertex) {

	}

	@Override
	public void onRemoveEdge(Edge removedEdge) {

	}

	@Override
	public void onAddVertexEvent(VertexEvent addedVertexEvent) {

	}

	@Override
	public void onUpdateVertexEventProperty(Document previous, Document updated) {

	}

	@Override
	public void onUpdateEdgeEventProperty(Document previous, Document updated) {

	}

	@Override
	public void onRemoveVertexEvent(VertexEvent removedVertex) {

	}

}
