package net.aeten.core;

import net.aeten.core.event.EventData;
import net.aeten.core.event.Handler;

public interface Model<M extends Model <M, F>, F extends Enum <F>> {
	void addObserver (Handler <EventData <M, F>> observer);

	void removeObserver (Handler <EventData <M, F>> observer);

	<T> void set (	F field,
						T value);

	<T> T get (F field);
}
