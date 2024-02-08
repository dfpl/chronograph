package org.dfpl.chronograph.chronoweb;

import io.vertx.core.json.JsonObject;

public class MessageBuilder {

	public static JsonObject propertySyntaxException = new JsonObject().put("type", "propertySyntaxException")
			.put("status", 400);
	
	public static JsonObject missingRequiredURLParameterException = new JsonObject().put("type", "missingRequiredURLParameterException")
			.put("status", 400);

	public static JsonObject invalidVertexIDException = new JsonObject().put("type", "invalidVertexIDException")
			.put("reason",
					"the syntax of resource identifier should be ^[^|_]+$")
			.put("status", 400);
	public static JsonObject invalidVertexEventIDException = new JsonObject().put("type", "invalidVertexEventIDException")
			.put("reason",
					"the syntax of resource identifier should be ^[^|_]+_[0-9]+$")
			.put("status", 400);
	
	public static JsonObject invalidResourceIDException = new JsonObject().put("type", "invalidResourceIDException")
			.put("reason",
					"the syntax of resource identifier should be one of ^[^|_]+$, ^[^|_]+\\|[^|_]+\\|[^|_]+$, ^[^|_]+_[0-9]+$, ^[^|_]+\\|[^|_]+\\|[^|_]+_[0-9]+$")
			.put("status", 400);
	
	public static JsonObject resourceNotFoundException = new JsonObject().put("type", "resourceNotFoundException")
			.put("reason",
					"The resource is not found in a graph")
			.put("status", 404);

	public static String getPropertySyntaxException(String reason) {
		JsonObject error = propertySyntaxException.copy();
		error.put("reason", reason);
		return error.toString();
	}
	public static String getMissingRequiredURLParameterException(String reason) {
		JsonObject error = missingRequiredURLParameterException.copy();
		error.put("reason", "required parameters " + reason + " are missed");
		return error.toString();
	}
}
