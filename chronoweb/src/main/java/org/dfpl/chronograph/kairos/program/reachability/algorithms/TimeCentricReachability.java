package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class TimeCentricReachability {
	/**
	 * Return true if the second argument is less than the first argument
	 */
	private final static BiPredicate<Long, Long> IS_AFTER = (t, u) -> u < t;

	private final static Comparator<Long> IS_AFTER_COMPARATOR = Long::compareTo;

	private final static Comparator<Event> IS_AFTER_EVENT_COMPARATOR = (e1, e2) -> IS_AFTER_COMPARATOR
			.compare(e1.getTime(), e2.getTime());

	private final static Comparator<Long> IS_BEFORE_COMPARATOR = Comparator.reverseOrder();

	private final static Comparator<Event> IS_BEFORE_EVENT_COMPARATOR = (e1, e2) -> IS_BEFORE_COMPARATOR
			.compare(e1.getTime(), e2.getTime());

	/**
	 * Return true if the second argument is greater than the first argument
	 */
	private final static BiPredicate<Long, Long> IS_BEFORE = (t, u) -> u > t;
	private Graph graph;
	private final GammaTable<String, Long> gammaTable;

	private Set<Vertex> sourceVertices;
	private Long sourceTime;

	public TimeCentricReachability(Graph graph, Set<Vertex> sourceVertices, Long sourceTime, String gammaPrimePath)
			throws NotDirectoryException, FileNotFoundException {
		this.graph = graph;

		File gammaPrimeDir = new File(gammaPrimePath);
		if (!gammaPrimeDir.exists())
			gammaPrimeDir.mkdirs();
		this.gammaTable = new FixedSizedGammaTable<>(gammaPrimePath, LongGammaElement.class);

		synchronized (gammaTable) {
			for (Vertex sourceVertex : sourceVertices) {
				gammaTable.addSource(sourceVertex.getId(), new LongGammaElement(sourceTime));
			}
		}
	}

	public TimeCentricReachability(Graph graph, GammaTable<String, Long> gammaTable) {
		this.graph = graph;
		this.gammaTable = gammaTable;
	}

	public void compute(Set<Vertex> sourceVertices, Long baseTime, TemporalRelation tr, String edgeLabel,
			Boolean includeBaseTime) {
		Stream<EdgeEvent> events = this.getValidEdgeEvents(baseTime, edgeLabel, tr, includeBaseTime);

		synchronized (gammaTable) {
			events.forEach(event -> {
				Vertex outV = event.getVertex(Direction.OUT);
				Vertex inV = event.getVertex(Direction.IN);

				sourceVertices.forEach(sourceVertex -> {
					Map<String, Long> gamma = gammaTable.getGamma(sourceVertex.getId()).toMap(true);
					boolean isReachable = gamma.get(outV.getId()) != null;

					if (!isReachable)
						return;

					Long gammaValue = gamma.get(inV.getId());

					if (gammaValue == null || IS_AFTER.test(gammaValue, event.getTime()))
						gammaTable.set(sourceVertex.getId(), inV.getId(), new LongGammaElement(event.getTime()));
				});
			});
		}
	}

	private Stream<EdgeEvent> getValidEdgeEvents(Long baseTime, String edgeLabel, TemporalRelation tr,
			Boolean includeBaseTime) {
		Comparator<Event> eventComparator = setEventComparator(tr);

		return ((MChronoGraph) graph).getEdgeEvents().filter(edgeEvent -> edgeEvent.getLabel().equals(edgeLabel))
				.filter(edgeEvent -> {

					if (baseTime.equals(edgeEvent.getTime()) && includeBaseTime)
						return true;
					else if (tr.equals(TemporalRelation.isAfter))
						return IS_AFTER.test(edgeEvent.getTime(), baseTime);
					else if (tr.equals(TemporalRelation.isBefore))
						return IS_BEFORE.test(edgeEvent.getTime(), baseTime);
					return false;
				}).sorted(eventComparator);
	}

	private Comparator<Event> setEventComparator(TemporalRelation tr) {
		return switch (tr) {
		case isAfter -> IS_AFTER_EVENT_COMPARATOR;
		case isBefore -> IS_BEFORE_EVENT_COMPARATOR;
		default ->
			throw new UnsupportedOperationException("Only temporal relations isAfter and isBefore are supported");
		};
	}

	public GammaTable<String, Long> getGammaTable() {
		return this.gammaTable;
	}
}
