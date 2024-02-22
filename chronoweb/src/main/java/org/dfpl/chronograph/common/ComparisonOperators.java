package org.dfpl.chronograph.common;

/**
 * Full Comparator
 * 
 * @author jack
 *
 */
public enum ComparisonOperators {
	/**
	 * Greater than
	 */
	$gt,
	/**
	 * Less than
	 */
	$lt,
	/**
	 * Equal to
	 */
	$eq,
	/**
	 * Greater than or equal to
	 */
	$gte,
	/**
	 * Less than or equal to
	 */
	$lte,
	/**
	 * Not equal to
	 */
	$ne,
	/**
	 * In collection
	 */
	$in,
	/**
	 * Not in collection
	 */
	$nin, $exists, $max, $min
}
