package net.aeten.core.event;

/**
 *
 * @author Thomas Pérennou
 */
public interface RegisterableTransmitter<Event, Data extends EventData<?, Event>> extends Transmitter<Data>, HandlerRegister<Event, Data> {}
