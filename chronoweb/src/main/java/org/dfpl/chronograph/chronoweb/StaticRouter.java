package org.dfpl.chronograph.chronoweb;

import java.util.List;

import org.dfpl.chronograph.khronos.memory.manipulation.ChronoGraph;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertex;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class StaticRouter {

	private static void sendResult(RoutingContext routingContext, String contentType, String message, int code) {
		routingContext.response().putHeader("content-type", contentType).setStatusCode(code).end(message);
	}

	@SuppressWarnings("unused")
	private static void sendResult(RoutingContext routingContext, String message, int code) {
		routingContext.response().setStatusCode(code).end(message);
	}

	private static void sendResult(RoutingContext routingContext, int code) {
		routingContext.response().setStatusCode(code).end();
	}

	@SuppressWarnings("unused")
	private static Boolean getBooleanURLParameter(RoutingContext routingContext, String key) {
		List<String> list = routingContext.queryParam(key);
		if (list.isEmpty())
			return null;
		else {
			try {
				return Boolean.parseBoolean(list.get(0));
			} catch (Exception e) {
				return null;
			}
		}
	}

	private static String getStringURLParameter(RoutingContext routingContext, String key) {
		List<String> list = routingContext.queryParam(key);
		if (list.isEmpty())
			return null;
		else {
			return list.get(0);
		}
	}

	static void registerAddVertexRouter(Router router, ChronoGraph graph) {
		router.post("/chronoweb/:vertexID").consumes("application/json").handler(routingContext -> {
			String vertexID = routingContext.pathParam("vertexID");
			String propertiesParameter = getStringURLParameter(routingContext, "properties");
			boolean isSet = propertiesParameter == null || propertiesParameter.equals("set") ? true : false;
			JsonObject properties = null;
			try {
				properties = routingContext.body().asJsonObject();
			} catch (Exception e) {
				sendResult(routingContext, "text/plain", e.getMessage(), 400);
			}
			if (properties == null)
				properties = new JsonObject();
			try {
				ChronoVertex v = (ChronoVertex) graph.addVertex(vertexID);
				v.setProperties(properties, isSet);
				sendResult(routingContext, "application/json", v.toJsonObject().toString(), 200);
			} catch (IllegalArgumentException e) {
				sendResult(routingContext, 406);
			}
		});

		Server.logger.info("POST /chronoweb/:vertexID router added");
	}

//	static void registerPutElementRouter(Router router) {
	// put == replace

//	router.put("chronoweb/:resource").handler(routingContext -> {
//		HttpServerResponse response = routingContext.response().setChunked(true);
//		String body = routingContext.getBodyAsString();
//		String resource = routingContext.pathParam("resource");
//		if (vPattern.matcher(resource).matches()) {
//			khronos.putVertex(response, resource, body);
//		} else if (ePattern.matcher(resource).matches()) {
//			khronos.putEdge(response, resource, body);
//		} else if (vtPattern.matcher(resource).matches()) {
//			khronos.putTimestampVertexEvent(response, resource, body);
//		} else if (vpPattern.matcher(resource).matches()) {
//			khronos.putTimeperiodVertexEvent(response, resource, body);
//		} else if (etPattern.matcher(resource).matches()) {
//			khronos.putTimestampEdgeEvent(response, resource, body);
//		} else if (epPattern.matcher(resource).matches()) {
//			khronos.putTimeperiodEdgeEvent(response, resource, body);
//		} else {
//			Server.logger.error("PUT /:resource: no resource pattern matched");
//			response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
//					.end(new JsonObject().put("error", "no resource pattern matched").toString());
//		}
//
//	});
//
//}

	@SuppressWarnings("unused")
	static void registerGetElementsRouter(Router router) {
		router.get("chronoweb").handler(routingContext -> {
			HttpServerResponse response = routingContext.response().setChunked(true);
			String body = routingContext.body().asString();
			List<String> targetList = routingContext.queryParam("target");
			if (targetList.isEmpty()) {
				routingContext.response().setStatusCode(400).end("target is mandatory field: vertices|edges");
			} else {
				String target = targetList.get(0);
				if (target.equals("vertices")) {
					// khronos.sendVertices(routingContext.response(), body);
				} else if (target.equals("edges")) {
					// khronos.sendEdges(routingContext.response(), routingContext.queryParams(),
					// body);
				} else if (target.equals("ping")) {
					routingContext.response().setStatusCode(200).end();
				} else {
					// Server.logger.error("PUT / unsupported target type: vertices|edges");
					response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
							.end(new JsonObject().put("error", "unsupported target type: vertices|edge").toString());
				}
			}
		});
	}

//	static void registerDeleteGraphRouter(Router router) {
//		router.delete("chronoweb").handler(routingContext -> {
//			//khronos.dropGraph(routingContext.response());
//		});
//	}

//	static void registerGetElementRouter(Router router) {
//		router.get("chronoweb/:resource").handler(routingContext -> {
//			HttpServerResponse response = routingContext.response().setChunked(true);
//			String body = routingContext.body().asString();
//			String resource = routingContext.pathParam("resource");
//
//			if (vPattern.matcher(resource).matches()) {
//				khronos.sendVertex(routingContext.response(), resource, includeEvents, body);
//			} else if (ePattern.matcher(resource).matches()) {
//				khronos.sendEdge(routingContext.response(), resource, includeEvents, body);
//			} else if (vtPattern.matcher(resource).matches()) {
//				khronos.sendVertexEvent(routingContext.response(), resource, body);
//			} else if (vpPattern.matcher(resource).matches()) {
//				khronos.sendVertexEvent(routingContext.response(), resource, body);
//			} else if (etPattern.matcher(resource).matches()) {
//				khronos.sendEdgeEvent(routingContext.response(), resource, body);
//			} else if (epPattern.matcher(resource).matches()) {
//				khronos.sendEdgeEvent(routingContext.response(), resource, body);
//			} else {
//				Server.logger.error("GET /:resource: no resource pattern matched");
//				response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
//						.end(new JsonObject().put("error", "no resource pattern matched").toString());
//			}
//		});
//
//	}
//

//
//	static void registerPatchElementRouter(Router router) {
//		// patch == update
//		router.patch("chronoweb/:resource").handler(routingContext -> {
//			HttpServerResponse response = routingContext.response().setChunked(true);
//			String body = routingContext.getBodyAsString();
//			String resource = routingContext.pathParam("resource");
//			if (vPattern.matcher(resource).matches()) {
//				khronos.patchVertex(response, resource, body);
//			} else if (ePattern.matcher(resource).matches()) {
//				khronos.patchEdge(response, resource, body);
//			} else if (vtPattern.matcher(resource).matches()) {
//				khronos.patchTimestampVertexEvent(response, resource, body);
//			} else if (vpPattern.matcher(resource).matches()) {
//				khronos.patchTimeperiodVertexEvent(response, resource, body);
//			} else if (etPattern.matcher(resource).matches()) {
//				khronos.patchTimestampEdgeEvent(response, resource, body);
//			} else if (epPattern.matcher(resource).matches()) {
//				khronos.patchTimeperiodEdgeEvent(response, resource, body);
//			} else {
//				Server.logger.error("PATCH /:resource: no resource pattern matched");
//				response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
//						.end(new JsonObject().put("error", "no resource pattern matched").toString());
//			}
//
//		});
//	}
//
//	/**
//	 * v -> v
//	 * 
//	 * @param router
//	 */
//	private void registerGetAdjacentVerticesRouter(Router router) {
//		router.get("chronoweb/:resource/adjacentVertices").handler(routingContext -> {
//			HttpServerResponse response = routingContext.response().setChunked(true);
//			String body = routingContext.getBodyAsString();
//			String resource = routingContext.pathParam("resource");
//
//			if (vPattern.matcher(resource).matches()) {
//				khronos.sendAdjacentVertices(response, resource, routingContext.queryParams(), body);
//			} else {
//				Server.logger.error("GET /:resource/adjacentVertices: resource should be a vertex id");
//				response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
//						.end(new JsonObject().put("error", "resource should be a vertex id").toString());
//			}
//		});
//
//	}
//
//	/**
//	 * v -> e
//	 * 
//	 * @param router
//	 */
//	private void registerGetIncidentEdgesRouter(Router router) {
//		router.get("chronoweb/:resource/incidentEdges").handler(routingContext -> {
//			HttpServerResponse response = routingContext.response().setChunked(true);
//			String body = routingContext.getBodyAsString();
//			String resource = routingContext.pathParam("resource");
//
//			if (vPattern.matcher(resource).matches()) {
//				khronos.sendIncidentEdges(response, resource, routingContext.queryParams(), body);
//			} else {
//				Server.logger.error("GET /:resource/adjacentVertices: resource should be a vertex id");
//				response.putHeader("content-type", "*/json; charset=utf-8").setStatusCode(400)
//						.end(new JsonObject().put("error", "resource should be a vertex id").toString());
//			}
//		});
//
//	}
}
