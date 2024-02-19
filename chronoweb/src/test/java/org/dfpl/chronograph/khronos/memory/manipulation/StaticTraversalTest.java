package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.junit.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class StaticTraversalTest {
	Graph g;
	Vertex v1;
	Vertex v2;
	Vertex v3;
	Vertex v4;
	Vertex v5;
	Vertex v6;
	Vertex v7;
	Vertex v8;
	Vertex v9;
	Vertex v10;
	Vertex v11;
	Vertex v12;
	Vertex v13;

	Edge e12;
	Edge e13;
	Edge e24;
	Edge e25;
	Edge e36;
	Edge e37;
	Edge e108;
	Edge e118;
	Edge e129;
	Edge e139;
	Edge e81;
	Edge e91;

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
		v1.setProperty("isOdd", true);
		v2 = g.addVertex("2");
		v2.setProperty("isOdd", false);
		v3 = g.addVertex("3");
		v3.setProperty("isOdd", true);
		v4 = g.addVertex("4");
		v4.setProperty("isOdd", false);
		v5 = g.addVertex("5");
		v5.setProperty("isOdd", true);
		v6 = g.addVertex("6");
		v6.setProperty("isOdd", false);
		v7 = g.addVertex("7");
		v7.setProperty("isOdd", true);
		v8 = g.addVertex("8");
		v8.setProperty("isOdd", false);
		v9 = g.addVertex("9");
		v9.setProperty("isOdd", true);
		v10 = g.addVertex("10");
		v10.setProperty("isOdd", false);
		v11 = g.addVertex("11");
		v11.setProperty("isOdd", true);
		v12 = g.addVertex("12");
		v12.setProperty("isOdd", false);
		v13 = g.addVertex("13");
		v13.setProperty("isOdd", true);

		e12 = g.addEdge(v1, v2, label);
		e13 = g.addEdge(v1, v3, label);
		e24 = g.addEdge(v2, v4, label);
		e25 = g.addEdge(v2, v5, label);
		e36 = g.addEdge(v3, v6, label);
		e37 = g.addEdge(v3, v7, label);
		e108 = g.addEdge(v10, v8, label);
		e118 = g.addEdge(v11, v8, label);
		e129 = g.addEdge(v12, v9, label);
		e139 = g.addEdge(v13, v9, label);
		e81 = g.addEdge(v8, v1, label);
		e91 = g.addEdge(v9, v1, label);

	}

	@After
	public void tearDown() {
	}

	@Test
	public void test() {
		Comparator<Vertex> vComparator = (o1, o2) -> o1.toString().compareTo(o2.toString());

		String s = v1.getEdges(Direction.OUT, List.of(label)).stream().map(e -> e.getVertex(Direction.IN))
				.sorted(vComparator).toList().toString();
		assertEquals(s, "[2, 3]");

		s = v1.getEdges(Direction.OUT, List.of(label)).stream().map(e -> e.getVertex(Direction.IN))
				.flatMap(v -> v.getVertices(Direction.OUT, List.of(label)).parallelStream()).sorted(vComparator)
				.toList().toString();
		assertEquals(s, "[4, 5, 6, 7]");

		s = v1.getEdges(Direction.IN, List.of(label)).stream().map(e -> e.getVertex(Direction.OUT)).sorted(vComparator)
				.toList().toString();
		assertEquals(s, "[8, 9]");
		
		s = v1.getEdges(Direction.IN, List.of(label)).stream().map(e -> e.getVertex(Direction.OUT))
				.flatMap(v -> v.getVertices(Direction.IN, List.of(label)).parallelStream()).sorted(vComparator)
				.toList().toString();
		assertEquals(s, "[10, 11, 12, 13]");
	}
}
