package org.dfpl.chronograph.khronos.manipulation.persistent;

import org.dfpl.chronograph.common.Event;

public class PChronoEvent extends PChronoElement implements Event, Comparable<Event> {

	protected String element;
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
