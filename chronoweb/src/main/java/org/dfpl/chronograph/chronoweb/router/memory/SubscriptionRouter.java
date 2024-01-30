package org.dfpl.chronograph.chronoweb.router.memory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.Set;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.kairos.AbstractKairosProgram;
import org.dfpl.chronograph.kairos.KairosEngine;
import org.dfpl.chronograph.kairos.gamma.GammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.LongGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.SparseGammaTable;
import org.dfpl.chronograph.kairos.recipe.IsAfterReachability;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertex;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertexEvent;
import com.tinkerpop.blueprints.Graph;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import static org.dfpl.chronograph.chronoweb.Server.*;

public class SubscriptionRouter extends BaseRouter {

	private KairosEngine kairos;

	public SubscriptionRouter(Graph graph, KairosEngine kairos) {
		super(graph);
		this.kairos = kairos;
	}

	public void registerGetSubscriptions(Router router, EventBus eventBus) {
		router.get("/chronoweb/subscribe").handler(routingContext -> {
			JsonArray arr = new JsonArray();
			Set<VertexEvent> set = kairos.getSources();
			for (VertexEvent s : set) {
				arr.add(s.getId());
			}
			sendResult(routingContext, "application/json", arr.toString(), 200);
		});

		Server.logger.info("GET /chronoweb/subscribe router added");
	}

	public void registerSubscribeVertexEventRouter(Router router, EventBus eventBus) {

		router.post("/chronoweb/subscribe/:resource").handler(routingContext -> {
			String resource = routingContext.pathParam("resource");
			String recipeParameter = getStringURLParameter(routingContext, "recipe");

			if (recipeParameter == null) {
				sendResult(routingContext, 406);
				return;
			}

			if (vtPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					String vertexID = arr[0];
					long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.addVertex(vertexID);
					ChronoVertexEvent ve = (ChronoVertexEvent) v.getEvent(time);

					String subDirectoryName = Server.baseDirectory + "\\" + ve.getTime() + "_" + recipeParameter;
					File subDirectory = new File(subDirectoryName);
					if (!subDirectory.exists())
						subDirectory.mkdirs();

					if (recipeParameter.equals("IsAfterReachability")) {

						AbstractKairosProgram<?> existing = kairos.getProgram(time, "IsAfterReachability");
						if (existing != null) {
							GammaTable<String, Long> gammaTable = existing.getGammaTable();
							if (gammaTable.getSources().contains(v.getId())) {
								sendResult(routingContext, 406);
								return;
							} else {
								gammaTable.set(v.getId(), v.getId(), new LongGammaElement(time));
								kairos.addSubscription(v, ve.getTime(), new IsAfterReachability(graph, gammaTable));
								sendResult(routingContext, 200);
								return;
							}

						} else {
							SparseGammaTable<String, Long> gammaTable = new SparseGammaTable<String, Long>(
									subDirectoryName, LongGammaElement.class);
							gammaTable.set(v.getId(), v.getId(), new LongGammaElement(time));
							kairos.addSubscription(v, ve.getTime(), new IsAfterReachability(graph, gammaTable));
							sendResult(routingContext, 200);
							return;
						}

					} else {
						sendResult(routingContext, 406);
						return;
					}

				} catch (IllegalArgumentException | NotDirectoryException | FileNotFoundException e) {
					sendResult(routingContext, 406);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}

		});

		Server.logger.info("POST /chronoweb/subscribe/:resource router added");
	}

}
