package org.dfpl.chronograph.khronos.manipulation.memory;

import org.dfpl.chronograph.common.Event;

import com.tinkerpop.blueprints.Element;

public class MChronoEvent extends MChronoElement implements Event, Comparable<Event> {

	protected Element element;
	protected Long time;
	
	@Override
	public String getElementId() {
		return element.getId();
	}

	@Override
	public Long getTime() {
		return time;
	}

	@Override
	public Element getElement() {
		return element;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(Event o) {
		return time.compareTo(o.getTime());
	}

}
