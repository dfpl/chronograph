package org.dfpl.chronograph.common;

import java.util.Map;
import java.util.Set;

import com.tinkerpop.blueprints.Element;

/**
 * The in-memory implementation of temporal graph database.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
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
