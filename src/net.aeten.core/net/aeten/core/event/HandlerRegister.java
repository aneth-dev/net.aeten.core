package net.aeten.core.event;

/**
 *
 * @author Thomas Pérennou
 */
@SuppressWarnings("unchecked")
public interface HandlerRegister<Event, Data extends EventData<?, Event>> {
	public void addEventHandler(Handler<Data> eventHandler, Event... eventList);

	public void removeEventHandler(Handler<Data> eventHandler, Event... eventList);
}
