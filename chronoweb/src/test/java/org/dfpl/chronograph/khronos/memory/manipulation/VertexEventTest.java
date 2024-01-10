package org.dfpl.chronograph.khronos.memory.manipulation;

import org.dfpl.chronograph.common.TemporalRelation;
import org.junit.*;

import com.tinkerpop.blueprints.*;

public class VertexEventTest {
	Graph g;
	TemporalRelation tr;
	Vertex a;
	Long time;
	VertexEvent expectedEvent;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		g = new ChronoGraph();
		a = g.addVertex("A");
	}

	@After
	public void tearDown() {
		g.removeVertex(a);
		g = null;
	}

	@Test
	public void foo() {
		for (long i = 0; i < 50; i++) {
			a.addEvent(i * 3);
		}

		// TODO
	}
}
