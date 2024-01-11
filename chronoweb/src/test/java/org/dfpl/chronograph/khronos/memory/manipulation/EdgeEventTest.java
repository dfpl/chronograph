package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.regex.Pattern;

import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.junit.*;

import com.tinkerpop.blueprints.*;

public class EdgeEventTest {
	Graph g;
	TemporalRelation tr;
	Vertex a;
	Vertex b;
	Edge ab;
	Long time;
	EdgeEvent expectedEvent;

	public static Pattern vPattern = Pattern.compile("^[^|_]+$");
	public static Pattern ePattern = Pattern.compile("^[^|_]+\\|[^|_]+\\|[^|_]+$");
	public static Pattern vtPattern = Pattern.compile("^[^|_]+_[0-9]+$");
	public static Pattern etPattern = Pattern.compile("^[^|_]+\\|[^|_]+\\|[^|_]+_[0-9]+$");
	
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
		b = g.addVertex("B");

		ab = g.addEdge(a, b, "likes");
	}

	@After
	public void tearDown() {
		g.removeEdge(ab);
		g.removeVertex(a);
		g.removeVertex(b);
		g = null;
	}
	
	@Test
	public void patternTest() {
		boolean b = etPattern.matcher("1|_3|5_3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, false);
		b = etPattern.matcher("|||||_3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, false);
		b = etPattern.matcher("a|b|c_3").matches();
		System.out.println("PATTERN " + b);
		assertEquals(b, true);
	}

	@Test
	public void foo() {
		for (long i = 0; i < 50; i++) {
			ab.addEvent(i * 3);
		}

		assertNull(ab.getEvent(1l));
		assertNotNull(ab.getEvent(3l));
		assertNull(ab.getEvent(2l, TemporalRelation.cotemporal));
		assertNotNull(ab.getEvent(3l, TemporalRelation.cotemporal));

		assertEquals(ab.getEvent(2l, TemporalRelation.isAfter), ab.getEvent(3l));
		assertEquals(ab.getEvent(3l, TemporalRelation.isAfter), ab.getEvent(6l));
		assertEquals(ab.getEvent(5l, TemporalRelation.isBefore), ab.getEvent(3l));
		assertEquals(ab.getEvent(3l, TemporalRelation.isBefore), ab.getEvent(0l));

		assertEquals(ab.getEvents().size(), 50);
		assertEquals(ab.getEvents(30l, TemporalRelation.cotemporal).size(), 1);
		assertEquals(ab.getEvents(31l, TemporalRelation.cotemporal).size(), 0);
		
		assertEquals(ab.getEvents(30l, TemporalRelation.isAfter).size(), 39);
		assertEquals(ab.getEvents(30l, TemporalRelation.isBefore).size(), 10);
	}

}
