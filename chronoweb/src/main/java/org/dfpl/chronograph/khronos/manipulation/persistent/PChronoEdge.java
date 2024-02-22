package org.dfpl.chronograph.khronos.manipulation.persistent;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.tinkerpop.blueprints.*;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
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
@SuppressWarnings("unused")
public class PChronoEdge extends PChronoElement implements Edge {

	private String outId;
	private String label;
	private String inId;

	public PChronoEdge(PChronoGraph g, Vertex out, String label, Vertex in, MongoCollection<Document> collection) {
		this.g = g;
		this.outId = out.getId();
		this.label = label;
		this.inId = in.getId();
		this.id = getEdgeID(out, in, label);
		this.collection = collection;
	}

	public PChronoEdge(PChronoGraph g, String out, String label, String in, MongoCollection<Document> collection) {
		this.g = g;
		this.outId = out;
		this.label = label;
		this.inId = in;
		this.id = getEdgeID(out, in, label);
		this.collection = collection;
	}

	public PChronoEdge(PChronoGraph g, String id, MongoCollection<Document> collection) {
		this.g = g;
		String[] arr = id.split("\\|");
		this.outId = arr[0];
		this.label = arr[1];
		this.inId = arr[2];
		this.id = id;
		this.collection = collection;
	}

	public static String getEdgeID(Vertex out, Vertex in, String label) {
		return out.toString() + "|" + label + "|" + in.toString();
	}

	public static String getEdgeID(String out, String in, String label) {
		return out + "|" + label + "|" + in;
	}

	@Override
	public Vertex getVertex(Direction direction) throws IllegalArgumentException {
		if (direction.equals(Direction.OUT)) {
			return new PChronoVertex((PChronoGraph) g, outId, collection);
		} else if (direction.equals(Direction.IN)) {
			return new PChronoVertex((PChronoGraph) g, inId, collection);
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

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		object.put("_o", outId);
		object.put("_l", label);
		object.put("_i", inId);
		if (includeProperties)
			object.put("properties", getProperties());
		return object;
	}

	@Override
	public EdgeEvent addEvent(long time) {
		EdgeEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event != null) {
			return event;
		} else {
			PChronoGraph pg = (PChronoGraph) g;

			event = new PChronoEdgeEvent(g, id, outId, label, inId, time, pg.edgeEvents);
			pg.edgeEvents.insertOne(event.toDocument(false));
			EventBus eb = pg.getEventBus();
			if (eb != null)
				eb.send("addEdgeEvent", event.getId());
			return event;
		}
	}

	@Override
	public EdgeEvent getEvent(long time) {
		EdgeEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event == null)
			return new PChronoEdgeEvent(g, id, outId, label, inId, time, ((PChronoGraph) g).edgeEvents);
		else
			return event;
	}

	@Override
	public Iterable<EdgeEvent> getEvents() {
		PChronoGraph pg = (PChronoGraph) g;
		return pg.edgeEvents.find(new Document("_v", id)).sort(new Document("_t", 1)).map(doc -> {
			return new PChronoEdgeEvent(g, id, outId, label, inId, doc.getLong("_t"), collection);
		});
	}

	@Override
	public Iterable<EdgeEvent> getEvents(long time, TemporalRelation tr) {
		Document query = new Document("_e", id);
		if (tr.equals(TemporalRelation.isAfter)) {
			query.append("_t", new Document("$gt", time));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			query.append("_t", new Document("$lt", time));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			query.append("_t", time);
		} else {
			throw new IllegalArgumentException();
		}

		PChronoGraph pg = (PChronoGraph) g;
		return pg.edgeEvents.find(query).sort(new Document("_t", 1)).map(doc -> {
			return new PChronoEdgeEvent(pg, id, outId, label, inId, doc.getLong("_t"), collection);
		});
	}

	@Override
	public EdgeEvent getEvent(long time, TemporalRelation tr) {
		PChronoGraph pg = (PChronoGraph) g;
		Document result = null;
		if (tr.equals(TemporalRelation.isAfter)) {
			result = pg.edgeEvents.find(new Document("_e", id).append("$gt", new Document("_t", time))).first();		
		} else if (tr.equals(TemporalRelation.isBefore)) {
			result = pg.edgeEvents.find(new Document("_e", id).append("$lt", new Document("_t", time))).first();
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			result = pg.edgeEvents.find(new Document("_e", id).append("_t", time)).first();
		} else {
			throw new IllegalArgumentException("Illegal temporal relation");
		}
		if(result == null)
			return null;
		return new PChronoEdgeEvent(pg, id, outId, label, inId, result.getLong("_t"), collection);
	}

	@Override
	public void removeEvents(long time, TemporalRelation tr) {
		PChronoGraph pg = (PChronoGraph) g;
		if (tr.equals(TemporalRelation.isAfter)) {
			pg.edgeEvents.deleteMany(new Document("_t", new Document("$gt", time)));
		} else if (tr.equals(TemporalRelation.isBefore)) {
			pg.edgeEvents.deleteMany(new Document("_t", new Document("$lt", time)));
		} else if (tr.equals(TemporalRelation.cotemporal)) {
			pg.edgeEvents.deleteMany(new Document("_t", time));
		} else {
			throw new IllegalArgumentException();
		}
	}
}
