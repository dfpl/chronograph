package org.dfpl.chronograph.common;

import java.util.Collection;

import org.bson.Document;

import com.tinkerpop.blueprints.Direction;

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
public interface VertexEvent extends Event {

	/**
	 * Return chronologically closest neighbor edge events per a pair (out vertex,
	 * label, in vertex)
	 * 
	 * @param direction
	 * @param tr
	 * @param label
	 * @return
	 */
	public Collection<EdgeEvent> getEdgeEvents(Direction direction, TemporalRelation tr, String label);

	/**
	 * Return chronologically closest neighbor vertex events per a pair (out vertex,
	 * label, in vertex)
	 * 
	 * @param direction
	 * @param tr
	 * @param label
	 * @param criteria
	 * @return
	 */
	public Collection<VertexEvent> getVertexEvents(Direction direction, TemporalRelation tr, String label);

	public Document toDocument(boolean includeProperties);
}
