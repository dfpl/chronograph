package org.dfpl.chronograph.khronos.manipulation.memory;

import java.util.Set;

import org.bson.Document;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;

public class MChronoElement implements Element {

	protected Graph g;
	protected String id;
	protected Document properties;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Document getProperties() {
		return properties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key) {
		return (T) properties.get(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return this.properties.keySet();
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		return (T) properties.remove(key);
	}

	public void setProperties(Document properties, boolean isSet) {
		if (!isSet) {
			this.properties = properties;
		} else {
			for (String key : properties.keySet()) {
				this.properties.put(key, properties.get(key));
			}
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return id.equals(obj.toString());
	}

	@Override
	public Graph getGraph() {
		return g;
	}

}
