package org.dfpl.chronograph.kairos;

import org.dfpl.chronograph.chronoweb.Server;

import com.tinkerpop.blueprints.Graph;

import io.vertx.core.eventbus.EventBus;

public class KairosEngine {
	@SuppressWarnings("unused")
	private Graph graph;
	private EventBus mainEventBus;

	public KairosEngine(Graph graph, EventBus mainEventBus) {
		this.graph = graph;
		this.mainEventBus = mainEventBus;

		this.mainEventBus.consumer("addVertex", v -> {
			Server.logger.debug("kairos addVertex: " + v.body());
		});

		this.mainEventBus.consumer("addEdge", e -> {
			Server.logger.debug("kairos addEdge: " + e.body());
		});

		this.mainEventBus.consumer("removeVertex", v -> {
			Server.logger.debug("kairos removeVertex: " + v.body());
		});

		this.mainEventBus.consumer("removeEdge", e -> {
			Server.logger.debug("kairos removeEdge: " + e.body());
		});

		this.mainEventBus.consumer("addVertexEvent", ve -> {
			Server.logger.debug("kairos addVertexEvent: " + ve.body());
		});

		this.mainEventBus.consumer("addEdgeEvent", ee -> {
			Server.logger.debug("kairos addEdgeEvent: " + ee.body());
		});

		this.mainEventBus.consumer("removeVertexEvent", ve -> {
			Server.logger.debug("kairos removeVertexEvent: " + ve.body());
		});

		this.mainEventBus.consumer("removeEdgeEvent", ee -> {
			Server.logger.debug("kairos removeEdgeEvent: " + ee.body());
		});

		this.mainEventBus.consumer("clear", e -> {
			Server.logger.debug("kairos cleared");
		});

	}

}
