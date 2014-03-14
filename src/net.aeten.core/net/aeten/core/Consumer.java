package net.aeten.core;

public interface Consumer<T> {
	void consume(T element);
}
