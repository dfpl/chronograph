package org.dfpl.chronograph.common;

@FunctionalInterface
public interface RetainEventBinaryOperator<E> {
	public E apply(Object gamma, E e1, E e2);
}
