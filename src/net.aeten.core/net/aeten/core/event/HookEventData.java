package net.aeten.core.event;

public class HookEventData<Source, E, H> extends
		EventData <Source, HookEvent <E, H>> {
	private boolean doIt;

	public HookEventData (	Source source,
									HookEvent <E, H> event,
									Priority priority) {
		this (source, event, priority, true);
	}

	public HookEventData (	Source source,
									HookEvent <E, H> event,
									Priority priority,
									boolean doIt) {
		super (source, event, priority);
		this.doIt = doIt;
	}

	public HookEventData (	Source source,
									HookEvent <E, H> event) {
		this (source, event, Priority.MEDIUM);
	}

	public boolean doIt () {
		return this.doIt;
	}

	public void setDoIt (boolean doIt) {
		this.doIt = doIt;
	}

	public HookEventData <Source, E, H> setHookEvent (HookEvent <E, H> event) {
		this.event = event;
		return this;
	}

}
