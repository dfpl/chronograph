package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.LoopBundle;

import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.traversal.TraversalEngine;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;

/**
 * {@code TraversalReachability} implements the temporal reachability algorithm
 * using the TraversalEngine
 */
public class TraversalReachability {
    private GammaTable<String, Long> gammaTable;
    private Gamma<String, Long> gamma;
    TraversalEngine engine;
    private long computationTime = 0;

    /**
     * Return true if the second argument is less than the first argument
     */
    private final BiPredicate<Long, Long> isAfter = (t, u) -> u < t;

    public TraversalReachability(Graph g, VertexEvent source, String programName)
            throws NotDirectoryException, FileNotFoundException {
        String subDirectoryName = Server.gammaBaseDirectory + "\\" + source.getTime() + "_" + programName;
        File subDirectory = new File(subDirectoryName);
        if (!subDirectory.exists())
            subDirectory.mkdirs();
        FixedSizedGammaTable<String, Long> gammaTable = new FixedSizedGammaTable<String, Long>(subDirectoryName,
                LongGammaElement.class);
        String sourceVertexId = ((Vertex) source.getElement()).getId();
        gammaTable.addSource(sourceVertexId, new LongGammaElement(source.getTime()));
        gamma = gammaTable.getGamma(sourceVertexId);

        engine = new TraversalEngine(g, source, VertexEvent.class, false);
    }

    public void compute(TemporalRelation tr, String edgeLabel) {
        List<String> edgeLabels = List.of(edgeLabel);
        Function<VertexEvent, Set<EdgeEvent>> outEdgeEvents = vertexEvent -> {
            Set<EdgeEvent> events = new HashSet<>();
            Vertex outVertex = (Vertex) vertexEvent.getElement();
            if (gamma.getElement(outVertex.getId()) == null)
                return events;

            for (Edge edge : outVertex.getEdges(Direction.OUT, edgeLabels)) {
                Vertex inVertex = edge.getVertex(Direction.IN);
                boolean isReachable = gamma.getElement(inVertex.getId()) != null;

                if (isReachable && gamma.getElement(inVertex.getId()) <= 0)
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
            Vertex inVertex = event.getVertex(Direction.IN);
            boolean isReachable = gamma.getElement(inVertex.getId()) != null;

            if (!isReachable || isAfter.test(gamma.getElement(inVertex.getId()), event.getTime()))
                gamma.setElement(inVertex.getId(), new LongGammaElement(event.getTime()));
        };
        Predicate<LoopBundle<Event>> exitIfEmpty = loopBundle -> {
            Collection<Event> traverserSet = loopBundle.getTraverserSet();

            return traverserSet != null && !traverserSet.isEmpty();
        };

        engine = engine.as("s");
        engine = engine.flatMap(outEdgeEvents, EdgeEvent.class);
        engine = engine.sideEffect(storeGamma);
        engine = engine.inVe();
        engine = engine.loop("s", exitIfEmpty);
        engine.toList();
    }

}
