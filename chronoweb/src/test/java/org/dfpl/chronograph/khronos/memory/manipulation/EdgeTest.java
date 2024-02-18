package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * The in-memory implementation of temporal graph database.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, Department of Software,
 *         Sejong University (slightly modify interface)
 */

public class EdgeTest {
	Graph g;
	Vertex a;
	Vertex b;
	Vertex c;
	Edge abLikes;
	Edge abLoves;
	Edge acLikes;
	Edge ccLikes;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}

	@Before
	public void setUp() {
		g = new MChronoGraph();
		a = g.addVertex("A");
		b = g.addVertex("B");
		c = g.addVertex("C");

		abLikes = g.addEdge(a, b, "likes");
		abLoves = g.addEdge(a, b, "loves");
		acLikes = g.addEdge(a, c, "likes");
		ccLikes = g.addEdge(c, c, "likes");
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testRemove() {
		acLikes.remove();

		assertThat(g.getVertices(), containsInAnyOrder(a, b, c));
		assertThat(g.getEdges(), containsInAnyOrder(abLikes, abLoves, ccLikes));
	}

	@Test
	public void testGetLabel() {
		assertEquals("likes", acLikes.getLabel());
	}

	@Test
	public void testGetId() {
		assertEquals("A|likes|C", acLikes.getId());
	}

	@Test
	public void testAddAndRemoveProperty() {
		// Set properties
		abLikes.setProperty("name", "J. B.");
		abLikes.setProperty("title", "Prof.");

		// Check all property keys
		assertThat(abLikes.getPropertyKeys(), containsInAnyOrder("name", "title"));

		// Check get property
		assertEquals("J. B.", abLikes.getProperty("name"));

		// Check removal of property
		abLikes.removeProperty("name");
		assertNull(abLikes.getProperty("name"));
	}

	@Test
	public void testGetVertex() {
		assertEquals(a, acLikes.getVertex(Direction.OUT));
		assertEquals(c, acLikes.getVertex(Direction.IN));
	}
}
