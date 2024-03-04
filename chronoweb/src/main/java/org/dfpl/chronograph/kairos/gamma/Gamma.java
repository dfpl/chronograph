package org.dfpl.chronograph.kairos.gamma;

import java.util.Map;

import io.vertx.core.json.JsonObject;

/**
 * A {@code Gamma} contains the temporal information diffusion results of a source to other vertices
 * 
 * @param <K> the data type of the source
 * @param <E> the data type of the temporal information diffusion result
 */
public interface Gamma<K, E> {
	/**
	 * Return the temporal information diffusion result
	 * 
	 * @param to the destination vertex
	 * @return the temporal information diffusion result
	 */
	public E getElement(K to);
	
	/**
	 * Set the temporal information diffusion result
	 * 
	 * @param to the destination vertex
	 * @param element the temporal information diffusion result to set
	 */
	public void setElement(K to, GammaElement<E> element);
	
	/**
	 * Convert this Gamma into a {@code Map}
	 * 
	 * @param setDefaultToNull set the default value to null if set to true
	 * @return the map representation of this Gamma
	 */
	public Map<K, E> toMap(boolean setDefaultToNull);
	
	/**
	 * Convert this Gamma into a JSON
	 * 
	 * @param setDefaultToNull set the default value to null if set to true
	 * @return the JSON representation of this Gamma
	 */
	public JsonObject toJson(boolean setDefaultToNull);
}
