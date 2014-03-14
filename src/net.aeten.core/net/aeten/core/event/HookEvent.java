package net.aeten.core.event;

import net.aeten.core.EnumElement;

public final class HookEvent<E, H> extends EnumElement<HookEvent<E, H>> {
	private final E sourceEvent;
	private final H hook;

	HookEvent(E event, H hook, int ordinal) {
		super(ordinal, ((hook == null)? "": hook + ":") + event.toString());
		this.sourceEvent = event;
		this.hook = hook;
	}

	public E getSourceEvent() {
		return this.sourceEvent;
	}

	public H getHook() {
		return this.hook;
	}

	@Override
	public boolean equals(Object anObject) {
		if (anObject == null) { return false; }
		if (anObject.getClass() == HookEvent.class) {
			HookEvent<?, ?> aHookEvent = (HookEvent<?, ?>) anObject;
			return this.sourceEvent.equals(aHookEvent.sourceEvent) && (this.hook == null)? (aHookEvent.hook == null): this.hook.equals(aHookEvent.hook);
		}
		return false;
	}

	@Override
	protected boolean isCanditateToCompare(HookEvent<E, H> element) {
		return super.isCanditateToCompare(element) && (sourceEvent.getClass() == element.sourceEvent.getClass()) && ((hook == null) || (element.hook == null) || (hook.getClass() == element.hook.getClass()));
	}

}
