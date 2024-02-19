package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.TimeInstant;

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
public class MChronoEdge extends MChronoElement implements Edge {

	private Vertex out;
	private String label;
	private Vertex in;

	private NavigableSet<EdgeEvent> events;

	public MChronoEdge(Graph g, Vertex out, String label, Vertex in) {
		this.g = (MChronoGraph) g;
		this.out = out;
		this.label = label;
		this.in = in;
		this.id = getEdgeID(out, in, label);
		this.properties = new Document();
		this.events = new TreeSet<EdgeEvent>();
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

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		object.put("_o", out.getId());
		object.put("_l", label);
		object.put("_i", in.getId());
		if (includeProperties)
			object.put("properties", properties);
		return object;
	}

	@Override
	public EdgeEvent addEvent(long time) {
		EdgeEvent event = getEvent(time);
		if (event == null) {
			EdgeEvent newEe = new MChronoEdgeEvent(this, time);
			this.events.add(newEe);
			if (((MChronoGraph) g).getEventBus() != null)
				((MChronoGraph) g).getEventBus().send("addEdgeEvent", newEe.getId());
			return newEe;
		} else
			return event;
	}

	@Override
	public EdgeEvent getEvent(long time) {
		EdgeEvent event = events.floor(new MChronoEdgeEvent(this, time));
		if (event == null)
			return null;
		else if (event.getTime() != time)
			return null;
		else
			return event;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents() {
		return events;
	}

	@Override
	public NavigableSet<EdgeEvent> getEvents(long time, TemporalRelation temporalRelation) {
		NavigableSet<EdgeEvent> validEvents = new TreeSet<>();
		if (temporalRelation == null)
			return validEvents;

		for (EdgeEvent event : this.events) {
			if (TimeInstant.getTemporalRelation(time, event.getTime()).equals(temporalRelation))
				validEvents.add(event);
		}
		return validEvents;
	}

	@Override
	public EdgeEvent getEvent(long time, TemporalRelation temporalRelation) {
		if (temporalRelation.equals(TemporalRelation.isAfter)) {
			return events.higher(new MChronoEdgeEvent(this, time));
		} else if (temporalRelation.equals(TemporalRelation.isBefore)) {
			return events.lower(new MChronoEdgeEvent(this, time));
		} else {
			return getEvent(time);
		}
	}

	@Override
	public void removeEvents(long time, TemporalRelation temporalRelation) {
		this.events.removeIf(event -> TimeInstant.getTemporalRelation(time, event.getTime()).equals(temporalRelation));
	}
}
