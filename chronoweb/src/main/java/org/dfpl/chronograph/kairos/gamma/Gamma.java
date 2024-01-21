package org.dfpl.chronograph.kairos.gamma;

public interface Gamma<K, E> {
	public GammaElement<E> getElement(K to);

	public void setElement(K to, GammaElement<E> element);
}
