package net.aeten.core;

public interface Predicate<T> {
	public boolean matches(T element);
}