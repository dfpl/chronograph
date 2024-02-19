package org.dfpl.chronograph.common;

import java.util.Collection;

import com.tinkerpop.blueprints.Element;

import io.vertx.core.json.JsonArray;

public class Util {
	public static JsonArray toJsonArrayOfIDs(Collection<? extends Element> edges) {
		JsonArray array = new JsonArray();
		edges.parallelStream().forEach(e -> array.add(e.getId()));
		return array;
	}
}
