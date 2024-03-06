package org.dfpl.chronograph.kairos.program.reachability;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class OutIsAfterReachability extends AbstractKairosProgram<Long> {

	public OutIsAfterReachability(Graph graph, GammaTable<String, Long> gammaTable) {
		super(graph, gammaTable, "OutIsAfterReachability");
	}

	/**
	 * Return true if the source value has a valid value
	 */
	Predicate<Long> sourceTest = t -> t != 9187201950435737471l;

	/**
	 * Return true if the second argument is less than the first argument
	 */
	BiPredicate<Long, Long> targetTest = (t, u) -> u < t;

	@Override
	public void onInitialization(Set<Vertex> sources, Long startTime) {
		synchronized (gammaTable) {
			for (Vertex s : sources) {
				gammaTable.set(s.getId(), s.getId(), new LongGammaElement(startTime));
			}

			if (graph instanceof MChronoGraph mg) {
				mg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new LongGammaElement(event.getTime()), targetTest);
				});
			} else if (graph instanceof PChronoGraph pg) {
				pg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					gammaTable.update(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()),
							event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
							new LongGammaElement(event.getTime()), targetTest);
				});
			}
			gammaTable.print();
		}
	}

	@Override
	public void onAddEdgeEvent(EdgeEvent addedEvent) {
		synchronized (gammaTable) {
			gammaTable.update(addedEvent.getVertex(Direction.OUT).getId(), sourceTest,
					addedEvent.getVertex(Direction.IN).getId(), new LongGammaElement(addedEvent.getTime()), targetTest);
			gammaTable.print();
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
