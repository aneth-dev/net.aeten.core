package net.aeten.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import net.aeten.core.Factory;
import net.aeten.core.util.Concurrents;
import net.aeten.core.util.Concurrents.AtomicComparator;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Thomas Pérennou
 */
@ThreadSafe
class SynchronousRegisterableTransmitter<Event, Data extends EventData <?, Event>> implements
		RegisterableTransmitter <Event, Data> {

	private static final Logger LOGGER = LoggerFactory.getLogger (SynchronousRegisterableTransmitter.class);

	private final Map <Event, List <Handler <Data>>> eventHandlerMap;
	private final List <Handler <Data>> allEventsHandlerList;

	public SynchronousRegisterableTransmitter (Event[] events) {
		Factory <List <Handler <Data>>, Object> handlerListFactory = new Factory <List <Handler <Data>>, Object> () {
			@Override
			public List <Handler <Data>> create (Object event) {
				return new CopyOnWriteArrayList <Handler <Data>> ();
			}
		};
		eventHandlerMap = Concurrents.concurrentFilledMap (AtomicComparator.REFESENCE, events, handlerListFactory);
		allEventsHandlerList = handlerListFactory.create (null);
	}

	@Override
	public void addEventHandler (	Handler <Data> eventHandler,
											Event... eventList) {
		if (eventList.length == 0) {
			allEventsHandlerList.add (eventHandler);
		} else {
			for (Event event: eventList) {
				eventHandlerMap.get (event).add (eventHandler);
			}
		}
	}

	@Override
	public void removeEventHandler (	Handler <Data> eventHandler,
												Event... eventList) {
		if (eventList.length == 0) {
			allEventsHandlerList.remove (eventHandler);
			return;
		}
		for (Event event: eventList) {
			final List <Handler <Data>> handlers = eventHandlerMap.get (event);
			if (handlers != null) {
				handlers.remove (eventHandler);
			}
		}
	}

	@Override
	public Future <Data> transmit (Data data) {
		fireEvent (allEventsHandlerList, data);
		fireEvent (eventHandlerMap.get (data.getEvent ()), data);
		return new FutureDone <Data> (data);
	}

	private void fireEvent (Iterable <Handler <Data>> handlers,
									Data data) {
		if (handlers == null) {
			return;
		}
		for (Handler <Data> eventHandler: handlers) {
			try {
				eventHandler.handleEvent (data);
			} catch (Throwable error) {
				LOGGER.error ("An handler has thrown an error", error);
			}
		}
	}

}
