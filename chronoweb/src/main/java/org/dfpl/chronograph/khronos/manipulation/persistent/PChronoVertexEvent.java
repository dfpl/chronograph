package org.dfpl.chronograph.khronos.manipulation.persistent;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

import com.mongodb.client.MongoCollection;
import com.tinkerpop.blueprints.*;

/**
 * The persistent implementation of temporal graph database.
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
public class PChronoVertexEvent extends PChronoEvent implements VertexEvent {

	public PChronoVertexEvent(Graph g, String v, Long time, MongoCollection<Document> collection) {
		this.g = g;
		this.element = v;
		this.time = time;
		this.id = v + "_" + time;
		this.collection = collection;
	}

	@Override
	public Iterable<EdgeEvent> getEdgeEvents(Direction direction, TemporalRelation tr, String label) {

		PChronoGraph pg = (PChronoGraph) g;
		Document query = new Document();
		if (direction.equals(Direction.OUT)) {
			query.append("_o", element);
		} else if (direction.equals(Direction.IN)) {
			query.append("_i", element);
		} else {
			throw new IllegalArgumentException();
		}
		query.append("_l", label);
		if (tr.equals(TemporalRelation.isAfter)) {
			query.append("_t", new Document("$gt", time));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			query.append("_t", new Document("$lt", time));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			query.append("_t", time);
		} else {
			throw new IllegalArgumentException();
		}

		return pg.edgeEvents.find(query).map(doc -> {
			return new PChronoEdgeEvent(g, doc.getString("_e"), doc.getString("_o"), doc.getString("_l"),
					doc.getString("_i"), doc.getLong("_t"), pg.edgeEvents);
		});
	}

	@Override
	public Iterable<VertexEvent> getVertexEvents(Direction direction, TemporalRelation tr, String label) {
		PChronoGraph pg = (PChronoGraph) g;
		Document query = new Document();
		if (direction.equals(Direction.OUT)) {
			query.append("_o", element);
		} else if (direction.equals(Direction.IN)) {
			query.append("_i", element);
		} else {
			throw new IllegalArgumentException();
		}
		query.append("_l", label);
		if (tr.equals(TemporalRelation.isAfter)) {
			query.append("_t", new Document("$gt", time));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			query.append("_t", new Document("$lt", time));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			query.append("_t", time);
		} else {
			throw new IllegalArgumentException();
		}

		return pg.edgeEvents.find(query).map(doc -> {
			if (direction.equals(Direction.OUT)) {
				return new PChronoVertexEvent(g, doc.getString("_i"), doc.getLong("_t"), pg.edgeEvents);
			} else {
				return new PChronoVertexEvent(g, doc.getString("_o"), doc.getLong("_t"), pg.edgeEvents);
			}
		});
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		object.put("_v", element);
		object.put("_t", time);
		if (includeProperties) {
			object.put("properties", getProperties());
		}
		return object;
	}
}
