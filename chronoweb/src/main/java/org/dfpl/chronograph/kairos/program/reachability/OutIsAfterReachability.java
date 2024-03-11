package org.dfpl.chronograph.kairos.program.reachability;

import com.tinkerpop.blueprints.*;
import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.kairos.program.reachability.algorithms.TraversalReachability;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertexEvent;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    public void onInitialization(Set<Vertex> sources, Long startTime, String edgeLabel) {
        // TODO: Make time-centric engine
        this.edgeLabel = edgeLabel;
        synchronized (gammaTable) {
            for (Vertex s : sources) {
                gammaTable.addSource(s.getId(), new LongGammaElement(startTime));
            }

            if (graph instanceof MChronoGraph mg) {
                mg.getEdgeEvents().forEach(event -> {
                    System.out.println("\t\t" + event);
                    gammaTable.update(sources.parallelStream().map(Element::getId).collect(Collectors.toSet()),
                            event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
                            new LongGammaElement(event.getTime()), targetTest);
                });
            } else if (graph instanceof PChronoGraph pg) {
                pg.getEdgeEvents().forEach(event -> {
                    System.out.println("\t\t" + event);
                    gammaTable.update(sources.parallelStream().map(Element::getId).collect(Collectors.toSet()),
                            event.getVertex(Direction.OUT).getId(), sourceTest, event.getVertex(Direction.IN).getId(),
                            new LongGammaElement(event.getTime()), targetTest);
                });
            }
            gammaTable.print();
        }
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
