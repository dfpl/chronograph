package org.dfpl.chronograph.kairos.gamma;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface GammaTable<K, E> {

	public Set<K> getSources();

	public E get(K from, K to);

	public void set(K from, K to, GammaElement<E> element);

	public void update(Set<K> sources, K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);

	public void update(K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);

	public Gamma<K, E> getGamma(K from);

	public void clear();

	public void addSource(K source, GammaElement<E> element);

	public void print();

}
