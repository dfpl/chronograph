package org.dfpl.chronograph.khronos.manipulation.persistent;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.tinkerpop.blueprints.*;

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
		String[] arr = id.split("|");
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
//		EdgeEvent event = getEvent(time);
//		if (event == null) {
//			EdgeEvent newEe = new MChronoEdgeEvent(this, time);
//			this.events.add(newEe);
//			if (((MChronoGraph) g).getEventBus() != null)
//				((MChronoGraph) g).getEventBus().send("addEdgeEvent", newEe.getId());
//			return newEe;
//		} else
//			return event;
		// TODO
		return null;
	}

	@Override
	public EdgeEvent getEvent(long time) {
//		EdgeEvent event = events.floor(new MChronoEdgeEvent(this, time));
//		if (event == null)
//			return null;
//		else if (event.getTime() != time)
//			return null;
//		else
//			return event;
		// TODO
		return null;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents() {
		// return events;
		// TODO
		return null;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents(long time, TemporalRelation temporalRelation) {
//		NavigableSet<EdgeEvent> validEvents = new TreeSet<>();
//		if (temporalRelation == null)
//			return validEvents;
//
//		for (EdgeEvent event : this.events) {
//			if (TimeInstant.getTemporalRelation(time, event.getTime()).equals(temporalRelation))
//				validEvents.add(event);
//		}
//		return validEvents;
		// TODO
		return null;
	}

	@Override
	public EdgeEvent getEvent(long time, TemporalRelation temporalRelation) {
//		if (temporalRelation.equals(TemporalRelation.isAfter)) {
//			return events.higher(new MChronoEdgeEvent(this, time));
//		} else if (temporalRelation.equals(TemporalRelation.isBefore)) {
//			return events.lower(new MChronoEdgeEvent(this, time));
//		} else {
//			return getEvent(time);
//		}
		// TODO
		return null;
	}

	@Override
	public void removeEvents(long time, TemporalRelation temporalRelation) {
		// this.events.removeIf(event -> TimeInstant.getTemporalRelation(time,
		// event.getTime()).equals(temporalRelation));
		// TODO
	}
}
