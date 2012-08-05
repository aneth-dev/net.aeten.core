package net.aeten.core.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.aeten.core.Factory;
import net.aeten.core.Predicate;
import net.aeten.core.parsing.Document;

/**
 *
 * @author Thomas Pérennou
 */
public class FieldInitFactory<T> implements Factory<T, Void> {
	private final SpiFactory<T> factory;
	private final String parameter;

	FieldInitFactory(SpiFactory<T> factory, String parameter) {
		this.factory = factory;
		this.parameter = parameter;
	}

	@Override
	public T create(Void context) {
		return factory.create(parameter);
	}

	public static Factory<Object, Void> create(final Document.Tag tag, final Class<?> type, ClassLoader classLoader) {
			if (tag.value instanceof String) {
				if (type == String.class) { // TODO Built in (boolean, Boolean, Numbers, Char…)
					return new Factory<Object, Void>() {
						@Override
						public Object create(Void context) {
							return tag.value;
						}
					};
				}
				return new FieldInitFactory<>(Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
					@Override
					public boolean evaluate(SpiFactory element) {
						return element.getType().equals(type);
					}
				}), (String)tag.value);
			} else if (tag.value instanceof Document.Entry) {
				try {
					Class<?> provider = classLoader.loadClass(tag.type);
					final Constructor<?> constructor = provider.getConstructor(SpiConfiguration.class);
					final SpiConfiguration tagConfiguration = new SpiConfiguration((Document.Entry) tag.value);
					return new Factory<Object, Void>() {
						@Override
						public Object create(Void context) {
							try {
								return constructor.newInstance(tagConfiguration);
							} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
								throw new IllegalArgumentException(ex);
							}
						}
					};
				} catch (NoSuchMethodException | ClassNotFoundException ex) {
					throw new IllegalArgumentException(ex);
				}
			}
			throw new IllegalArgumentException(tag.toString()); // Should not happend
	}
}
