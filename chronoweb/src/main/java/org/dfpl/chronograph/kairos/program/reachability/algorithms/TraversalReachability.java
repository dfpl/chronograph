package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.LoopBundle;
import io.vertx.core.json.JsonObject;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.traversal.TraversalEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * {@code TraversalReachability} implements the temporal reachability algorithm
 * using the TraversalEngine
 */
public class TraversalReachability {
    private GammaTable<String, Long> gammaTable;
    private Gamma<String, Long> gamma;
    TraversalEngine engine;
    private long computationTime = 0;
    JsonObject configuration;

    /**
     * Return true if the second argument is less than the first argument
     */
    private final BiPredicate<Long, Long> isAfter = (t, u) -> u < t;

    /**
     * Return true if the second argument is greater than the first argument
     */
    private final BiPredicate<Long, Long> isBefore = (t, u) -> u > t;

    /**
     * Return true if the first and second arguments are equal
     */
    private final BiPredicate<Long, Long> isCotemporal = (t, u) -> Objects.equals(u, t);

    private final Predicate<LoopBundle<Event>> exitIfEmpty = loopBundle -> {
        Collection<Event> traverserSet = loopBundle.getTraverserSet();

        return traverserSet != null && !traverserSet.isEmpty();
    };

    public TraversalReachability(Graph g, VertexEvent source, String gammaPrimePath)
            throws NotDirectoryException, FileNotFoundException {
        File gammaPrimeDir = new File(gammaPrimePath);
        if (!gammaPrimeDir.exists())
            gammaPrimeDir.mkdirs();
        gammaTable = new FixedSizedGammaTable<>(gammaPrimePath, LongGammaElement.class);
        String sourceVertexId = ((Vertex) source.getElement()).getId();
        gammaTable.addSource(sourceVertexId, new LongGammaElement(source.getTime()));
        gamma = gammaTable.getGamma(sourceVertexId);
        engine = new TraversalEngine(g, source, VertexEvent.class, false);
    }

    public GammaTable<String, Long> getGammaTable() {
        return this.gammaTable;
    }

    public Gamma<String, Long> getGamma() {
        return this.gamma;
    }

    public Gamma<String, Long> compute(TemporalRelation tr, String edgeLabel) {
        List<String> edgeLabels = List.of(edgeLabel);
        Function<VertexEvent, Set<EdgeEvent>> outEdgeEvents = vertexEvent -> {
            Set<EdgeEvent> events = new HashSet<>();
            Vertex outVertex = (Vertex) vertexEvent.getElement();
            if (gamma.getElement(outVertex.getId()) == null)
                return events;

            for (Edge edge : outVertex.getEdges(Direction.OUT, edgeLabels)) {
                String inVertexId = edge.getVertex(Direction.IN).getId();
                boolean isReachable = gamma.getElement(inVertexId) != null;

                if (isReachable && (isBefore.test(gamma.getElement(inVertexId), vertexEvent.getTime()) || isCotemporal.test(gamma.getElement(inVertexId), vertexEvent.getTime())))
                    continue;

                EdgeEvent event = edge.getEvent(vertexEvent.getTime(), tr);
                if (event == null)
                    continue;

                if (!isReachable || isAfter.test(gamma.getElement(inVertexId), event.getTime()))
                    events.add(event);
            }

            return events;
        };

        Consumer<EdgeEvent> storeGamma = event -> {
            String inVertexId = event.getVertex(Direction.IN).getId();
            boolean isReachable = gamma.getElement(inVertexId) != null;

            if (!isReachable || isAfter.test(gamma.getElement(inVertexId), event.getTime()))
                gamma.setElement(inVertexId, new LongGammaElement(event.getTime()));
        };

        engine = engine.as("s");
        engine = engine.flatMap(outEdgeEvents, EdgeEvent.class);
        engine = engine.sideEffect(storeGamma);
        engine = engine.inVe();
        engine = engine.loop("s", exitIfEmpty);
        engine.toList();

        return gamma;
    }

    public Gamma<String, Long> computeInverse(TemporalRelation tr, String edgeLabel) {
        List<String> edgeLabels = List.of(edgeLabel);

        Function<VertexEvent, Set<EdgeEvent>> inEdgeEvents = vertexEvent -> {
            Set<EdgeEvent> events = new HashSet<>();
            Vertex inVertex = (Vertex) vertexEvent.getElement();
            if (gamma.getElement(inVertex.getId()) == null)
                return events;

            for (Edge edge : inVertex.getEdges(Direction.IN, edgeLabels)) {
                String outVertexId = edge.getVertex(Direction.OUT).getId();
                boolean isReachable = gamma.getElement(outVertexId) != null;

                if (isReachable &&
                        (isAfter.test(gamma.getElement(outVertexId), vertexEvent.getTime()) || isCotemporal.test(gamma.getElement(outVertexId), vertexEvent.getTime())))
                    continue;

                EdgeEvent event = edge.getEvent(vertexEvent.getTime(), tr);
                if (event == null)
                    continue;

                if (!isReachable || isAfter.test(gamma.getElement(inVertex.getId()), event.getTime()))
                    events.add(event);
            }
            return events;
        };

        Consumer<EdgeEvent> storeGamma = event -> {
            String outVertexId = event.getVertex(Direction.OUT).getId();
            boolean isReachable = gamma.getElement(outVertexId) != null;

            if (!isReachable || isAfter.test(gamma.getElement(outVertexId), event.getTime()))
                gamma.setElement(outVertexId, new LongGammaElement(event.getTime()));
        };

        engine = engine.as("s");
        engine = engine.flatMap(inEdgeEvents, EdgeEvent.class);
        engine = engine.sideEffect(storeGamma);
        engine = engine.outVe();
        engine = engine.loop("s", exitIfEmpty);
        engine.toList();

        return gamma;
    }
}
