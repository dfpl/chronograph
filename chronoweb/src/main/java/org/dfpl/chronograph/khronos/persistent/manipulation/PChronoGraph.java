package org.dfpl.chronograph.khronos.persistent.manipulation;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.tinkerpop.blueprints.*;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * The persistent implementation of temporal graph database with MongoDB.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
@SuppressWarnings("unused")
public class PChronoGraph implements Graph {

	private MongoClient client;
	private MongoDatabase database;
	MongoCollection<Document> vertices;
	MongoCollection<Document> edges;
	MongoCollection<Document> vertexEvents;
	MongoCollection<Document> edgeEvents;

	private EventBus eventBus;

	public PChronoGraph(String connectionString, String databaseName) {
		client = MongoClients.create(connectionString);
		database = client.getDatabase(databaseName);
		vertices = database.getCollection("vertex", Document.class);
		edges = database.getCollection("edge", Document.class);
		vertexEvents = database.getCollection("vertexEvent", Document.class);
		edgeEvents = database.getCollection("edgeEvents", Document.class);
	}

	public PChronoGraph(String connectionString, String databaseName, EventBus eventBus) {
		client = MongoClients.create(connectionString);
		database = client.getDatabase(databaseName);
		vertices = database.getCollection("vertex", Document.class);
		edges = database.getCollection("edge", Document.class);
		vertexEvents = database.getCollection("vertexEvent", Document.class);
		edgeEvents = database.getCollection("edgeEvents", Document.class);
		this.eventBus = eventBus;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * Create a new vertex, add it to the graph, and return the newly created
	 * vertex. The provided object identifier is a recommendation for the identifier
	 * to use. It is not required that the implementation use this identifier.
	 *
	 * @param id the recommended object identifier
	 * @return the newly created vertex
	 */
	@Override
	public Vertex addVertex(String id) {
		if (id.contains("|"))
			throw new IllegalArgumentException("Vertex ID cannot contains '|'");
		Document v = vertices.find(new Document("_id", id)).first();
		Vertex vertex = new PChronoVertex(this, id, vertices);
		if (v != null) {
			return vertex;
		} else {
			vertices.insertOne(new Document("_id", id));
			if (eventBus != null)
				eventBus.send("addVertex", id);
			return vertex;
		}
	}

	/**
	 * public JsonObject toJsonObject(boolean includeProperties) { JsonObject object
	 * = new JsonObject(); object.put("_id", id); if (includeProperties)
	 * object.put("properties", new JsonObject(properties)); return object; }
	 */

	/**
	 * Return the vertex referenced by the provided object identifier. If no vertex
	 * is referenced by that identifier, then return null.
	 *
	 * @param id the identifier of the vertex to retrieved from the graph
	 * @return the vertex referenced by the provided identifier or null when no such
	 *         vertex exists
	 */
	@Override
	public Vertex getVertex(String id) {
		Document v = vertices.find(new Document("_id", id)).first();
		Vertex vertex = new PChronoVertex(this, id, vertices);
		if (v == null) {
			return null;
		} else {
			return vertex;
		}
	}

	/**
	 * Return an iterable to all the vertices in the graph. If this is not possible
	 * for the implementation, then an UnsupportedOperationException can be thrown.
	 *
	 * @return an iterable reference to all vertices in the graph
	 */
	@Override
	public Collection<Vertex> getVertices() {
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		MongoCursor<Document> cursor = this.vertices.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			vertices.add(new PChronoVertex(this, doc.getString("_id"), this.vertices));
		}
		return vertices;
	}

	/**
	 * Return an iterable to all the vertices in the graph that have a particular
	 * key/value property. If this is not possible for the implementation, then an
	 * UnsupportedOperationException can be thrown. The graph implementation should
	 * use indexing structures to make this efficient else a full vertex-filter scan
	 * is required.
	 *
	 * @param key   the key of vertex
	 * @param value the value of the vertex
	 * @return an iterable of vertices with provided key and value
	 */
	@Override
	public Collection<Vertex> getVertices(String key, Object value) {
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		MongoCursor<Document> cursor = this.vertices.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			Document properties = doc.get("properties", Document.class);
			if (properties == null)
				continue;
			if (!properties.containsKey(key))
				continue;
			if (properties.get(key).equals(value))
				vertices.add(new PChronoVertex(this, doc.getString("_id"), this.vertices));
		}

		return vertices;
	}

	/**
	 * Add an edge to the graph. The added edges requires a recommended identifier,
	 * a tail vertex, an head vertex, and a label. Like adding a vertex, the
	 * provided object identifier may be ignored by the implementation.
	 *
	 * @param outVertex the vertex on the tail of the edge
	 * @param inVertex  the vertex on the head of the edge
	 * @param label     the label associated with the edge
	 * @return the newly created edge
	 * 
	 */
	@Override
	public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) {
		if (label.contains("|"))
			throw new IllegalArgumentException("An edge label cannot contain '|'");

		String edgeId = PChronoEdge.getEdgeID(outVertex, inVertex, label);
		Document e = edges.find(new Document("_id", edgeId)).first();
		Edge edge = new PChronoEdge(this, outVertex, label, inVertex, edges);
		if (e != null) {
			return edge;
		} else {
			edges.insertOne(new Document("_id", edgeId));
			if (eventBus != null)
				eventBus.send("addEdge", edgeId);
			return edge;
		}
	}

	/**
	 * Return the edge referenced by the provided object identifier. If no edge is
	 * referenced by that identifier, then return null.
	 *
	 * @param outVertex the identifier of the out-going vertex to retrieve from the
	 *                  graph
	 * @param inVertex  the identifier of the in-going vertex to retrieve from the
	 *                  graph
	 * @param label     the the label of the edge
	 * @return the edge referenced by the provided identifier or null when no such
	 *         edge exists
	 */
	@Override
	public Edge getEdge(Vertex outVertex, Vertex inVertex, String label) {
		String edgeId = PChronoEdge.getEdgeID(outVertex, inVertex, label);
		Document e = edges.find(new Document("_id", edgeId)).first();
		Edge edge = new PChronoEdge(this, outVertex, label, inVertex, edges);
		if (e == null) {
			return null;
		} else {
			return edge;
		}
	}

	/**
	 * Return the edge referenced by the provided object identifier. If no edge is
	 * referenced by that identifier, then return null.
	 *
	 * @param id the identifier of the edge to retrieved from the graph
	 * @return the edge referenced by the provided identifier or null when no such
	 *         edge exists
	 */
	@Override
	public Edge getEdge(String id) {
		Document e = edges.find(new Document("_id", id)).first();
		if (e == null) {
			return null;
		} else {
			Edge edge = new PChronoEdge(this, id, edges);
			return edge;
		}
	}

	/**
	 * Return an iterable to all the edges in the graph. If this is not possible for
	 * the implementation, then an UnsupportedOperationException can be thrown.
	 *
	 * @return an iterable reference to all edges in the graph
	 */
	@Override
	public Collection<Edge> getEdges() {
		HashSet<Edge> edges = new HashSet<Edge>();
		MongoCursor<Document> cursor = this.edges.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			edges.add(new PChronoEdge(this, doc.getString("_id"), this.edges));
		}
		return edges;
	}

	/**
	 * Return an iterable to all the edges in the graph that have a particular
	 * key/value property. If this is not possible for the implementation, then an
	 * UnsupportedOperationException can be thrown. The graph implementation should
	 * use indexing structures to make this efficient else a full edge-filter scan
	 * is required.
	 *
	 * @param key   the key of the edge
	 * @param value the value of the edge
	 * @return an iterable of edges with provided key and value
	 */
	@Override
	public Collection<Edge> getEdges(String key, Object value) {
		HashSet<Edge> edges = new HashSet<Edge>();
		MongoCursor<Document> cursor = this.edges.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			Document properties = doc.get("properties", Document.class);
			if (properties == null)
				continue;
			if (!properties.containsKey(key))
				continue;
			if (properties.get(key).equals(value))
				edges.add(new PChronoEdge(this, doc.getString("_id"), this.edges));
		}

		return edges;
	}

	/**
	 * Remove the provided vertex from the graph. Upon removing the vertex, all the
	 * edges by which the vertex is connected must be removed as well.
	 *
	 * @param vertex the vertex to remove from the graph
	 */
	@Override
	public void removeVertex(Vertex vertex) {
		String id = vertex.getId();
		vertices.deleteOne(new Document("_id", id));
		edges.deleteMany(new Document("_o", id));
		edges.deleteMany(new Document("_i", id));
	}

	@Override
	public void removeEdge(Edge edge) {
		edges.deleteOne(new Document("_id", edge.getId()));
	}

	@Override
	public void clear() {
		vertices.deleteMany(new Document());
		edges.deleteMany(new Document());
	}

	public Iterator<Entry<Long, HashSet<EdgeEvent>>> getEdgeEventIterator() {
		// TODO
		TreeMap<Long, HashSet<EdgeEvent>> eventMap = new TreeMap<Long, HashSet<EdgeEvent>>();
		Collection<Edge> edges = getEdges();
		edges.parallelStream().flatMap(e -> {
			Stream<EdgeEvent> stream = e.getEvents().parallelStream();
			return stream;
		}).forEach(ee -> {
			Long t = ee.getTime();
			if (eventMap.containsKey(t)) {
				eventMap.get(t).add(ee);
			} else {
				HashSet<EdgeEvent> newSet = new HashSet<EdgeEvent>();
				newSet.add(ee);
				eventMap.put(t, newSet);
			}
		});
		return eventMap.entrySet().iterator();
	}

	@Override
	public void shutdown() {
		client.close();
	}
}
