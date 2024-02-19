package org.dfpl.chronograph.khronos.memory.manipulation;

import java.util.*;
import java.util.stream.Collectors;

import org.bson.Document;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.TimeInstant;
import org.dfpl.chronograph.common.VertexEvent;

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
public class MChronoVertex extends MChronoElement implements Vertex {

	private NavigableSet<VertexEvent> events;

	MChronoVertex(MChronoGraph g, String id) {
		this.id = id;
		this.g = g;
		this.properties = new Document();
		this.events = new TreeSet<VertexEvent>();
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

		if (labels == null || labels.isEmpty()) {
			return edgeSet.get(id);
		} else {
			return edgeSet.get(id).parallelStream().filter(e -> {
				for (String label : labels) {
					if (e.getLabel().equals(label))
						return true;
				}
				return false;
			}).collect(Collectors.toSet());
		}
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

		if (labels == null || labels.isEmpty()) {
			return edgeSet.get(id).parallelStream().map(e -> e.getVertex(direction.opposite()))
					.collect(Collectors.toSet());
		} else {
			return edgeSet.get(id).parallelStream().filter(e -> {
				for (String label : labels) {
					if (e.getLabel().equals(label))
						return true;
				}
				return false;
			}).map(e -> e.getVertex(direction.opposite())).collect(Collectors.toSet());
		}
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex) {
		return g.addEdge(this, inVertex, label);
	}

	@Override
	public void remove() {
		g.removeVertex(this);
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		if (includeProperties)
			object.put("properties", properties);
		return object;
	}

	@Override
	public VertexEvent addEvent(long time) {
		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event == null) {
			VertexEvent newVe = new MChronoVertexEvent(this, time);
			this.events.add(newVe);
			if (g.getEventBus() != null)
				g.getEventBus().send("addVertexEvent", newVe.getId());
			return newVe;
		} else
			return event;
	}

	@Override
	public VertexEvent getEvent(long time) {
		VertexEvent event = getEvent(time, TemporalRelation.cotemporal);
		if (event == null)
			return new MChronoVertexEvent(this, time);
		else
			return event;
	}

	@Override
	public NavigableSet<VertexEvent> getEvents(boolean awareOutEvents, boolean awareInEvents) {
		NavigableSet<VertexEvent> resultSet = new TreeSet<>();

		resultSet.addAll(events);

		List<Direction> directions = new LinkedList<>();
		if (awareOutEvents)
			directions.add(Direction.OUT);
		if (awareInEvents)
			directions.add(Direction.IN);

		for (Direction direction : directions) {
			for (Edge e : this.getEdges(direction, null)) {
				e.getEvents().stream().map(Event::getTime).forEach(t -> {
					resultSet.add(new MChronoVertexEvent(this, t));
				});
			}
		}
		return resultSet;
	}

	@Override
	public NavigableSet<VertexEvent> getEvents(long time, TemporalRelation tr, boolean awareOutEvents,
			boolean awareInEvents) {
		NavigableSet<VertexEvent> resultSet = new TreeSet<>();

		resultSet.addAll(this.getEvents(time, tr));

		List<Direction> directions = new LinkedList<>();
		if (awareOutEvents)
			directions.add(Direction.OUT);
		if (awareInEvents)
			directions.add(Direction.IN);

		for (Direction direction : directions) {
			for (Edge e : this.getEdges(direction, null)) {
				e.getEvents(time, tr).stream().map(Event::getTime).forEach(t -> {
					resultSet.add(new MChronoVertexEvent(this, t));
				});
			}
		}
		return resultSet;
	}

	@Override
	public void removeEvents(long time, TemporalRelation tr) {
		this.events.removeIf(event -> {
			if (TimeInstant.getTemporalRelation(time, event.getTime()).equals(tr))
				return true;
			else
				return false;
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends Event> NavigableSet<T> getEvents(long time, TemporalRelation... temporalRelations) {
		NavigableSet<Event> validEvents = new TreeSet<>();
		if (temporalRelations == null)
			return (NavigableSet<T>) validEvents;

		for (Event event : this.events) {
			for (TemporalRelation tr : temporalRelations) {
				if (TimeInstant.checkTemporalRelation(event.getTime(), time, tr))
					validEvents.add(event);
			}

		}
		return (NavigableSet<T>) validEvents;
	}

	@Override
	public VertexEvent getEvent(long time, TemporalRelation tr) {
		MChronoVertexEvent tve = new MChronoVertexEvent(this, time);
		if (tr.equals(TemporalRelation.isAfter)) {
			return events.higher(tve);
		} else if (tr.equals(TemporalRelation.isBefore)) {
			return events.lower(tve);
		} else {
			VertexEvent ve = events.floor(tve);
			if (ve == null)
				return null;
			else if (ve.equals(tve))
				return ve;
			else
				return null;
		}
	}
}
