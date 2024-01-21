package org.dfpl.chronograph.kairos.gamma;

public interface GammaTable<K, E> {

	public E get(K from, K to);

	public void set(K from, K to, GammaElement<E> element);

	public Gamma<K, GammaElement<E>> getGamma(K from);

	public void setGamma(K from, Gamma<K, GammaElement<E>> gamma);

}
