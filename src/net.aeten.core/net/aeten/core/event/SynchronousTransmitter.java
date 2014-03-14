package net.aeten.core.event;

import java.util.concurrent.Future;

/**
 *
 * @author Thomas Pérennou
 */
class SynchronousTransmitter<Event, Data extends EventData<?, Event>> implements Transmitter<Data> {

	private final Handler<Data> eventHandler;
	private final Event[] events;

	public SynchronousTransmitter(Handler<Data> eventHandler, Event... events) {
		this.eventHandler = eventHandler;
		this.events = events;
	}

	public Future<Data> transmit(Data data) {
		for (Event registredEvent: this.events) {
			if (registredEvent.equals(data.getEvent())) {
				this.eventHandler.handleEvent(data);
			}
		}
		return new FutureDone<Data>(data);
	}
}
