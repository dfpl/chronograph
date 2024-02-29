package org.dfpl.chronograph.khronos.manipulation.persistent;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tinkerpop.blueprints.*;

import io.vertx.core.eventbus.EventBus;

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
public class PChronoGraph implements Graph {

	private MongoClient client;
	private MongoDatabase database;
	MongoCollection<Document> vertices;
	MongoCollection<Document> edges;
	MongoCollection<Document> vertexEvents;
	MongoCollection<Document> edgeEvents;

	private EventBus eventBus;

	public MongoCollection<Document> getVertexCollection() {
		return vertices;
	}

	public MongoCollection<Document> getVertexEventCollection() {
		return vertexEvents;
	}

	public void createIndex() {
		List<Document> edgeIndexes = new ArrayList<Document>();
		edges.listIndexes().into(edgeIndexes);
		if (edgeIndexes.size() <= 1) {
			edges.createIndex(new Document("_o", 1).append("_l", 1).append("_i", 1));
			edges.createIndex(new Document("_i", 1).append("_l", 1).append("_o", 1));
		}
		List<Document> vertexEventIndexes = new ArrayList<Document>();
		vertexEvents.listIndexes().into(vertexEventIndexes);
		if (vertexEventIndexes.size() <= 1) {
			vertexEvents.createIndex(new Document("_v", 1).append("_t", 1));
		}
		List<Document> edgeEventIndexes = new ArrayList<Document>();
		edgeEvents.listIndexes().into(edgeEventIndexes);
		if (edgeEventIndexes.size() <= 1) {
			edgeEvents.createIndex(new Document("_o", 1).append("_l", 1).append("_t", 1).append("_i", 1));
			edgeEvents.createIndex(new Document("_i", 1).append("_l", 1).append("_t", 1).append("_o", 1));
		}
	}

	public PChronoGraph(String connectionString, String databaseName) {
		client = MongoClients.create(connectionString);
		database = client.getDatabase(databaseName);
		vertices = database.getCollection("vertex", Document.class);
		edges = database.getCollection("edge", Document.class);
		vertexEvents = database.getCollection("vertexEvent", Document.class);
		edgeEvents = database.getCollection("edgeEvents", Document.class);
		createIndex();
	}

	public PChronoGraph(String connectionString, String databaseName, EventBus eventBus) {
		client = MongoClients.create(connectionString);
		database = client.getDatabase(databaseName);
		vertices = database.getCollection("vertex", Document.class);
		edges = database.getCollection("edge", Document.class);
		vertexEvents = database.getCollection("vertexEvent", Document.class);
		edgeEvents = database.getCollection("edgeEvents", Document.class);
		this.eventBus = eventBus;
		createIndex();
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
			vertices.insertOne(vertex.toDocument(false));
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
	public Iterable<Vertex> getVertices() {
		return this.vertices.find().map(doc -> {
			return new PChronoVertex(this, doc.getString("_id"), this.vertices);
		});
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
	public Iterable<Vertex> getVertices(String key, Object value) {
		return this.vertices.find().map(doc -> {
			Document properties = doc.get("properties", Document.class);
			if (properties == null)
				return null;
			if (!properties.containsKey(key))
				return null;
			if (properties.get(key).equals(value))
				return new PChronoVertex(this, doc.getString("_id"), this.vertices);
			else
				return null;
		});
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
			edges.insertOne(edge.toDocument(false));
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
	public Iterable<Edge> getEdges() {
		return this.edges.find().map(doc -> {
			return new PChronoEdge(this, doc.getString("_id"), this.edges);
		});
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
	public Iterable<Edge> getEdges(String key, Object value) {
		return this.edges.find().map(doc -> {
			Document properties = doc.get("properties", Document.class);
			if (properties == null)
				return null;
			if (!properties.containsKey(key))
				return null;
			if (properties.get(key).equals(value))
				new PChronoEdge(this, doc.getString("_id"), this.edges);
			return null;
		});
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
		vertexEvents.deleteMany(new Document("_v", id));
		edges.deleteMany(new Document("_o", id));
		edges.deleteMany(new Document("_i", id));
		edgeEvents.deleteMany(new Document("_o", id));
		edgeEvents.deleteMany(new Document("_i", id));
	}

	@Override
	public void removeEdge(Edge edge) {
		edges.deleteOne(new Document("_id", edge.getId()));
		edgeEvents.deleteMany(new Document("_e", edge.getId()));
	}

	@Override
	public void clear() {
		database.drop();
		createIndex();
	}

	@Override
	public void shutdown() {
		client.close();
	}

	@Override
	public Iterable<EdgeEvent> getEdgeEvents() {
		return edgeEvents.find().sort(new Document("_t", 1)).map(doc -> {
			return new PChronoEdgeEvent(this, doc.getString("_id"), doc.getString("_o"), doc.getString("_l"),
					doc.getString("_i"), doc.getLong("_t"), edgeEvents);
		});
	}
}
