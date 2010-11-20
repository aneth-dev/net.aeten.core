package net.aeten.core.logging;

import net.aeten.core.event.Handler;
import net.aeten.core.event.RegisterableTransmitter;
import net.aeten.core.event.TransmitterFactory;

/**
 *
 * @author Thomas Pérennou
 */
public final class Logger {

	private final static RegisterableTransmitter<LogLevel, LoggingData> TRANSMITTER = TransmitterFactory.synchronous();

	private final Object source;
	public Logger(Object source) {
		this.source = source;	
	}
	
	public static void log(Object source, LogLevel level, String message, Throwable throwable) {
		TRANSMITTER.transmit(new LoggingData(source, level, message, throwable));
	}

	public static void log(Object source, LogLevel level, Throwable throwable) {
		TRANSMITTER.transmit(new LoggingData(source, level, throwable));
	}

	public static void log(Object source, LogLevel level, String message) {
		TRANSMITTER.transmit(new LoggingData(source, level, message));
	}

	public void trace(String message, Throwable throwable) {
		log(source, LogLevel.TRACE, message, throwable);
	}
	public void trace(Throwable throwable) {
		log(source, LogLevel.TRACE, throwable);
	}
	public void trace(String message) {
		log(source, LogLevel.TRACE, message);
	}

	public void info(String message, Throwable throwable) {
		log(source, LogLevel.INFO, message, throwable);
	}
	public void info(Throwable throwable) {
		log(source, LogLevel.INFO, throwable);
	}
	public void info(String message) {
		log(source, LogLevel.INFO, message);
	}

	public void debug(String message, Throwable throwable) {
		log(source, LogLevel.DEBUG, message, throwable);
	}
	public void debug(Throwable throwable) {
		log(source, LogLevel.DEBUG, throwable);
	}
	public void debug(String message) {
		log(source, LogLevel.DEBUG, message);
	}

	public void warn(String message, Throwable throwable) {
		log(source, LogLevel.WARN, message, throwable);
	}
	public void warn(Throwable throwable) {
		log(source, LogLevel.WARN, throwable);
	}
	public void warn(String message) {
		log(source, LogLevel.WARN, message);
	}

	public void error(String message, Throwable throwable) {
		log(source, LogLevel.ERROR, message, throwable);
	}
	public void error(Throwable throwable) {
		log(source, LogLevel.ERROR, throwable);
	}
	public void error(String message) {
		log(source, LogLevel.ERROR, message);
	}

	public void fatal(String message, Throwable throwable) {
		log(source, LogLevel.FATAL, message, throwable);
	}
	public void fatal(Throwable throwable) {
		log(source, LogLevel.FATAL, throwable);
	}
	public void fatal(String message) {
		log(source, LogLevel.FATAL, message);
	}

	
	public static void addEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.addEventHandler(eventHandler, eventList);
	}

	public static void removeEventHandler(Handler<LoggingData> eventHandler, LogLevel... eventList) {
		TRANSMITTER.removeEventHandler(eventHandler, eventList);
	}
}
