package net.aeten.core;

import java.lang.reflect.InvocationTargetException;
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
				} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException exception) {
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
				} catch (InstantiationException | IllegalAccessException exception) {
					throw new Error(exception);
				}
			}
		}, null);
	}

	public static <T, C> Lazy<T, C> build(Factory<T, C> factory, C configuration) {
		return new Lazy<>(factory, configuration);
	}

	public static <T> Lazy<T, Void> build(Factory<T, Void> factory) {
		return build(factory, null);
	}

	private Lazy(Factory<T, C> factory, C configuration) {
		this.factory = factory;
		this.configuration = configuration;
		this.instance = new AtomicReference<>();
	}

	public T instance() {
		T ref = this.instance.get();
		if (ref == null) {
			this.instance.compareAndSet(null, this.factory.create(this.configuration));
			return this.instance.get();
		}
		return ref;
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
