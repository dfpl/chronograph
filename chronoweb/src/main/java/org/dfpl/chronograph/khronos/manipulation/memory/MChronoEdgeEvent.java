package org.dfpl.chronograph.khronos.manipulation.memory;

import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
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
public class MChronoEdgeEvent extends MChronoEvent implements EdgeEvent {

	public MChronoEdgeEvent(Edge e, Long time) {
		this.g = e.getGraph();
		this.element = e;
		this.time = time;
		this.id = e + "_" + time;
		this.properties = new Document();
	}

	@Override
	public VertexEvent getVertexEvent(Direction direction) {
		return ((Edge) element).getVertex(direction).getEvent(time);
	}

	@Override
	public Vertex getVertex(Direction direction) {
		return ((Edge) element).getVertex(direction);
	}

	@Override
	public String getLabel() {
		return ((Edge) element).getLabel();
	}

	public Document toDocument(boolean includeProperties) {
		Document object = ((Edge) element).toDocument(false);
		object.put("_id", id);
		object.put("_e", element.getId());
		object.put("_t", time);
		if (includeProperties)
			object.put("properties", properties);

		return object;
	}

	@Override
	public Graph getGraph() {
		return element.getGraph();
	}
}
