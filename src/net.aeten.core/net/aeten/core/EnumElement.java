package net.aeten.core;

import java.lang.reflect.Array;
import java.util.Objects;

public abstract class EnumElement<T extends EnumElement <?>> implements
		Comparable <T> {

	private final int ordinal;
	private final String name;
	private final Class <?> type;

	protected EnumElement (	int ordinal,
									String name) {
		this (null, ordinal, name);
	}

	protected EnumElement (	Class <?> type,
									int ordinal,
									String name) {
		super ();
		this.ordinal = ordinal;
		this.name = name;
		this.type = type == null? getClass (): type;
	}

	public int ordinal () {
		return ordinal;
	}

	public String name () {
		return name;
	}

	@Override
	public int hashCode () {
		return Objects.hash (getClass (), name);
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || (obj.getClass () != this.getClass ())) {
			return false;
		}
		EnumElement <?> element = (EnumElement <?>) obj;
		return (type == element.type) && Objects.equals (name, element.name);
	}

	@Override
	public String toString () {
		return name;
	}

	@Override
	public int compareTo (T o) {
		if (!isCanditateToCompare (o)) {
			throw new ClassCastException ();
		}
		return ordinal - o.ordinal ();
	}

	protected boolean isCanditateToCompare (T element) {
		return element.getClass () == getClass ();
	}

	public static class FactoryContext<T> {
		public final T element;
		public final int ordinal;
		public final String name;

		public FactoryContext (	T element,
										int ordinal,
										String name) {
			this.element = element;
			this.ordinal = ordinal;
			this.name = name;
		}
	}

	public static class Generic<T> extends
			EnumElement <Generic <T>> {
		private final T element;

		private Generic (	T element,
								int ordinal,
								String name) {
			super (ordinal, name);
			this.element = element;
		}

		public T element () {
			return element;
		}

		public static <T> Factory <Generic <T>, FactoryContext <T>> buildFactory () {
			return new Factory <Generic <T>, FactoryContext <T>> () {
				@Override
				public Generic <T> create (FactoryContext <T> context) {
					return new Generic <> (context.element, context.ordinal, context.name);
				}
			};
		}
	}

	public static <T, E extends EnumElement <?>> E[] elements (	T[] array,
																					Factory <String, T> nameProvider,
																					Factory <E, FactoryContext <T>> elementFactory) {
		E[] elements = (E[]) Array.newInstance (array.getClass ().getComponentType (), array.length);
		for (int i = 0; i < array.length; i++) {
			elements[i] = elementFactory.create (new FactoryContext <> (array[i], i, nameProvider.create (array[i])));
		}
		return elements;
	}

	public static <T, E extends EnumElement <?>> E[] elements (	Factory <E, FactoryContext <T>> elementFactory,
																					final T... array) {
		return elements (array, new Factory <String, T> () {
			@Override
			public String create (T context) {
				return context.toString ();
			}
		}, elementFactory);
	}

	public static <T> Generic <T>[] genericEnum (T... array) {
		return elements (Generic.<T> buildFactory (), array);
	}

	public static <T> Generic <T>[] genericEnum (Factory <String, T> nameProvider,
																T... array) {
		return elements (array, nameProvider, Generic.<T> buildFactory ());
	}
}
