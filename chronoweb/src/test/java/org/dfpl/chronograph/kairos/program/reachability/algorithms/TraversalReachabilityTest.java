package org.dfpl.chronograph.kairos.program.reachability.algorithms;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.file.LongGammaElement;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertex;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoVertexEvent;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;

public class TraversalReachabilityTest {
    public static final String TEMP_DIR = "D:\\chronoweb\\gamma_tables";


    @Test
    public void testAlgo() throws NotDirectoryException, FileNotFoundException {
        Graph g = new MChronoGraph();


        Vertex sourceVertex = new MChronoVertex(g, "A");
        VertexEvent sourceEvent = new MChronoVertexEvent(sourceVertex, 1L);
        FixedSizedGammaTable<String, Long> gammaTable = new FixedSizedGammaTable<>(TEMP_DIR, LongGammaElement.class);

        String gammaPrimePath = String.format("%s\\%s_onAdd", gammaTable.getDirectory().getAbsolutePath(), sourceEvent.getTime());
        File subDirectory = new File(gammaPrimePath);
        if (!subDirectory.exists())
            subDirectory.mkdirs();

        TraversalReachability algorithm = new TraversalReachability(g, sourceEvent, gammaPrimePath);

        algorithm.getGammaTable().print();
        Vertex a = sourceVertex;
        Vertex b = g.addVertex("B");
        Edge edge = g.addEdge(a,b, "label");
        edge.addEvent(2);

        algorithm.compute(TemporalRelation.isAfter, "label");

        algorithm.getGammaTable().print();
    }
}
