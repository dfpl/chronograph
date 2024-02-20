package org.dfpl.chronograph.khronos.manipulation.persistent;

import org.dfpl.chronograph.common.Event;

import com.tinkerpop.blueprints.Element;

public class PChronoEvent extends PChronoElement implements Event, Comparable<Event> {
	
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
	public int compareTo(Event o) {
		return time.compareTo(o.getTime());
	}

}
