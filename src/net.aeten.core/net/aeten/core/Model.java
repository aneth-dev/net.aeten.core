package net.aeten.core;

import net.aeten.core.event.EventData;
import net.aeten.core.event.Handler;


public interface Model<M, F extends Enum<F>> {
	void addObserver(Handler<EventData<M, F>> observer);
	void removeObserver(Handler<EventData<M, F>> observer);
}
