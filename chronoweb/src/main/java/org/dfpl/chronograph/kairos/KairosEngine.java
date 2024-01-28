package org.dfpl.chronograph.kairos;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.tinkerpop.blueprints.Graph;

import io.vertx.core.eventbus.EventBus;

@SuppressWarnings("unused")
public class KairosEngine {

	private Graph graph;
	private EventBus mainEventBus;
	private HashMap<Long, HashSet<AbstractKairosProgram<?>>> kairosPrograms;

	public KairosEngine(Graph graph, EventBus mainEventBus) {
		this.graph = graph;
		this.mainEventBus = mainEventBus;
		this.kairosPrograms = new HashMap<Long, HashSet<AbstractKairosProgram<?>>>();

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

			kairosPrograms.forEach((start, programs) -> {
				programs.forEach(program -> {
					//
				});
			});

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

	public void addSubscriptionBase(Long startTime, AbstractKairosProgram<?> program) {
		HashSet<AbstractKairosProgram<?>> programs = kairosPrograms.get(startTime);
		if (programs == null) {
			programs = new HashSet<AbstractKairosProgram<?>>();
			programs.add(program);
			kairosPrograms.put(startTime, programs);
		} else {
			programs.add(program);
		}
		program.onInitialization();
	}

}
