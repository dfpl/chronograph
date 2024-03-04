package org.dfpl.chronograph.kairos.gamma;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * The {@code GammaTable} contains the temporal information diffusion results
 * of a set of sources
 * 
 * @param <K> the data type of the source
 * @param <E> the data type of the temporal information diffusion results
 */
public interface GammaTable<K, E> {

	/**
	 * Return the sources
	 * 
	 * @return the sources
	 */
	public Set<K> getSources();

	/**
	 * Return the temporal information diffusion result of a source and its
	 * destination
	 * 
	 * @param from the source
	 * @param to   the destination
	 * 
	 * @return the temporal information diffusion result
	 */
	public E get(K from, K to);

	/**
	 * Set the temporal information result of a source to a destination
	 * 
	 * @param from    the source
	 * @param to      the destination
	 * @param element the temporal information to be set
	 */
	public void set(K from, K to, GammaElement<E> element);

	/**
	 * TODO
	 * 
	 * @param sources
	 * @param check
	 * @param testCheck
	 * @param update
	 * @param newValue
	 * @param testUpdate
	 */
	public void update(Set<K> sources, K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);
	
	/**
	 * TODO
	 * 
	 * @param check
	 * @param testCheck
	 * @param update
	 * @param newValue
	 * @param testUpdate
	 */
	public void update(K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);
	
	/**
	 * TODO
	 * 
	 * @param sources
	 * @param check
	 * @param testCheck
	 * @param update
	 * @param newValue
	 * @param testUpdate
	 */
	public void append(Set<K> sources, K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);
	
	/**
	 * TODO 
	 * @param check
	 * @param testCheck
	 * @param update
	 * @param newValue
	 * @param testUpdate
	 */
	public void append(K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate);

	/**
	 * Return the temporal information results from a source
	 * 
	 * @param from the source
	 * @return the temporal information diffusion results
	 */
	public Gamma<K, E> getGamma(K from);

	/**
	 * Clear the Gamma Table
	 */
	public void clear();

	/**
	 * Add a source and its temporal information diffusion result
	 * 
	 * @param source  the source
	 * @param element the temporal information diffusion result
	 */
	public void addSource(K source, GammaElement<E> element);

	/**
	 * Print the Gamma Table
	 */
	public void print();

}
