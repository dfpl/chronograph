package com.tinkerpop.blueprints;

public interface EdgeEvent extends Event {

	/**
	 * Get a vertex event
	 * 
	 * @param direction
	 * @return the vertex event
	 */
	public VertexEvent getVertexEvent(Direction direction);

	public Vertex getVertex(Direction direction);

	public String getLabel();
}
