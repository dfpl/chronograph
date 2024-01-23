package org.dfpl.chronograph.chronoweb.router.memory;

import java.io.IOException;
import java.util.List;

import org.dfpl.chronograph.chronoweb.Server;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.khronos.memory.dataloader.DataLoader;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoEdge;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoEdgeEvent;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoGraph;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertex;
import org.dfpl.chronograph.khronos.memory.manipulation.ChronoVertexEvent;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import static org.dfpl.chronograph.chronoweb.Server.*;

public class ManipulationRouter {

	private Graph graph;

	public ManipulationRouter(Graph graph) {
		this.graph = graph;
	}

	private static void sendResult(RoutingContext routingContext, String contentType, String message, int code) {
		routingContext.response().putHeader("content-type", contentType).setStatusCode(code).end(message);
	}

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

	private static Long getLongURLParameter(RoutingContext routingContext, String key) {
		List<String> list = routingContext.queryParam(key);
		if (list.isEmpty())
			return null;
		else {
			try {
				return Long.parseLong(list.get(0));
			} catch (Exception e) {
				return null;
			}
		}
	}

	private static TemporalRelation getTemporalRelationURLParameter(RoutingContext routingContext, String key) {
		List<String> list = routingContext.queryParam(key);
		if (list.isEmpty())
			return null;
		else {
			String tr = list.get(0);
			if (tr.equals("isAfter"))
				return TemporalRelation.isAfter;
			else if (tr.equals("isBefore"))
				return TemporalRelation.isBefore;
			else if (tr.equals("cotemporal"))
				return TemporalRelation.cotemporal;
			else
				throw new IllegalArgumentException(key + " should be one of 'isAfter', 'isBefore', 'cotemporal'");
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

	public void registerAddElementRouter(Router router, EventBus eventBus) {
		router.post("/chronoweb/graph/:resource").consumes("application/json").handler(routingContext -> {
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
					ChronoVertex v = null;
					if (graph.getVertex(resource) == null) {
						v = (ChronoVertex) graph.addVertex(resource);
						eventBus.send("addVertex", v.toJsonObject(false));
					} else {
						v = (ChronoVertex) graph.addVertex(resource);
					}
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

			} else if (vtPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					String vertexID = arr[0];
					long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.addVertex(vertexID);
					ChronoVertexEvent ve = (ChronoVertexEvent) v.addEvent(time);
					ve.setProperties(properties, isSet);
					sendResult(routingContext, "application/json",
							ve.toJsonObject(includeProperties == null ? false : includeProperties).toString(), 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
			} else if (etPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					long time = Long.parseLong(arr[1]);
					String[] arr2 = arr[0].split("\\|");
					ChronoEdge e = (ChronoEdge) graph.addEdge(graph.addVertex(arr2[0]), graph.addVertex(arr2[2]),
							arr2[1]);
					ChronoEdgeEvent ee = (ChronoEdgeEvent) e.addEvent(time);
					ee.setProperties(properties, isSet);
					sendResult(routingContext, "application/json",
							ee.toJsonObject(includeProperties == null ? false : includeProperties).toString(), 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;

			} else {
				sendResult(routingContext, 406);
				return;
			}

		});

		Server.logger.info("POST /chronoweb/graph/:resource router added");
	}

	public void registerGetElementRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:resource").handler(routingContext -> {
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
			} else if (Server.vtPattern.matcher(resource).matches()) {
				String[] arr = resource.split("\\_");
				String vertexID = arr[0];
				long time = Long.parseLong(arr[1]);
				ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
				if (v != null) {
					ChronoVertexEvent ve = (ChronoVertexEvent) v.getEvent(time, TemporalRelation.cotemporal);
					if (ve != null) {
						sendResult(routingContext, "application/json",
								ve.toJsonObject(includeProperties == null ? true : includeProperties).toString(), 200);
						return;
					}
				}
				sendResult(routingContext, 404);
				return;
			} else if (etPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					long time = Long.parseLong(arr[1]);
					String[] arr2 = arr[0].split("\\|");
					ChronoEdge e = (ChronoEdge) graph.addEdge(graph.addVertex(arr2[0]), graph.addVertex(arr2[2]),
							arr2[1]);
					if (e != null) {
						ChronoEdgeEvent ee = (ChronoEdgeEvent) e.getEvent(time);
						if (ee != null) {
							sendResult(routingContext, "application/json",
									ee.toJsonObject(includeProperties == null ? true : includeProperties).toString(),
									200);
							return;
						}
					}
					sendResult(routingContext, 404);
					return;
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:resource router added");
	}

	public void registerGetElementsRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph").handler(routingContext -> {
			String target = getStringURLParameter(routingContext, "target");
			target = target == null ? "vertices" : target;
			if (target.equals("vertices")) {
				JsonArray result = new JsonArray(graph.getVertices().parallelStream().map(v -> v.getId()).toList());
				sendResult(routingContext, "application/json", result.toString(), 200);
				return;
			} else {
				JsonArray result = new JsonArray(graph.getEdges().parallelStream().map(e -> e.getId()).toList());
				sendResult(routingContext, "application/json", result.toString(), 200);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph router added");
	}

	public void registerGetIncidentEdgesRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:vertexID/outE").handler(routingContext -> {
			String vertexID = routingContext.pathParam("vertexID");
			List<String> labels = routingContext.queryParam("label");
			if (Server.vPattern.matcher(vertexID).matches()) {
				ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
				if (v != null) {
					sendResult(routingContext, "application/json",
							ChronoEdge.toJsonArrayOfIDs(v.getEdges(Direction.OUT, labels)).toString(), 200);
				} else
					sendResult(routingContext, 404);
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexID/outE router added");

		router.get("/chronoweb/graph/:vertexID/inE").handler(routingContext -> {
			String vertexID = routingContext.pathParam("vertexID");
			List<String> labels = routingContext.queryParam("label");
			if (Server.vPattern.matcher(vertexID).matches()) {
				ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
				if (v != null) {
					sendResult(routingContext, "application/json",
							ChronoEdge.toJsonArrayOfIDs(v.getEdges(Direction.IN, labels)).toString(), 200);
				} else
					sendResult(routingContext, 404);
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexID/inE router added");
	}

	public void registerGetAdjacentVerticesRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:vertexID/out").handler(routingContext -> {
			String vertexID = routingContext.pathParam("vertexID");
			List<String> labels = routingContext.queryParam("label");
			if (Server.vPattern.matcher(vertexID).matches()) {
				ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
				if (v != null) {
					sendResult(routingContext, "application/json",
							ChronoVertex.toJsonArrayOfIDs(v.getVertices(Direction.OUT, labels)).toString(), 200);
				} else
					sendResult(routingContext, 404);
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexID/out router added");

		router.get("/chronoweb/graph/:vertexID/in").handler(routingContext -> {
			String vertexID = routingContext.pathParam("vertexID");
			List<String> labels = routingContext.queryParam("label");
			if (Server.vPattern.matcher(vertexID).matches()) {
				ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
				if (v != null) {
					sendResult(routingContext, "application/json",
							ChronoEdge.toJsonArrayOfIDs(v.getEdges(Direction.IN, labels)).toString(), 200);
				} else
					sendResult(routingContext, 404);
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexID/in router added");
	}

	public void registerDeleteGraphRouter(Router router, EventBus eventBus) {
		router.delete("/chronoweb/graph").handler(routingContext -> {
			graph.clear();
			sendResult(routingContext, 200);
		});

		Server.logger.info("DELETE /chronoweb/graph router added");
	}

	public void registerRemoveElementRouter(Router router, EventBus eventBus) {
		router.delete("/chronoweb/graph/:resource").handler(routingContext -> {
			String resource = routingContext.pathParam("resource");
			if (Server.vPattern.matcher(resource).matches()) {
				try {
					ChronoVertex v = (ChronoVertex) graph.getVertex(resource);
					if (v == null) {
						sendResult(routingContext, 404);
						return;
					}
					graph.removeVertex(v);
					sendResult(routingContext, 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else if (ePattern.matcher(resource).matches()) {
				try {
					ChronoEdge e = (ChronoEdge) graph.getEdge(resource);
					if (e == null) {
						sendResult(routingContext, 404);
						return;
					}
					graph.removeEdge(e);
					sendResult(routingContext, 200);
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else if (Server.vtPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					String vertexID = arr[0];
					long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.getVertex(vertexID);
					if (v != null) {
						ChronoVertexEvent ve = (ChronoVertexEvent) v.getEvent(time, TemporalRelation.cotemporal);
						if (ve != null) {
							v.removeEvents(time, TemporalRelation.cotemporal);
							sendResult(routingContext, 200);
							return;
						}
					}
					sendResult(routingContext, 404);
					return;
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else if (etPattern.matcher(resource).matches()) {
				try {
					String[] arr = resource.split("\\_");
					long time = Long.parseLong(arr[1]);
					String[] arr2 = arr[0].split("\\|");
					ChronoEdge e = (ChronoEdge) graph.addEdge(graph.addVertex(arr2[0]), graph.addVertex(arr2[2]),
							arr2[1]);
					if (e != null) {
						ChronoEdgeEvent ee = (ChronoEdgeEvent) e.getEvent(time);
						if (ee != null) {
							e.removeEvents(time, TemporalRelation.cotemporal);
							sendResult(routingContext, 200);
							return;
						}
					}
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

		Server.logger.info("DELETE /chronoweb/graph/:resource router added");
	}

	public void registerGetEventsRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:resource/events").handler(routingContext -> {
			String resource = routingContext.pathParam("resource");
			Long time = null;
			TemporalRelation tr = null;
			try {
				time = getLongURLParameter(routingContext, "time");
				tr = getTemporalRelationURLParameter(routingContext, "temporalRelation");
			} catch (Exception e) {
				sendResult(routingContext, e.getMessage(), 404);
				return;
			}
			if (Server.vPattern.matcher(resource).matches()) {
				try {
					Boolean awareOutEvents = false;
					Boolean awareInEvents = false;
					try {
						Boolean b1 = getBooleanURLParameter(routingContext, "awareOutEvents");
						awareOutEvents = b1 == null ? false : b1;
						Boolean b2 = getBooleanURLParameter(routingContext, "awareInEvents");
						awareInEvents = b2 == null ? false : b2;
					} catch (Exception e) {
						sendResult(routingContext, e.getMessage(), 404);
						return;
					}
					ChronoVertex v = (ChronoVertex) graph.getVertex(resource);
					if (time == null || tr == null) {
						sendResult(routingContext, "application/json",
								ChronoVertexEvent.toJsonArray(
										v.getEvents(awareOutEvents.booleanValue(), awareInEvents.booleanValue()))
										.toString(),
								200);
						return;
					} else {
						sendResult(routingContext, "application/json", ChronoVertexEvent.toJsonArray(
								v.getEvents(time, tr, awareOutEvents.booleanValue(), awareInEvents.booleanValue()))
								.toString(), 200);
						return;
					}
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else if (ePattern.matcher(resource).matches()) {
				try {
					ChronoEdge e = (ChronoEdge) graph.getEdge(resource);
					if (time == null || tr == null) {
						sendResult(routingContext, "application/json",
								ChronoEdgeEvent.toJsonArray(e.getEvents()).toString(), 200);
						return;
					} else {
						sendResult(routingContext, "application/json",
								ChronoEdgeEvent.toJsonArray(e.getEvents(time, tr)).toString(), 200);
						return;
					}
				} catch (IllegalArgumentException e) {
					sendResult(routingContext, 406);
				}
				return;
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:resource/events router added");
	}

	public void registerGetIncidentEdgeEventsRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:vertexEventID/outEe").handler(routingContext -> {
			String vertexEventID = routingContext.pathParam("vertexEventID");
			TemporalRelation tr = getTemporalRelationURLParameter(routingContext, "temporalRelation");
			String label = getStringURLParameter(routingContext, "label");
			if (tr == null || label == null) {
				sendResult(routingContext, 406);
				return;
			}
			if (Server.vtPattern.matcher(vertexEventID).matches()) {
				String[] arr = vertexEventID.split("\\_");
				try {
					Long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.getVertex(arr[0]);
					VertexEvent ve = v.getEvent(time);
					sendResult(routingContext, "application/json",
							ChronoEdgeEvent.toJsonArray(ve.getEdgeEvents(Direction.OUT, tr, label)).toString(), 200);
					return;
				} catch (Exception e) {
					sendResult(routingContext, 404);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexEventID/outEe router added");

		router.get("/chronoweb/graph/:vertexEventID/inEe").handler(routingContext -> {
			String vertexEventID = routingContext.pathParam("vertexEventID");
			TemporalRelation tr = getTemporalRelationURLParameter(routingContext, "temporalRelation");
			String label = getStringURLParameter(routingContext, "label");
			if (tr == null || label == null) {
				sendResult(routingContext, 406);
				return;
			}
			if (Server.vtPattern.matcher(vertexEventID).matches()) {
				String[] arr = vertexEventID.split("\\_");
				try {
					Long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.getVertex(arr[0]);
					VertexEvent ve = v.getEvent(time);
					sendResult(routingContext, "application/json",
							ChronoEdgeEvent.toJsonArray(ve.getEdgeEvents(Direction.IN, tr, label)).toString(), 200);
					return;
				} catch (Exception e) {
					sendResult(routingContext, 404);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexEventID/inEe router added");
	}

	public void registerGetAdjacentVertexEventsRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/graph/:vertexEventID/oute").handler(routingContext -> {
			String vertexEventID = routingContext.pathParam("vertexEventID");
			TemporalRelation tr = getTemporalRelationURLParameter(routingContext, "temporalRelation");
			String label = getStringURLParameter(routingContext, "label");
			if (tr == null || label == null) {
				sendResult(routingContext, 406);
				return;
			}
			if (Server.vtPattern.matcher(vertexEventID).matches()) {
				String[] arr = vertexEventID.split("\\_");
				try {
					Long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.getVertex(arr[0]);
					VertexEvent ve = v.getEvent(time);
					sendResult(routingContext, "application/json",
							ChronoVertexEvent.toJsonArray(ve.getVertexEvents(Direction.OUT, tr, label)).toString(),
							200);
					return;
				} catch (Exception e) {
					sendResult(routingContext, 404);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexEventID/oute router added");

		router.get("/chronoweb/graph/:vertexEventID/ine").handler(routingContext -> {
			String vertexEventID = routingContext.pathParam("vertexEventID");
			TemporalRelation tr = getTemporalRelationURLParameter(routingContext, "temporalRelation");
			String label = getStringURLParameter(routingContext, "label");
			if (tr == null || label == null) {
				sendResult(routingContext, 406);
				return;
			}
			if (Server.vtPattern.matcher(vertexEventID).matches()) {
				String[] arr = vertexEventID.split("\\_");
				try {
					Long time = Long.parseLong(arr[1]);
					ChronoVertex v = (ChronoVertex) graph.getVertex(arr[0]);
					VertexEvent ve = v.getEvent(time);
					sendResult(routingContext, "application/json",
							ChronoVertexEvent.toJsonArray(ve.getVertexEvents(Direction.IN, tr, label)).toString(), 200);
					return;
				} catch (Exception e) {
					sendResult(routingContext, 404);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("GET /chronoweb/graph/:vertexEventID/ine router added");
	}

	public void registerGetDatasetsRouter(Router router, EventBus eventBus) {
		router.get("/chronoweb/datasets").handler(routingContext -> {
			sendResult(routingContext, "application/json", new JsonArray(Server.datasetList).toString(), 200);
		});

		Server.logger.info("GET /chronoweb/datasets router added");
	}

	public void registerLoadDatasetRouter(Router router, EventBus eventBus) {
		router.post("/chronoweb/datasets/:dataset").handler(routingContext -> {
			String dataset = routingContext.pathParam("dataset");
			if (dataset.equals("EgoFacebook")) {
				try {
					ChronoGraph newGraph = new ChronoGraph();
					DataLoader.EgoFacebook("d:\\dataset", newGraph, "hasFriend");
					synchronized (graph) {
						graph = newGraph;
					}
					sendResult(routingContext, 200);
					return;
				} catch (IOException e) {
					sendResult(routingContext, 500);
					return;
				}
			} else if (dataset.equals("EUEmailCommunicationNetwork")) {
				try {
					ChronoGraph newGraph = new ChronoGraph();
					DataLoader.EUEmailCommunicationNetwork("d:\\dataset", newGraph, "sendEmail");
					synchronized (graph) {
						graph = newGraph;
					}
					sendResult(routingContext, 200);
					return;
				} catch (IOException e) {
					sendResult(routingContext, 500);
					return;
				}
			} else if (dataset.equals("sx-mathoverflow")) {
				try {
					ChronoGraph newGraph = new ChronoGraph();
					DataLoader.SxMathOverflow("c:\\dataset", newGraph, "c");
					synchronized (graph) {
						graph = newGraph;
					}
					sendResult(routingContext, 200);
					return;
				} catch (IOException e) {
					sendResult(routingContext, 500);
					return;
				}
			} else {
				sendResult(routingContext, 406);
				return;
			}
		});

		Server.logger.info("POST /chronoweb/datasets/:dataset router added");
	}
}