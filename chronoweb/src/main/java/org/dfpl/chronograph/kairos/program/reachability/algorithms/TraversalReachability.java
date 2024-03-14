package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.LoopBundle;
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
    /**
     * Return true if the second argument is less than the first argument
     */
    private final static BiPredicate<Long, Long> IS_AFTER = (t, u) -> u < t;

    /**
     * Return true if the second argument is greater than the first argument
     */
    private final static BiPredicate<Long, Long> IS_BEFORE = (t, u) -> u > t;

    /**
     * Return true if the first and second arguments are equal
     */
    private final static BiPredicate<Long, Long> IS_COTEMPORAL = (t, u) -> Objects.equals(u, t);

    private final static Predicate<LoopBundle<Event>> IS_EMPTY = loopBundle -> {
        Collection<Event> traverserSet = loopBundle.getTraverserSet();

        return traverserSet != null && !traverserSet.isEmpty();
    };
    private GammaTable<String, Long> gammaTable;
    private Gamma<String, Long> gamma;

    public TraversalReachability(String gammaPrimePath)
            throws NotDirectoryException, FileNotFoundException {
        File gammaPrimeDir = new File(gammaPrimePath);
        if (!gammaPrimeDir.exists())
            gammaPrimeDir.mkdirs();
        gammaTable = new FixedSizedGammaTable<>(gammaPrimePath, LongGammaElement.class);
    }

    public GammaTable<String, Long> getGammaTable() {
        return this.gammaTable;
    }

    public Gamma<String, Long> getGamma() {
        return this.gamma;
    }

    public Gamma<String, Long> compute(Graph g, VertexEvent source, TemporalRelation tr, String edgeLabel) {
        Consumer<EdgeEvent> storeGamma = event -> {
            if (event == null)
                return;

            String inVertexId = event.getVertex(Direction.IN).getId();
            gamma.setElement(inVertexId, new LongGammaElement(event.getTime()));
        };

        Predicate<EdgeEvent> affectsGamma = edgeEvent -> {
            if (edgeEvent == null)
                return false;

            String inVertexId = edgeEvent.getVertex(Direction.IN).getId();
            Long currValue = gamma.getElement(inVertexId);
            boolean isReachable = currValue != null;

            return !isReachable || IS_AFTER.test(currValue, edgeEvent.getTime());
        };

        String sourceVertexId = ((Vertex) source.getElement()).getId();
        gammaTable.addSource(sourceVertexId, new LongGammaElement(source.getTime()));
        gamma = gammaTable.getGamma(sourceVertexId);

        TraversalEngine engine = new TraversalEngine(g, source, VertexEvent.class, false);
        engine = engine.as("s");
        engine = engine.outEe(tr, edgeLabel);
        engine = engine.filter(affectsGamma);
        engine = engine.sideEffect(storeGamma);
        engine = engine.inVe();
        engine = engine.loop("s", IS_EMPTY);
        engine.toList();

        return gamma;
    }

    public Gamma<String, Long> computeInverse(Graph g, VertexEvent source, TemporalRelation tr, String edgeLabel) {

        Predicate<EdgeEvent> affectsGamma = edgeEvent -> {
            if (edgeEvent == null)
                return false;

            String outVertexId = edgeEvent.getVertex(Direction.OUT).getId();
            Long currValue = gamma.getElement(outVertexId);
            boolean isReachable = currValue != null;

            return !isReachable || IS_AFTER.test(currValue, edgeEvent.getTime());
        };

        Consumer<EdgeEvent> storeGamma = event -> {
            String outVertexId = event.getVertex(Direction.OUT).getId();
            gamma.setElement(outVertexId, new LongGammaElement(event.getTime()));
        };

        String sourceVertexId = ((Vertex) source.getElement()).getId();
        gammaTable.addSource(sourceVertexId, new LongGammaElement(source.getTime()));
        gamma = gammaTable.getGamma(sourceVertexId);

        TraversalEngine engine = new TraversalEngine(g, source, VertexEvent.class, false);
        engine = engine.as("s");
        engine = engine.inEe(tr, edgeLabel);
        engine = engine.filter(affectsGamma);
        engine = engine.sideEffect(storeGamma);
        engine = engine.outVe();
        engine = engine.loop("s", IS_EMPTY);
        engine.toList();

        return gamma;
    }
}
