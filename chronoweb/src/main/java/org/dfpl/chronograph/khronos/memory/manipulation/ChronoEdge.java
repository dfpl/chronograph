package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;

import com.tinkerpop.blueprints.*;

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
public class ChronoEdge implements Edge {

	private Graph g;
	private String id;
	private Vertex out;
	private String label;
	private Vertex in;
	private HashMap<String, Object> properties;

	public ChronoEdge(Graph g, Vertex out, String label, Vertex in) {
		this.g = g;
		this.out = out;
		this.label = label;
		this.in = in;
		this.id = getEdgeID(out, in, label);
		this.properties = new HashMap<>();
	}

	public static String getEdgeID(Vertex out, Vertex in, String label) {
		return out.toString() + "|" + label + "|" + in.toString();
	}

	@Override
	public Vertex getVertex(Direction direction) throws IllegalArgumentException {
		if (direction.equals(Direction.OUT)) {
			return out;
		} else if (direction.equals(Direction.IN)) {
			return in;
		} else {
			throw new IllegalArgumentException("A direction should be either OUT or IN");
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void remove() {
		g.removeEdge(this);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Edge))
			return false;
		return id.equals(obj.toString());
	}

	@Override
	public String toString() {
		return id;
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		return (T) properties.remove(key);
	}
}
