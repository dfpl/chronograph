package org.dfpl.chronograph.kairos.program.set_reachability;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.db.StringSetGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class IsAfterSetReachability extends AbstractKairosProgram<Set<String>> {

	public IsAfterSetReachability(Graph graph, GammaTable<String, Set<String>> gammaTable) {
		super(graph, gammaTable, "IsAfterSetReachability");
	}

	Predicate<Set<String>> sourceTest = new Predicate<Set<String>>() {
		@Override
		public boolean test(Set<String> t) {
			if (t.isEmpty())
				return false;
			return true;
		}
	};

	BiPredicate<Set<String>, Set<String>> targetTest = new BiPredicate<Set<String>, Set<String>>() {
		@Override
		public boolean test(Set<String> t, Set<String> u) {
			for (String uv : u) {
				if (t.contains(uv))
					return false;
			}
			return true;
		}
	};

	@Override
	public void onInitialization(Set<Vertex> sources, Long startTime) {
		synchronized (gammaTable) {
			for (Vertex s : sources) {
				gammaTable.set(s.getId(), s.getId(), new StringSetGammaElement(s.getId()));
			}

			if (graph instanceof MChronoGraph mg) {
				mg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new StringSetGammaElement(event.getVertex(Direction.IN).getId()), targetTest);
					gammaTable.print();
				});
			} else if (graph instanceof PChronoGraph pg) {
				pg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new StringSetGammaElement(event.getVertex(Direction.IN).getId()), targetTest);
					gammaTable.print();
				});
			}
		}
	}

	@Override
	public void onAddEdgeEvent(EdgeEvent addedEvent) {
		synchronized (gammaTable) {
			gammaTable.update(addedEvent.getVertex(Direction.OUT).getId(), sourceTest,
					addedEvent.getVertex(Direction.IN).getId(),
					new StringSetGammaElement(addedEvent.getVertex(Direction.IN).getId()), targetTest);
			gammaTable.print();
		}
	}

	@Override
	public void onRemoveEdgeEvent(EdgeEvent removedEdge) {

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
