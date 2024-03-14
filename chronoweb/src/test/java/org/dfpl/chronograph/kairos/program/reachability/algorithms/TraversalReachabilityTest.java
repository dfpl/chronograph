package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertexEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;

public class TraversalReachabilityTest {
    public static final String TEMP_DIR = "D:\\chronoweb\\gamma_tables";
    public static final String EDGE_LABEL = "label";


    @Test
    public void testIsAfterAlgo() throws NotDirectoryException, FileNotFoundException {
        Graph g = new MChronoGraph();


        Vertex a = new MChronoVertex(g, "A");
        VertexEvent sourceEvent = new MChronoVertexEvent(a, 1L);
        FixedSizedGammaTable<String, Long> gammaTable = new FixedSizedGammaTable<>(TEMP_DIR, LongGammaElement.class);
        String onAddPath = String.format("%s\\%s_onAdd", gammaTable.getDirectory().getAbsolutePath(), sourceEvent.getTime());

        TraversalReachability algorithm = new TraversalReachability(onAddPath);

        Vertex b = g.addVertex("B");
        Edge edge = g.addEdge(a, b, EDGE_LABEL);
        edge.addEvent(2);

        algorithm.compute(g, sourceEvent, TemporalRelation.isAfter, EDGE_LABEL);

        Assert.assertEquals("A -> {A=1, B=2}", algorithm.getGammaTable().toString());

        Vertex c = g.addVertex("C");
        edge = g.addEdge(a, c, "label");
        edge.addEvent(0);

        algorithm.getGammaTable().clear();
        algorithm = new TraversalReachability(onAddPath);
        algorithm.compute(g, sourceEvent, TemporalRelation.isAfter, EDGE_LABEL);

        Assert.assertEquals("A -> {A=1, B=2}", algorithm.getGammaTable().toString());

        algorithm.getGammaTable().clear();
    }

    @Test
    public void testisBeforeAlgo() throws NotDirectoryException, FileNotFoundException {
        Graph g = new MChronoGraph();


        Vertex c = new MChronoVertex(g, "C");
        VertexEvent sourceEvent = new MChronoVertexEvent(c, 3L);
        FixedSizedGammaTable<String, Long> gammaTable = new FixedSizedGammaTable<>(TEMP_DIR, LongGammaElement.class);

        String gammaPrimePath = String.format("%s\\%s_onAdd", gammaTable.getDirectory().getAbsolutePath(), sourceEvent.getTime());
        File subDirectory = new File(gammaPrimePath);
        if (!subDirectory.exists())
            subDirectory.mkdirs();

        TraversalReachability algorithm = new TraversalReachability(gammaPrimePath);

        Vertex b = g.addVertex("B");
        Edge edge = g.addEdge(b, c, "label");
        edge.addEvent(2);
        algorithm.computeInverse(g, sourceEvent, TemporalRelation.isBefore, EDGE_LABEL);

        Assert.assertEquals("C -> {B=2, C=3}", algorithm.getGammaTable().toString());
        algorithm.getGammaTable().clear();

        algorithm = new TraversalReachability(gammaPrimePath);
        algorithm.computeInverse(g, sourceEvent, TemporalRelation.isBefore, EDGE_LABEL);
        algorithm.getGammaTable().clear();

        Vertex a = g.addVertex("A");
        edge = g.addEdge(a, b, EDGE_LABEL);
        edge.addEvent(1);
        algorithm = new TraversalReachability(gammaPrimePath);
        algorithm.computeInverse(g, sourceEvent, TemporalRelation.isBefore, EDGE_LABEL);
        Assert.assertEquals("C -> {A=1, B=2, C=3}", algorithm.getGammaTable().toString());

        algorithm.getGammaTable().clear();
    }
}
