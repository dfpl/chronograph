package org.dfpl.chronograph.khronos.memory.manipulation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.junit.*;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class VertexTest {
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
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
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
	public void testGetId() {
		assertEquals("A", a.getId());
	}

	@Test
	public void testGetEdges_WithDirectionIn() {
		assertThat(b.getEdges(Direction.IN, List.of("likes")), containsInAnyOrder(abLikes));
	}

	@Test
	public void testGetEdges_WithDirectionOut() {
		assertThat(a.getEdges(Direction.OUT, List.of("likes")), containsInAnyOrder(abLikes, acLikes));
		assertThat(a.getEdges(Direction.OUT, List.of("likes", "loves")), containsInAnyOrder(abLikes, acLikes, abLoves));
	}

	@Test
	public void testGetVertices() {
		assertThat(a.getVertices(Direction.OUT, null), containsInAnyOrder(b, c));
		assertThat(a.getVertices(Direction.OUT, List.of("likes", "loves")), containsInAnyOrder(b, c));
	}

	@Test
	public void testRemoveVertex() {
		a.remove();

		assertThat(g.getVertices(), containsInAnyOrder(b, c));
	}

	@Test
	public void testAddAndRemoveProperty() {
		assertEquals(0, a.getPropertyKeys().size());

		// Add properties
		a.setProperty("name", "J. B.");
		a.setProperty("title", "Prof.");

		// Assert added properties
		assertEquals("J. B.", a.getProperty("name").toString());
		assertEquals("Prof.", a.getProperty("title").toString());
		assertEquals(2, a.getPropertyKeys().size());

		// Assert removal of properties
		a.removeProperty("name");
		assertNull(a.getProperty("name"));
		assertEquals(1, a.getPropertyKeys().size());
	}
}
