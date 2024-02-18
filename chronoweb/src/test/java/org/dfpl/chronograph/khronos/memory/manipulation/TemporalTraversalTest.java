package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.junit.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TemporalTraversalTest {
	Graph g;
	Vertex v1;
	Vertex v2;
	Vertex v3;
	Vertex v4;
	Vertex v5;

	Edge e12;
	Edge e14;
	Edge e23;
	Edge e42;
	Edge e43;
	Edge e35;

	EdgeEvent e12t10;
	EdgeEvent e14t5;
	EdgeEvent e23t8;
	EdgeEvent e23t16;
	EdgeEvent e42t12;
	EdgeEvent e43t13;
	EdgeEvent e35t14;

	String label = "l";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		g = new MChronoGraph();

		v1 = g.addVertex("1");
		v2 = g.addVertex("2");
		v3 = g.addVertex("3");
		v4 = g.addVertex("4");
		v5 = g.addVertex("5");

		e12 = g.addEdge(v1, v2, label);
		e14 = g.addEdge(v1, v4, label);
		e23 = g.addEdge(v2, v3, label);
		e42 = g.addEdge(v4, v2, label);
		e43 = g.addEdge(v4, v3, label);
		e35 = g.addEdge(v3, v5, label);

		e12t10 = e12.addEvent(10l);
		e14t5 = e14.addEvent(5l);
		e23t8 = e23.addEvent(8l);
		e23t16 = e23.addEvent(16l);
		e42t12 = e42.addEvent(12l);
		e43t13 = e43.addEvent(13l);
		e35t14 = e35.addEvent(14l);

	}

	@After
	public void tearDown() {
	}

	@Test
	public void testOutE() {
		Comparator<VertexEvent> veComp = (ve1, ve2) -> ve1.toString().compareTo(ve2.toString());
		VertexEvent v1t3 = g.getVertex("1").getEvent(3l);
		assertEquals(v1t3.toString(), "1_3");

		HashSet<VertexEvent> gamma = new HashSet<VertexEvent>();
		gamma.add(v1t3);

		Collection<VertexEvent> cc = List.of(v1t3);

		while (true) {
			int preSize = gamma.size();

			cc = cc.stream().flatMap(ve -> ve.getEdgeEvents(Direction.OUT, TemporalRelation.isAfter, label).stream())
					.filter(ee -> {
						System.out.println(ee);
						if (ee != null)
							return true;
						return false;
					}).map(ee -> ee.getVertexEvent(Direction.IN)).toList();
			gamma.addAll(cc);
			int aftSize = gamma.size();
			System.out.println(preSize + " -> " + aftSize);
			if (preSize == aftSize)
				break;
		}

		String s = gamma.stream().sorted(veComp).toList().toString();

		assertEquals(s.toString(), "[1_3, 2_10, 2_12, 3_13, 3_16, 4_5, 5_14]");
	}
}
