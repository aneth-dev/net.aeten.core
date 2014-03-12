package net.aeten.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.aeten.core.EnumElement;

/**
 * 
 * @author Thomas Pérennou
 */
public final class TransmitterFactory {

	private TransmitterFactory () {}

	public static final <Event, Data extends EventData <?, Event>> Transmitter <Data> synchronous (	Handler <Data> eventHandler,
																																	Event... events) {
		return new SynchronousTransmitter <Event, Data> (eventHandler, events);
	}

	public static final <Data extends EventData <?, ?>> Transmitter <Data> synchronous (final Handler <Data> handler) {
		return new Transmitter <Data> () {
			@Override
			public Future <Data> transmit (Data data) {
				handler.handleEvent (data);
				return new FutureDone <Data> (data);
			}
		};
	}

	public static final <Event, Data extends EventData <?, Event>> RegisterableTransmitter <Event, Data> synchronous (Event[] events) {
		return new SynchronousRegisterableTransmitter <Event, Data> (events);
	}

	public static final <Event, Data extends EventData <?, Event>> TransmitterService <Event, Data> asynchronous (	final String threadName,
																																						Event[] eventEnum,
																																						final Handler <Data> eventHandler,
																																						final Event... events) {
		return register (new AsynchronousTransmitter <Event, Data> (threadName, eventEnum), eventHandler, events);
	}

	public static final <Event, Data extends EventData <?, Event>> TransmitterService <Event, Data> asynchronous (	final String threadName,
																																						final Event... events) {
		return new AsynchronousTransmitter <Event, Data> (threadName, events);
	}

	public static final <Data extends EventData <?, EnumElement.Generic <?>>> TransmitterService <EnumElement.Generic <?>, Data> asynchronous (	final String threadName,
																																																final Handler <Data> eventHandler) {
		EnumElement.Generic <?>[] events = EnumElement.genericEnum (new Object ());
		return register (new AsynchronousTransmitter <EnumElement.Generic <?>, Data> (threadName, events), eventHandler, events);
	}

	public static final <Data extends EventData <?, EnumElement.Generic <?>>> TransmitterService <EnumElement.Generic <?>, Data> asynchronous (final String threadName) {
		return new AsynchronousTransmitter <EnumElement.Generic <?>, Data> (threadName, EnumElement.genericEnum (new Object ()));
	}

	public static final <Event, Data extends EventData <?, Event>> TransmitterService <Event, Data> asynchronous (	final String threadName,
																																						final RegisterableTransmitter <Event, Data> transmitter) {
		return new AsynchronousTransmitter <Event, Data> (threadName, transmitter);
	}

	public static final <Event, Data extends EventData <?, Event>> TransmitterService <Event, Data> asynchronous (	String identifier,
																																						RegisterableTransmitter <Event, Data> transmitter,
																																						boolean autoStart,
																																						ExecutorService executorService) {
		return new AsynchronousTransmitter <Event, Data> (identifier, transmitter, autoStart, executorService);
	}

	private static final <Event, Data extends EventData <?, Event>> TransmitterService <Event, Data> register (	TransmitterService <Event, Data> transmitter,
																																					final Handler <Data> eventHandler,
																																					final Event[] events) {
		transmitter.addEventHandler (eventHandler, events);
		return transmitter;
	}
}
