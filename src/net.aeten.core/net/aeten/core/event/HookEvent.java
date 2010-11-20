package net.aeten.core.event;

public class HookEvent<E, H> {
	private final E sourceEvent;
	private final H hook;
	private final int hashCode;

	private HookEvent(E event, H hook) {
		super();
		this.sourceEvent = event;
		this.hook = hook;
		this.hashCode = this.toString().hashCode();
	}

	public E getSourceEvent() {
		return this.sourceEvent;
	}

	public H getHook() {
		return this.hook;
	}
	
	public static <E, H> HookEvent<E, H> get(E event, H hook) {
		return new HookEvent<E, H>(event, hook);
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (anObject == null) {
			return false;
		}
		if (anObject instanceof HookEvent<?, ?>) {
			HookEvent<?, ?> aHookEvent = (HookEvent<?, ?>)anObject;
			return this.sourceEvent.equals(aHookEvent.getSourceEvent()) && this.hook.equals(aHookEvent.getHook());
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.hashCode;
	}	
	@Override
	public String toString() {
		return ((this.hook == null) ? "" : this.hook + " ") + this.sourceEvent.toString();
	}
	
}
