package net.aeten.core.event;

public class EventData<Source, Event> {
	protected Source source;
	protected Event event;
	protected Priority priority;

	public EventData (Source source,
							Event event) {
		this (source, event, Priority.MEDIUM);
	}

	public EventData (Source source,
							Event event,
							Priority priority) {
		super ();
		this.event = event;
		this.source = source;
		this.priority = priority;
	}

	public Source getSource () {
		return this.source;
	}

	public Event getEvent () {
		return this.event;
	}

	public Priority getPriority () {
		return this.priority;
	}
}
