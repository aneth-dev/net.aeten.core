package net.aeten.core.event;

import java.util.concurrent.ExecutorService;


/**
 *
 * @author Thomas Pérennou
 */
public final class TransmitterFactory {

	private TransmitterFactory() {}
	
	public static final <Event, Data extends EventData<?, Event>> Transmitter<Data> synchronous(Handler<Data> eventHandler, Event... events) {
		return new SynchronousTransmitter<Event, Data>(eventHandler, events);
	}
	public static final <Data extends EventData<?, Default.SingleEvent>> Transmitter<Data> synchronous(Handler<Data> eventHandler) {
		return new SynchronousTransmitter<Default.SingleEvent, Data>(eventHandler, Default.SINGLE_EVENT);
	}
	public static final <Event, Data extends EventData<?, Event>> RegisterableTransmitter<Event, Data> synchronous() {
		return new SynchronousRegisterableTransmitter<Event, Data>();
	}
	public static final <Event, Data extends EventData<?, Event>> TransmitterService<Event, Data> asynchronous(final String threadName, final Handler<Data> eventHandler, final Event... events) {
		return register(new AsynchronousTransmitter<Event, Data>(threadName), eventHandler, events);
	}
	public static final <Data extends EventData<?, Default.SingleEvent>> TransmitterService<Default.SingleEvent, Data> asynchronous(final String threadName, final Handler<Data> eventHandler) {
		return register(new AsynchronousTransmitter<Default.SingleEvent, Data>(threadName), eventHandler, new Default.SingleEvent[] {Default.SINGLE_EVENT});
	}
	public static final <Event, Data extends EventData<?, Event>> TransmitterService<Event, Data> asynchronous(final String threadName) {
		return new AsynchronousTransmitter<Event, Data>(threadName);
	}
	public static final <Event, Data extends EventData<?, Event>> TransmitterService<Event, Data> asynchronous(final String threadName, final RegisterableTransmitter<Event, Data> transmitter) {
		return new AsynchronousTransmitter<Event, Data>(threadName, transmitter);
	}
	public static final <Event, Data extends EventData<?, Event>> TransmitterService<Event, Data> asynchronous(String identifier, RegisterableTransmitter<Event, Data> transmitter, boolean autoStart, ExecutorService executorService) {
		return new AsynchronousTransmitter<Event, Data>(identifier, transmitter, autoStart, executorService);
	}
	
	private static final <Event, Data extends EventData<?, Event>> TransmitterService<Event, Data> register(TransmitterService<Event, Data> transmitter, final Handler<Data> eventHandler, final Event[] events) {
		transmitter.addEventHandler(eventHandler, events);
		return transmitter;
	}	
}
