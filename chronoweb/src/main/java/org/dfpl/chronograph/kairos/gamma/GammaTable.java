package org.dfpl.chronograph.kairos.gamma;

import java.util.function.BiPredicate;

public interface GammaTable<K, E> {

	public E get(K from, K to);

	public void set(K from, K to, GammaElement<E> element);

	public void setIfExists(K ifExists, K set, GammaElement<E> newElement, BiPredicate<E, E> setNew);

	public Gamma<K, GammaElement<E>> getGamma(K from);

	public void setGamma(K from, Gamma<K, GammaElement<E>> gamma);

	public void clear();

}
