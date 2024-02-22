package org.dfpl.chronograph.khronos.manipulation.memory;

import org.dfpl.chronograph.common.Event;

import com.tinkerpop.blueprints.Element;

public class MChronoEvent extends MChronoElement implements Event, Comparable<Event> {

	protected Element element;
	protected Long time;

	@Override
	public Long getTime() {
		return time;
	}

	@Override
	public Object getElement() {
		return element;
	}

	@Override
	public int compareTo(Event o) {
		return time.compareTo(o.getTime());
	}

}
