package org.dfpl.chronograph.common;

import com.tinkerpop.blueprints.Element;

import io.vertx.core.json.JsonArray;

public class Util {
	public static JsonArray toJsonArrayOfIDs(Iterable<? extends Element> edges) {
		JsonArray array = new JsonArray();
		for (Element e : edges) {
			array.add(e.getId());
		}
		return array;
	}
}
