package org.dfpl.chronograph.kairos.gamma;

import java.util.Map;

import io.vertx.core.json.JsonObject;

public interface Gamma<K, E> {
	public E getElement(K to);

	public void setElement(K to, GammaElement<E> element);

	public Map<K, E> toMap(boolean setDefaultToNull);
	
	public JsonObject toJson(boolean setDefaultToNull);
}
