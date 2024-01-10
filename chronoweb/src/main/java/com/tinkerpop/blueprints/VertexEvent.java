package com.tinkerpop.blueprints;

import java.util.Collection;

import org.dfpl.chronograph.common.TemporalRelation;

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

}
