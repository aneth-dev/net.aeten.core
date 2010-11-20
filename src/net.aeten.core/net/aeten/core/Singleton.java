package net.aeten.core;

/**
 * 
 * @author Thomas Pérennou
 */
public class Singleton<T> {

	private final Factory<T> factory;
	private T instance = null;

	public <Type extends Configurable<C>, C> Singleton(final Class<Type> clazz, final C configuration) {
		this(new Factory<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public Type create() throws Exception {
				Type object = clazz.newInstance();
				if ((configuration != null) && (object instanceof Configurable)) {
					((Configurable<C>) object).configure(configuration);
				}
				return object;
			}
		});
	}
	 
	public <Y extends T> Singleton(final Class<Y> clazz) {
		this(new Factory<T>() {
			@Override
			public Y create() throws Exception {
				return clazz.newInstance();
			}
		});
	}

	public Singleton(Factory<T> factory) {
		this.factory = factory;
	}

	@SuppressWarnings("cast")
	public synchronized T getInstance() throws Exception {
		if (this.instance == null) {
			instance = factory.create();
		}
		return this.instance;
	}

	public static final class Null<T extends Configurable<Object>> extends Singleton<T> {

		public Null() {
			super(new Factory<T>() {
				@Override
				public T create() throws Exception {
					return null;
				}
			});
		}
	}

}
