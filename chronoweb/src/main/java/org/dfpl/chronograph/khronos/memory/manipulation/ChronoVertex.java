package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;
import java.util.stream.Collectors;

import com.tinkerpop.blueprints.*;

import io.vertx.core.json.JsonObject;

/**
 * The in-memory implementation of temporal graph database.
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
public class ChronoVertex implements Vertex {

	private ChronoGraph g;
	private String id;
	private HashMap<String, Object> properties;

	ChronoVertex(ChronoGraph g, String id) {
		this.id = id;
		this.g = g;
		this.properties = new HashMap<>();
	}

	@Override
	public Collection<Edge> getEdges(Direction direction, List<String> labels) {
		HashMap<String, HashSet<Edge>> edgeSet = null;
		if (direction.equals(Direction.OUT))
			edgeSet = g.getOutEdges();
		else if (direction.equals(Direction.IN))
			edgeSet = g.getInEdges();

		if (edgeSet == null || !edgeSet.containsKey(id))
			return new HashSet<>();

		return edgeSet.get(id).parallelStream().filter(e -> {
			if (labels == null)
				return true;

			for (String label : labels) {
				if (e.getLabel().equals(label))
					return true;
			}
			return false;
		}).collect(Collectors.toSet());
	}

	@Override
	public Collection<Vertex> getVertices(Direction direction, List<String> labels) {
		HashMap<String, HashSet<Edge>> edgeSet = null;
		if (direction.equals(Direction.OUT))
			edgeSet = g.getOutEdges();
		else if (direction.equals(Direction.IN))
			edgeSet = g.getInEdges();

		if (edgeSet == null || !edgeSet.containsKey(id))
			return new HashSet<>();

		return edgeSet.get(id).parallelStream().filter(e -> {
			if (labels == null)
				return true;

			for (String label : labels) {
				if (e.getLabel().equals(label))
					return true;
			}
			return false;
		}).map(e -> e.getVertex(direction.opposite())).collect(Collectors.toSet());
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex) {
		return g.addEdge(this, inVertex, label);
	}

	@Override
	public void remove() {
		g.removeVertex(this);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key) {
		return (T) properties.get(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return this.properties.keySet();
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public void setProperties(JsonObject properties, boolean isSet) {
		if (!isSet)
			this.properties.clear();
		properties.stream().forEach(e -> {
			this.properties.put(e.getKey(), e.getValue());
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		return (T) properties.remove(key);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChronoVertex))
			return false;
		return this.getId().equals(((ChronoVertex) obj).getId());
	}

	@Override
	public String toString() {
		return id;
	}

	public JsonObject toJsonObject(boolean includeProperties) {
		JsonObject object = new JsonObject();
		object.put("_id", id);
		if (includeProperties)
			object.put("properties", new JsonObject(properties));
		return object;
	}
}
