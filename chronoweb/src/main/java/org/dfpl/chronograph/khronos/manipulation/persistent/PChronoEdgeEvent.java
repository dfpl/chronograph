package org.dfpl.chronograph.khronos.manipulation.persistent;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.VertexEvent;

import com.mongodb.client.MongoCollection;
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
public class PChronoEdgeEvent extends PChronoEvent implements EdgeEvent {

	private String outId;
	private String label;
	private String inId;

	public PChronoEdgeEvent(Graph g, String e, String out, String label, String in, long time,
			MongoCollection<Document> collection) {
		this.g = g;
		this.element = e;
		this.outId = out;
		this.label = label;
		this.inId = in;
		this.id = e + "_" + time;
		this.time = time;
		this.collection = collection;
	}

	@Override
	public VertexEvent getVertexEvent(Direction direction) {
		if (direction.equals(Direction.OUT)) {
			return new PChronoVertexEvent(g, outId, time, ((PChronoGraph) g).vertexEvents);
		} else if (direction.equals(Direction.IN)) {
			return new PChronoVertexEvent(g, inId, time, ((PChronoGraph) g).vertexEvents);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Vertex getVertex(Direction direction) {
		if (direction.equals(Direction.OUT)) {
			return new PChronoVertex(g, outId, ((PChronoGraph) g).vertices);
		} else if (direction.equals(Direction.IN)) {
			return new PChronoVertex(g, inId, ((PChronoGraph) g).vertices);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		object.put("_e", element);
		object.put("_o", outId);
		object.put("_l", label);
		object.put("_i", inId);
		object.put("_t", time);
		if (includeProperties)
			object.put("properties", getProperties());

		return object;
	}

	@Override
	public Graph getGraph() {
		return g;
	}
}
