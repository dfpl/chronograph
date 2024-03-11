package org.dfpl.chronograph.kairos.program.reachability;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.kairos.program.reachability.algorithms.TimeCentricReachability;
import org.dfpl.chronograph.kairos.program.reachability.algorithms.TraversalReachability;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertexEvent;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OutIsAfterReachability extends AbstractKairosProgram<Long> {
    public static final TemporalRelation TR = TemporalRelation.isAfter;

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
    public void onInitialization(Set<Vertex> sources, Long startTime, String edgeLabel) {
        this.edgeLabel = edgeLabel;

        synchronized (this.gammaTable) {
            for (Vertex sourceVertex : sources) {
                gammaTable.addSource(sourceVertex.getId(), new LongGammaElement(startTime));
            }
        }
        new TimeCentricReachability(this.graph, this.gammaTable).compute(sources, startTime, TR, this.edgeLabel, true);
    }

    @Override
    public void onAddEdgeEvent(EdgeEvent addedEvent) {
        Vertex iPrime = addedEvent.getVertex(Direction.OUT);
        Vertex jPrime = addedEvent.getVertex(Direction.IN);

        // Step 1: Computing affected subgraph
        VertexEvent sourcePrime = new MChronoVertexEvent(jPrime, addedEvent.getTime());
        String gammaPrimePath = String.format("%s\\onAdd\\%s", ((FixedSizedGammaTable<String, Long>) this.gammaTable).getDirectory().getPath(), sourcePrime.getId());

        try {
            TraversalReachability algorithm = new TraversalReachability(this.graph, sourcePrime, gammaPrimePath);
            Set<Map.Entry<String, Long>> gammaPrime = algorithm.compute(TemporalRelation.isAfter, this.edgeLabel).toMap(true).entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toSet());

            // Step 2: Updating the Gamma Table
            for (Map.Entry<String, Long> entry : gammaPrime) {
                synchronized (gammaTable) {
                    gammaTable.update(iPrime.getId(), sourceTest, entry.getKey(), new LongGammaElement(entry.getValue()), targetTest);
                    gammaTable.print();
                }
            }
            algorithm.getGammaTable().clear();
        } catch (NotDirectoryException | FileNotFoundException e) {
            e.printStackTrace();
        }
        gammaTable.print();
    }

    @Override
    public void onRemoveEdgeEvent(EdgeEvent removedEdge) {
        // TODO Auto-generated method stub
        // Step 1: Compute immediate results using traversal approach
        Vertex iPrime = removedEdge.getVertex(Direction.IN);

        VertexEvent sourcePrime = new MChronoVertexEvent(iPrime, removedEdge.getTime());
        String gammaPrimePath = String.format("%s\\onRemove\\%s", ((FixedSizedGammaTable<String, Long>) this.gammaTable).getDirectory().getPath(), sourcePrime.getId());

        try {
            TraversalReachability algorithm = new TraversalReachability(this.graph, sourcePrime, gammaPrimePath);
            Set<String> gammaPrime = algorithm.computeInverse(TemporalRelation.isBefore, this.edgeLabel).toMap(true).entrySet().stream().filter(entry -> entry.getValue() != null)
                    .map(Map.Entry::getKey).collect(Collectors.toSet());

            // Step 2: Invalidate gamma values
            Set<Vertex> sourcesPrime = this.gammaTable.getSources().stream().filter(gammaPrime::contains).map(id -> new MChronoVertex(this.graph, id)).collect(Collectors.toSet());

            for (Vertex source : sourcesPrime) {
                Map<String, Long> gamma = this.gammaTable.getGamma(source.getId()).toMap(true);

                for (Map.Entry<String, Long> entry : gamma.entrySet()) {

                    if (gamma.get(entry.getKey()) != null && this.targetTest.test(gamma.get(entry.getKey()), removedEdge.getTime())) {
                        this.gammaTable.set(source.getId(), entry.getKey(), null);
                    }
                }

            }
            // Step 3: Re-compute Gamma Table using time-centric approach
            new TimeCentricReachability(this.graph, this.gammaTable).compute(sourcesPrime, removedEdge.getTime(), TR, this.edgeLabel, true);

            algorithm.getGammaTable().clear();
        } catch (NotDirectoryException | FileNotFoundException e) {
            e.printStackTrace();
        }


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
