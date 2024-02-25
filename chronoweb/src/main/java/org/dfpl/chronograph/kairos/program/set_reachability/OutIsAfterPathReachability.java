package org.dfpl.chronograph.kairos.program.set_reachability;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.db.PathGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class OutIsAfterPathReachability extends AbstractKairosProgram<Document> {

	public OutIsAfterPathReachability(Graph graph, GammaTable<String, Document> gammaTable) {
		super(graph, gammaTable, "OutIsAfterPathReachability");
	}

	Predicate<Document> sourceTest = new Predicate<Document>() {
		@Override
		public boolean test(Document t) {
			if (t == null)
				return false;
			return true;
		}
	};

	BiPredicate<Document, Document> targetTest = new BiPredicate<Document, Document>() {
		@Override
		public boolean test(Document t, Document u) {
			if (u.getLong("time") < t.getLong("time"))
				return true;

			return false;
		}
	};

	@Override
	public void onInitialization(Set<Vertex> sources, Long startTime) {
		synchronized (gammaTable) {
			for (Vertex s : sources) {
				String id = s.getId();
				gammaTable.set(id, id, new PathGammaElement(List.of(id), startTime));
			}

			if (graph instanceof MChronoGraph mg) {
				mg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					String out = event.getVertex(Direction.OUT).getId();
					String in = event.getVertex(Direction.IN).getId();
					gammaTable.append(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()), out,
							sourceTest, in, new PathGammaElement(List.of(in), event.getTime()), targetTest);
					gammaTable.print();
				});
			} else if (graph instanceof PChronoGraph pg) {
				pg.getEdgeEvents().forEach(event -> {
					System.out.println("\t\t" + event);
					String out = event.getVertex(Direction.OUT).getId();
					String in = event.getVertex(Direction.IN).getId();
					gammaTable.append(sources.parallelStream().map(v -> v.getId()).collect(Collectors.toSet()), out,
							sourceTest, in, new PathGammaElement(List.of(in), event.getTime()), targetTest);
					gammaTable.print();
				});
			}
		}
	}

	@Override
	public void onAddEdgeEvent(EdgeEvent addedEvent) {
		synchronized (gammaTable) {
			String out = addedEvent.getVertex(Direction.OUT).getId();
			String in = addedEvent.getVertex(Direction.IN).getId();
			gammaTable.update(out, sourceTest, in, new PathGammaElement(List.of(in), addedEvent.getTime()), targetTest);
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
