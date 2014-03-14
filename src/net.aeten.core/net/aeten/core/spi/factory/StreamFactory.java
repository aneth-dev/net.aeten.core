package net.aeten.core.spi.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import net.aeten.core.parsing.Document;
import net.aeten.core.spi.FieldInitFactory;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

public abstract class StreamFactory<T> implements SpiFactory<T, Document.Element> {

	@Override
	public Class<Document.Element> getParameterType() {
		return Document.Element.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create(Document.Element configuration) {
		try {
			Class<? extends T> type = (Class<? extends T>) Class.forName(configuration.valueType);
			if (configuration.asSequence().size() == 1 && configuration.asSequence().getFirst().asMappingEntry().getKey().asString().equals("underlying")) {
				Document.MappingEntry underlying = configuration.asSequence().getFirst().asMappingEntry();
				return type.getConstructor(getTypes()[0]).newInstance(FieldInitFactory.create(underlying.getValue(), getTypes()[0], Collections.<Class<?>>emptyList(), Thread.currentThread().getContextClassLoader()).create(null));
			}
			if (configuration.valueType != null) { return (T) FieldInitFactory.create(configuration, type, Collections.<Class<?>>emptyList(), Thread.currentThread().getContextClassLoader()).create(null); }
			throw new IllegalArgumentException("Unable to create " + configuration);
		} catch (NoSuchMethodException
					| SecurityException
					| ClassNotFoundException
					| InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException ex) {
			throw new IllegalArgumentException("Unable to create " + configuration, ex);
		}
	}
}
