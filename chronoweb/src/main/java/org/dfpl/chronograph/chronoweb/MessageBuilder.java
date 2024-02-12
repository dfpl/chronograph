package org.dfpl.chronograph.chronoweb;

import io.vertx.core.json.JsonObject;

public class MessageBuilder {

	static JsonObject propertySyntaxException = new JsonObject().put("type", "propertySyntaxException").put("status",
			400);

	static JsonObject missingRequiredURLParameterException = new JsonObject()
			.put("type", "missingRequiredURLParameterException").put("status", 400);

	public static String invalidVertexIDException = new JsonObject().put("type", "invalidVertexIDException")
			.put("reason", "the syntax of resource identifier should be ^[^|_]+$").put("status", 400).toString();
	public static String invalidVertexEventIDException = new JsonObject().put("type", "invalidVertexEventIDException")
			.put("reason", "the syntax of resource identifier should be ^[^|_]+_[0-9]+$").put("status", 400).toString();

	public static String invalidResourceIDException = new JsonObject().put("type", "invalidResourceIDException").put(
			"reason",
			"the syntax of resource identifier should be one of ^[^|_]+$, ^[^|_]+\\|[^|_]+\\|[^|_]+$, ^[^|_]+_[0-9]+$, ^[^|_]+\\|[^|_]+\\|[^|_]+_[0-9]+$")
			.put("status", 400).toString();

	public static String invalidTemporalRelationSyntaxException = new JsonObject()
			.put("type", "invalidTemporalRelationSyntaxException")
			.put("reason", "temporal relation should be one of 'isAfter', 'isBefore', 'cotemporal'").put("status", 400)
			.toString();

	public static String invalidTimeSynta1xException = new JsonObject()
			.put("type", "invalidTimeSyntaxException")
			.put("reason", "the value of time should be from -9223372036854775808 to 9223372036854775807").put("status", 400)
			.toString();
	
	public static String resourceNotFoundException = new JsonObject().put("type", "resourceNotFoundException")
			.put("reason", "The resource is not found in a graph").put("status", 404).toString();

	public static String noSuchProgramException = new JsonObject().put("type", "noSuchProgramException")
			.put("reason", "The program is not available").put("status", 404).toString();
	
	public static String sourceAlreadySubscribedException = new JsonObject().put("type", "sourceAlreadySubscribedException")
			.put("reason", "The source is already subscribed with the program").put("status", 409).toString();
	
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
