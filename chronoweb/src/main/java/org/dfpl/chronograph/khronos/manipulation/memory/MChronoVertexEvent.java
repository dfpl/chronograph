package org.dfpl.chronograph.khronos.manipulation.memory;

import java.util.*;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.TemporalRelation;
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
public class MChronoVertexEvent extends MChronoEvent implements VertexEvent {

	public MChronoVertexEvent(Vertex v, Long time) {
		this.g = v.getGraph();
		this.element = v;
		this.time = time;
		this.id = v + "_" + time;
		this.properties = new Document();
	}

	@Override
	public Iterable<EdgeEvent> getEdgeEvents(Direction direction, TemporalRelation tr, String label) {
		return ((Collection<Edge>) ((Vertex) element).getEdges(direction, List.of(label))).parallelStream()
				.map(e -> e.getEvent(time, tr)).toList();
	}

	@Override
	public Iterable<VertexEvent> getVertexEvents(Direction direction, TemporalRelation tr, String label) {
		return ((Collection<Edge>) ((Vertex) element).getEdges(direction, List.of(label))).parallelStream().map(e -> {
			EdgeEvent neighborEe = e.getEvent(time, tr);
			if (neighborEe == null)
				return null;
			else
				return neighborEe.getVertexEvent(direction.opposite());
		}).toList();
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		object.put("_v", element.getId());
		object.put("_t", time);
		if (includeProperties)
			object.put("properties", properties);
		return object;
	}
}
