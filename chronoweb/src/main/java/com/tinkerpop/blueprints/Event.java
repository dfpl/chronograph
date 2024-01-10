package com.tinkerpop.blueprints;

import java.util.Map;
import java.util.Set;

public interface Event {

	public String getId();
	
	public String getElementId();
	
	Map<String, Object> getProperties();
	
	public <T> T getProperty(String key);
	
	public Set<String> getPropertyKeys();
	
	public void setProperty(String key, Object value);
	
	public <T> T removeProperty(String key);
	
	public Long getTime();

	public Element getElement();

}
