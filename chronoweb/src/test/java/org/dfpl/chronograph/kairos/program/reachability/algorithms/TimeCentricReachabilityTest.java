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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.time.temporal.Temporal;
import java.util.Set;

public class TimeCentricReachabilityTest {
    public static final String TEMP_DIR = "D:\\chronoweb\\gamma_tables";
    public static final String EDGE_LABEL = "label";

    @Test
    public void testIsAfterAlgor() throws NotDirectoryException, FileNotFoundException {
        Graph g = new MChronoGraph();

        Vertex a = g.addVertex("A");
        VertexEvent sourceEvent = a.addEvent(1L);

        FixedSizedGammaTable<String, Long> gammaTable = new FixedSizedGammaTable<>(TEMP_DIR, LongGammaElement.class);
        String gammaPrimePath = String.format("%s\\%s_onAdd", gammaTable.getDirectory().getAbsolutePath(), sourceEvent.getTime());
        File subDirectory = new File(gammaPrimePath);
        if (!subDirectory.exists())
            subDirectory.mkdirs();

        gammaTable.addSource(a.getId(), new LongGammaElement(1L));

        TimeCentricReachability algorithm = new TimeCentricReachability(g, Set.of(a), sourceEvent.getTime(), gammaPrimePath);
        Assert.assertEquals("A -> {A=1}", algorithm.getGammaTable().toString());

        Vertex b = g.addVertex("B");
        Edge edge = g.addEdge(a, b, EDGE_LABEL);
        edge.addEvent(2);
        algorithm.compute(Set.of(a), 2L, TemporalRelation.isAfter, EDGE_LABEL, true);
        Assert.assertEquals("A -> {A=1, B=2}", algorithm.getGammaTable().toString());

        algorithm.getGammaTable().clear();
        algorithm = new TimeCentricReachability(g, Set.of(a), sourceEvent.getTime(), gammaPrimePath);
        algorithm.compute(Set.of(a), 2L, TemporalRelation.isAfter, EDGE_LABEL, true);

        Assert.assertEquals("A -> {A=1, B=2}", algorithm.getGammaTable().toString());

        algorithm.getGammaTable().clear();
    }
}
