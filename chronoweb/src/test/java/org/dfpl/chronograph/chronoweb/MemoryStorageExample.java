package org.dfpl.chronograph.chronoweb;

import java.util.HashMap;
import java.util.HashSet;

import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.junit.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class MemoryStorageExample {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testMemoryStorage() {
		MChronoGraph g = new MChronoGraph();
		Vertex jack = g.addVertex("Jack");
		Vertex sarah = g.addVertex("Sarah");
		Vertex david = g.addVertex("David");
		Vertex mary = g.addVertex("Mary");
		Vertex emma = g.addVertex("Emma");

		Edge js = g.addEdge(jack, sarah, "contact");
		Edge sj = g.addEdge(sarah, jack, "contact");

		Edge jm = g.addEdge(jack, mary, "contact");
		Edge mj = g.addEdge(mary, jack, "contact");

		Edge sd = g.addEdge(sarah, david, "contact");
		Edge ds = g.addEdge(david, sarah, "contact");

		Edge sm = g.addEdge(sarah, mary, "contact");
		Edge ms = g.addEdge(mary, sarah, "contact");

		Edge dm = g.addEdge(david, mary, "contact");
		Edge md = g.addEdge(mary, david, "contact");

		Edge de = g.addEdge(david, emma, "contact");
		Edge ed = g.addEdge(emma, david, "contact");

		jm.addEvent(1);
		mj.addEvent(1);

		sd.addEvent(2);
		ds.addEvent(2);

		js.addEvent(3);
		sj.addEvent(3);

		sm.addEvent(4);
		ms.addEvent(4);

		dm.addEvent(5);
		md.addEvent(5);

		de.addEvent(6);
		ed.addEvent(6);

		sd.addEvent(7);
		ds.addEvent(7);

		HashMap<Vertex, Long> gamma = new HashMap<Vertex, Long>();
		gamma.put(jack, 0l);
		HashSet<VertexEvent> traversers = new HashSet<VertexEvent>();
		traversers.add(jack.getEvent(0));
		while (true) {

			HashSet<VertexEvent> newTraversers = new HashSet<VertexEvent>();
			for (VertexEvent t : traversers) {
				for (VertexEvent ot : t.getVertexEvents(Direction.OUT, TemporalRelation.isAfter, "contact")) {
					if (ot == null)
						continue;
					Vertex o = (Vertex) ot.getElement();
					if (gamma.containsKey(o) && (gamma.get(o) > ot.getTime())) {

						gamma.put(o, ot.getTime());
						newTraversers.add(ot);
					} else if (!gamma.containsKey(o)) {
						gamma.put(o, ot.getTime());
						newTraversers.add(ot);
					}
				}
			}
			if (newTraversers.isEmpty())
				break;
			else
				traversers = newTraversers;
		}
		System.out.println("Reachable from Jack: " + gamma);
	}
}
