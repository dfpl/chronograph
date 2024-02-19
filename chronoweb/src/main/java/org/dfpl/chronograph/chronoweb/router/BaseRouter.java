package org.dfpl.chronograph.chronoweb.router;

import java.util.List;

import org.dfpl.chronograph.common.TemporalRelation;

import com.tinkerpop.blueprints.Graph;

import io.vertx.ext.web.RoutingContext;

public abstract class BaseRouter {
	protected Graph graph;

	public BaseRouter(Graph graph) {
		this.graph = graph;
	}

	protected static void sendResult(RoutingContext routingContext, String contentType, String message, int code) {
		routingContext.response().putHeader("content-type", contentType).setStatusCode(code).end(message);
	}

	protected static void sendResult(RoutingContext routingContext, String message, int code) {
		routingContext.response().setStatusCode(code).end(message);
	}

	protected static void sendResult(RoutingContext routingContext, int code) {
		routingContext.response().setStatusCode(code).end();
	}

	protected static Boolean getBooleanURLParameter(RoutingContext routingContext, String key) {
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

	protected static Long getLongURLParameter(RoutingContext routingContext, String key) {
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

	protected static TemporalRelation getTemporalRelationURLParameter(RoutingContext routingContext, String key) {
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

	protected static String getStringURLParameter(RoutingContext routingContext, String key) {
		List<String> list = routingContext.queryParam(key);
		if (list.isEmpty())
			return null;
		else {
			return list.get(0);
		}
	}
}
