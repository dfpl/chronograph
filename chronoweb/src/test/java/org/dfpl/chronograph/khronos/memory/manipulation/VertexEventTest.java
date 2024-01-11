package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.regex.Pattern;

import org.dfpl.chronograph.common.TemporalRelation;
import org.junit.*;

import com.tinkerpop.blueprints.*;

public class VertexEventTest {
	Graph g;
	TemporalRelation tr;
	Vertex a;
	Long time;
	VertexEvent expectedEvent;

	public static Pattern vPattern = Pattern.compile("^[^|_]+$");
	public static Pattern ePattern = Pattern.compile("^[^|_]+|[^|_]+|[^|_]+$");
	public static Pattern vtPattern = Pattern.compile("^[^|_]+_[0-9]+$");
	public static Pattern etPattern = Pattern.compile("^[^|_]+|[^|_]+|[^|_]+_[0-9]+$");

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
	public void patternTest() {
		boolean b = vtPattern.matcher("|_3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, false);
		b = vtPattern.matcher("__3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, false);
		b = vtPattern.matcher("a_3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, true);
	}

	@Test
	public void base() {
		for (long i = 0; i < 50; i++) {
			a.addEvent(i * 3);
		}

		assertNotNull(a.getEvent(1l));
		assertNotNull(a.getEvent(3l));

		assertNull(a.getEvent(2l, TemporalRelation.cotemporal));
		assertNotNull(a.getEvent(3l, TemporalRelation.cotemporal));

		assertEquals(a.getEvent(2l, TemporalRelation.isAfter), a.getEvent(3l));
		assertEquals(a.getEvent(3l, TemporalRelation.isAfter), a.getEvent(6l));

		assertEquals(a.getEvent(4l, TemporalRelation.isBefore), a.getEvent(3l));
		assertEquals(a.getEvent(3l, TemporalRelation.isBefore), a.getEvent(0l));

//		assertEquals(ab.getEvents().size(), 50);
//		assertEquals(ab.getEvents(30l, TemporalRelation.cotemporal).size(), 1);
//		assertEquals(ab.getEvents(31l, TemporalRelation.cotemporal).size(), 0);
//		
//		assertEquals(ab.getEvents(30l, TemporalRelation.isAfter).size(), 39);
//		assertEquals(ab.getEvents(30l, TemporalRelation.isBefore).size(), 10);
	}

	@Test
	public void getEvents() {
		Vertex c = g.addVertex("c");
		Vertex d = g.addVertex("d");
		Vertex e = g.addVertex("e");
		Edge cd = g.addEdge(c, d, "label");
		Edge de = g.addEdge(d, e, "label");
		cd.addEvent(3l);
		cd.addEvent(6l);
		cd.addEvent(9l);
		de.addEvent(5l);
		de.addEvent(8l);
		de.addEvent(9l);
		System.out.println(d.getEvents(0l, TemporalRelation.isAfter, false, false));
		assertEquals(d.getEvents(0l, TemporalRelation.isAfter, false, false).size(), 0);
		System.out.println(d.getEvents(0l, TemporalRelation.isAfter, true, false));
		assertEquals(d.getEvents(0l, TemporalRelation.isAfter, true, false).size(), 3);
		System.out.println(d.getEvents(0l, TemporalRelation.isAfter, false, true));
		assertEquals(d.getEvents(0l, TemporalRelation.isAfter, false, true).size(), 3);
		System.out.println(d.getEvents(0l, TemporalRelation.isAfter, true, true));
		assertEquals(d.getEvents(0l, TemporalRelation.isAfter, true, true).size(), 5);
		System.out.println();

		System.out.println(d.getEvents(15l, TemporalRelation.isBefore, false, false));
		assertEquals(d.getEvents(15l, TemporalRelation.isBefore, false, false).size(), 0);
		System.out.println(d.getEvents(15l, TemporalRelation.isBefore, true, false));
		assertEquals(d.getEvents(15l, TemporalRelation.isBefore, true, false).size(), 3);
		System.out.println(d.getEvents(15l, TemporalRelation.isBefore, false, true));
		assertEquals(d.getEvents(15l, TemporalRelation.isBefore, false, true).size(), 3);
		System.out.println(d.getEvents(15l, TemporalRelation.isBefore, true, true));
		assertEquals(d.getEvents(15l, TemporalRelation.isBefore, true, true).size(), 5);
		System.out.println();

		System.out.println(d.getEvents(5l, TemporalRelation.isAfter, false, false));
		assertEquals(d.getEvents(5l, TemporalRelation.isAfter, false, false).size(), 0);
		System.out.println(d.getEvents(5l, TemporalRelation.isAfter, true, false));
		assertEquals(d.getEvents(5l, TemporalRelation.isAfter, true, false).size(), 2);
		System.out.println(d.getEvents(5l, TemporalRelation.isAfter, false, true));
		assertEquals(d.getEvents(5l, TemporalRelation.isAfter, false, true).size(), 2);
		System.out.println(d.getEvents(5l, TemporalRelation.isAfter, true, true));
		assertEquals(d.getEvents(5l, TemporalRelation.isAfter, true, true).size(), 3);
		System.out.println();

		System.out.println(d.getEvents(5l, TemporalRelation.isBefore, false, false));
		assertEquals(d.getEvents(5l, TemporalRelation.isBefore, false, false).size(), 0);
		System.out.println(d.getEvents(5l, TemporalRelation.isBefore, true, false));
		assertEquals(d.getEvents(5l, TemporalRelation.isBefore, true, false).size(), 0);
		System.out.println(d.getEvents(5l, TemporalRelation.isBefore, false, true));
		assertEquals(d.getEvents(5l, TemporalRelation.isBefore, false, true).size(), 1);
		System.out.println(d.getEvents(5l, TemporalRelation.isBefore, true, true));
		assertEquals(d.getEvents(5l, TemporalRelation.isBefore, true, true).size(), 1);
		System.out.println();

		System.out.println(d.getEvents(false, false));
		assertEquals(d.getEvents(false, false).size(), 0);
		System.out.println(d.getEvents(true, false));
		assertEquals(d.getEvents(true, false).size(), 3);
		System.out.println(d.getEvents(false, true));
		assertEquals(d.getEvents(false, true).size(), 3);
		System.out.println(d.getEvents(true, true));
		assertEquals(d.getEvents(true, true).size(), 5);
		System.out.println();
	}
}
