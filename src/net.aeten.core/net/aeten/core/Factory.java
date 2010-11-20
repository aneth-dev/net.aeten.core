package net.aeten.core;

public interface Factory<T> {
	public <Y extends T> T create() throws Exception;
}
