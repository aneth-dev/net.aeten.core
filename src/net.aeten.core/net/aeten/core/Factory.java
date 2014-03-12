package net.aeten.core;

public interface Factory<T, C> {
	public T create (C context);
}
