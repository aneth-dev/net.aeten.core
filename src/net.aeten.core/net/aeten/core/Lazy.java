package net.aeten.core;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author Thomas Pérennou
 */
public class Lazy<T, C> {

	private final Factory<T, C> factory;
	private final C configuration;
	private final AtomicReference<T> instance;

	public static <T, C> Lazy<T, C> build(final Class<T> clazz, C configuration) {
		return build(new Factory<T, C>() {
			@Override
			public T create(C context) {
				try {
					if (context == null) {
						return clazz.newInstance();
					} else {
						return clazz.getConstructor(context.getClass()).newInstance(context);
					}
				} catch (Exception exception) {
					throw new Error(exception);
				}
			}
		}, configuration);
	}

	public static <T> Lazy<T, Void> build(final Class<T> clazz) {
		return build(new Factory<T, Void>() {
			@Override
			public T create(Void context) {
				try {
					return clazz.newInstance();
				} catch (Exception exception) {
					throw new Error(exception);
				}
			}
		}, null);
	}

	public static <T, C> Lazy<T, C> build(Factory<T, C> factory, C configuration) {
		return new Lazy<T, C>(factory, configuration);
	}

	public static <T> Lazy<T, Void> build(Factory<T, Void> factory) {
		return build(factory, null);
	}

	private Lazy(Factory<T, C> factory, C configuration) {
		this.factory = factory;
		this.configuration = configuration;
		this.instance = new AtomicReference<T>();
	}

	public T instance() {
		T instance = this.instance.get();
		if (instance == null) {
			this.instance.compareAndSet(null, this.factory.create(this.configuration));
			return this.instance.get();
		}
		return instance;
	}

	public static final class Null<T> extends Lazy<T, Void> {
		public Null() {
			super(new Factory<T, Void>() {
				@Override
				public T create(Void context) {
					return null;
				}
			}, null);
		}
	}

}
