package net.aeten.core.event;

import java.util.Arrays;

public abstract class HookEventGroup<E, H> {
	protected final HookEvent <E, H>[] values;
	protected final int eventsCount;
	protected final int hooksCount;

	@SuppressWarnings ("unchecked")
	private HookEventGroup (E[] events,
									H[] hooks) {
		this.eventsCount = events.length;
		this.hooksCount = hooks.length;
		this.values = new HookEvent[this.eventsCount * this.hooksCount];

		for (int hookIndex = 0, eventIndex = 0; hookIndex < this.values.length; hookIndex++, eventIndex++) {
			for (H hook: hooks) {
				this.values[hookIndex] = new HookEvent <E, H> (events[eventIndex], hook, hookIndex);
				hookIndex++;
			}
			hookIndex--;
		}
	}

	public HookEvent <E, H>[] values () {
		return Arrays.copyOf (this.values, this.values.length);
	}

	public static <E, H> HookEventGroup <E, H> build (	E[] events,
																		H[] hooks) {
		return new HookEventGroup <E, H> (events, hooks) {
			@Override
			public HookEvent <E, H> get (	E event,
													H hook) {
				for (int i = 0, eventIndex = 0; eventIndex < this.eventsCount; eventIndex++, i += this.hooksCount) {
					if (this.values[i].getSourceEvent ().equals (event)) {
						for (int hookIndex = 0; hookIndex < this.hooksCount; hookIndex++, i++) {
							if (this.values[i].getHook ().equals (hook)) {
								return this.values[i];
							}
						}
					}
				}
				return null;
			}
		};
	}

	public static <E extends Enum <?>, H extends Enum <?>> HookEventGroup <E, H> build (E[] events,
																													H[] hooks) {
		return new HookEventGroup <E, H> (events, hooks) {
			@Override
			public HookEvent <E, H> get (	E event,
													H hook) {
				return this.values[(event.ordinal () * this.hooksCount) + hook.ordinal ()];
			}
		};
	}

	public static <E> HookEventGroup <E, Hook> build (E[] events) {
		return build (events, Hook.values ());
	}

	public <T extends HookEventData <S, E, H>, S> T hook (T data,
																			H hook) {
		data.event = this.get (data.event.getSourceEvent (), hook);
		return data;
	}

	public abstract HookEvent <E, H> get (	E event,
														H hook);
}
