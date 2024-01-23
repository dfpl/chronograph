package org.dfpl.chronograph.kairos.gamma;

import java.util.List;

public interface Gamma<K, E> {
	public E getElement(K to);

	public void setElement(K to, GammaElement<E> element);

	public List<E> toList();
}
