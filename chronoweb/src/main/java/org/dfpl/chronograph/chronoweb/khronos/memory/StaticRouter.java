package org.dfpl.chronograph.chronoweb.khronos.memory;

import java.util.List;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoEdge;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoGraph;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertex;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import static org.dfpl.chronograph.chronoweb.Server.*;

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

	public static void registerAddElementRouter(Router router, ChronoGraph graph) {
		router.post("/chronoweb/:resource").consumes("application/json").handler(routingContext -> {
			String resource = routingContext.pathParam("resource");
			String propertiesParameter = getStringURLParameter(routingContext, "properties");
			boolean isSet = propertiesParameter == null || propertiesParameter.equals("set") ? true : false;
			Boolean includeProperties = getBooleanURLParameter(routingContext, "includeProperties");
			JsonObject properties = null;
			try {
				properties = routingContext.body().asJsonObject();
			} catch (Exception e) {
				sendResult(routingContext, "text/plain", e.getMessage(), 400);
				return;
			}
			if (properties == null)
				properties = new JsonObject();

			if (Server.vPattern.matcher(resource).matches()) {
				try {
					ChronoVertex v = (ChronoVertex) graph.addVertex(resource);
					v.setProperties(properties, isSet);
					sendResult(routingContext, "application/json",
							v.toJsonObject(includeProperties == null ? false : includeProperties).toString(), 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else if (ePattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\|");
					ChronoEdge e = (ChronoEdge) graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[2]), arr[1]);
					e.setProperties(properties, isSet);
					sendResult(routingContext, "application/json",
							e.toJsonObject(includeProperties == null ? false : includeProperties).toString(), 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;

			} else {
				sendResult(routingContext, 406);
				return;
			}

		});

		Server.logger.info("POST /chronoweb/:resource router added");
	}

	public static void registerGetElementRouter(Router router, ChronoGraph graph) {
		router.get("/chronoweb/:resource").handler(routingContext -> {
			String resource = routingContext.pathParam("resource");
			Boolean includeProperties = getBooleanURLParameter(routingContext, "includeProperties");
			if (Server.vPattern.matcher(resource).matches()) {
				ChronoVertex v = (ChronoVertex) graph.getVertex(resource);
				if (v != null)
					sendResult(routingContext, "application/json",
							v.toJsonObject(includeProperties == null ? true : includeProperties).toString(), 200);
				else
					sendResult(routingContext, 404);
				return;
			} else if (ePattern.matcher(resource).matches()) {
				try {
					ChronoEdge e = (ChronoEdge) graph.getEdge(resource);
					if (e != null)
						sendResult(routingContext, "application/json",
								e.toJsonObject(includeProperties == null ? true : includeProperties).toString(), 200);
					else
						sendResult(routingContext, 404);
					return;
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/:resource router added");
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
