package net.aeten.core;

public interface Predicate<T> {
	public boolean evaluate (T element);
}