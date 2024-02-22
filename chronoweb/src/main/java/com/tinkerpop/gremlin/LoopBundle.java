package com.tinkerpop.gremlin;

import java.util.Collection;
import java.util.Map;

public class LoopBundle<T> {
	private Collection<T> traverserSet;
	private Map<Object, Object> currentPath;
	private int loopCount;

	public LoopBundle(Collection<T> traverserSet, Map<Object, Object> currentPath, int loopCount) {
		super();
		this.traverserSet = traverserSet;
		this.currentPath = currentPath;
		this.loopCount = loopCount;
	}

	public Collection<T> getTraverserSet() {
		return traverserSet;
	}

	public void setTraverser(Collection<T> traverserSet) {
		this.traverserSet = traverserSet;
	}

	public Map<Object, Object> getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(Map<Object, Object> currentPath) {
		this.currentPath = currentPath;
	}

	public int getLoopCount() {
		return loopCount;
	}

	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}

}
