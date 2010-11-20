package net.aeten.core.event;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.util.CollectionUtil;

/**
 * 
 * @author Thomas Pérennou
 */
class SynchronousRegisterableTransmitter<Event, Data extends EventData<?, Event>> implements RegisterableTransmitter<Event, Data> {

	private final ConcurrentMap<Event, List<Handler<Data>>> eventHandlerMap;
	private final List<Handler<Data>> allEventsHandlerList;

	public SynchronousRegisterableTransmitter() {
		eventHandlerMap = new ConcurrentHashMap<Event, List<Handler<Data>>>();
		allEventsHandlerList = new CopyOnWriteArrayList<Handler<Data>>();
	}

	@Override
	public void addEventHandler(Handler<Data> eventHandler, Event... eventList) {
		if (eventList.length == 0) {
			allEventsHandlerList.add(eventHandler);
		} else {
			for (Event event : eventList) {
				CollectionUtil.putIfAbsent(eventHandlerMap, event, new CopyOnWriteArrayList<Handler<Data>>()).add(eventHandler);
			}
		}
	}

	@Override
	public void removeEventHandler(Handler<Data> eventHandler, Event... eventList) {
		if (eventList.length == 0) {
			allEventsHandlerList.remove(eventHandler);
			return;
		}
		synchronized (this.eventHandlerMap) {
			for (Event event : eventList) {
				final List<Handler<Data>> handlers = this.eventHandlerMap.get(event);
				if (handlers != null) {
					handlers.remove(eventHandler);
				}
			}
		}
	}

	@Override
	public Future<Data> transmit(Data data) {
		fireEvent(allEventsHandlerList, data);
		fireEvent(eventHandlerMap.get(data.getEvent()), data);
		return new FutureDone<Data>(data);
	}

	private void fireEvent(Iterable<Handler<Data>> handlers, Data data) {
		if (handlers == null) {
			return;
		}
		for (Handler<Data> eventHandler : handlers) {
			try {
				eventHandler.handleEvent(data);
			} catch (Throwable error) {
				Logger.log(eventHandler, LogLevel.ERROR, error);
			}
		}
	}

}
