package net.aeten.core.logging;

import net.aeten.core.event.EventData;

/**
 *
 * @author Thomas Pérennou
 */
public class LoggingData extends EventData<Object, LogLevel> {
	private final String message;
	private final Throwable throwable;

	public LoggingData(Object source, LogLevel level, String message, Throwable throwable) {
		super(source, level);
		this.message = message;
		this.throwable = throwable;
	}

	public LoggingData(Object source, LogLevel level, Throwable throwable) {
		this(source, level, source.toString() + " has thrown an exception… ", throwable);
	}

	public LoggingData(Object source, LogLevel level, String message) {
		this(source, level, message, null);
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}

}
