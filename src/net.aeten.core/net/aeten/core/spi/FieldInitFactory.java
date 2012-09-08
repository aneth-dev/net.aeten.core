package net.aeten.core.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.aeten.core.Factory;
import net.aeten.core.Identifiable;
import net.aeten.core.Predicate;
import net.aeten.core.parsing.Document;
import net.aeten.core.parsing.Document.ElementType;

/**
 * 
 * @author Thomas Pérennou
 */
public class FieldInitFactory<T, P> implements
      Factory<T, Void> {

	private final SpiFactory<T, P> factory;
	private final P parameter;

	FieldInitFactory(SpiFactory<T, P> factory, P parameter) {
		this.factory = factory;
		this.parameter = parameter;
	}

	@Override
	public T create(Void context) {
		return factory.create(parameter);
	}

	public static Factory<Object, Void> create(final Document.Element configuration, final Class<?> type, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		// Simple class name given, try default constructor
		if (configuration.value == null || configuration.elementType == ElementType.STRING && configuration.asString().isEmpty()) {
			if (configuration.valueType == null) {
				throw new IllegalArgumentException("Empty configuration");
			}
			return getFactoryFromClassName(configuration.valueType, classLoader);
		}

		// Only one textual value
		else if (configuration.elementType == ElementType.STRING) {
			return getFactoryFromSimpleTextualValue(configuration.asString(), type, parameterizedTypes, classLoader);
		}

		// Configuration given
		else {
			try {
				return getFactoryWithSpiInitializer(type, configuration, classLoader);
			} catch (IllegalArgumentException | NoSuchElementException argumentException) {
				try {
					return getFactoryWithSpiFactory(configuration, type);
				} catch (Throwable exception) {
					try {
						if (type.isAssignableFrom(List.class)) {
							return getFactoryFromList(configuration, parameterizedTypes, classLoader);
						} else if (type.isAssignableFrom(Map.class) && parameterizedTypes.get(0) == String.class) {
							return getFactoryFromMap(configuration, parameterizedTypes, classLoader);
						} else {
							return getFactoryWithSpiInitializer(type, configuration, classLoader);
						}
					} catch (Throwable error) {
						throw new IllegalArgumentException(configuration.toString(), error);
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Factory<Object, Void> getFactoryWithSpiFactory(final Document.Element configuration, final Class<?> type) {
		return new FieldInitFactory<>(Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
			@Override
			public boolean evaluate(SpiFactory element) {
				if (element.getParameterType().equals(Document.Element.class) && element.getTypes().length > 0) {
					for (Class<?> t : element.getTypes()) {
						if (t.equals(type)) { return true; }
					}
				}
				return false;
			}
		}), configuration);
	}

	private static Factory<Object, Void> getFactoryWithSpiInitializer(Class<?> type, Document.Element configuration, ClassLoader classLoader) throws IllegalArgumentException {
		try {
			if (configuration.valueType == null) {
				return getFactoryWithSpiInitializer(type, configuration);
			} else {
				try {
					return getFactoryWithSpiInitializer(classLoader.loadClass(configuration.valueType), configuration);
				} catch (NoSuchElementException exception) {
					try {
						return getFactoryWithSpiInitializer(type, configuration);
					} catch (NoSuchElementException exception2) {
						throw new IllegalArgumentException("Unable to find construtor with SpiInitializer with neither " + configuration.valueType + " nor " + type + " for configuration = " + configuration, exception2);
					}
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new IllegalArgumentException(String.format("Unable to load %s (%s) as %s. Configuration = %s", configuration.value, configuration.valueType, type, configuration), ex);
		}
	}

	private static Factory<Object, Void> getFactoryWithSpiInitializer(Class<?> type, Document.Element configuration) throws NoSuchElementException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Constructor<?> constructor : type.getConstructors()) {
			Annotation[][] annotations = constructor.getParameterAnnotations();
			if (annotations.length == 1 && annotations[0].length == 1 && annotations[0][0] instanceof SpiInitializer) {
				Class<?> initializerClass = constructor.getParameterTypes()[0];
				Object initializer = initializerClass.getConstructor(SpiConfiguration.class).newInstance(new SpiConfiguration(configuration));
				return getFactoryWithSpiInitializer(constructor, initializer);
			}
		}
		throw new NoSuchElementException("Unable to find construtor with SpiInitializer in class " + type + " for " + configuration);
	}

	private static Factory<Object, Void> getFactoryWithSpiInitializer(final Constructor<?> constructor, final Object parameter) {
		return new Factory<Object, Void>() {
			@Override
			public Object create(Void context) {
				try {
					return constructor.newInstance(parameter);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
					throw new IllegalArgumentException(ex);
				}
			}
		};
	}

	private static Factory<Object, Void> getFactoryFromClassName(final String className, final ClassLoader classLoader) {
		return new Factory<Object, Void>() {
			@Override
			public Object create(Void context) {
				try {
					return classLoader.loadClass(className).newInstance();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
					throw new IllegalArgumentException("Unable to instanciate " + className + " with default construtor", ex);
				}
			}
		};
	}

	private static Factory<Object, Void> getFactoryFromList(final Document.Element configuration, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		return new Factory<Object, Void>() {
			@Override
			public Object create(Void context) {
				List<Object> list = new ArrayList<>();
				for (Document.Element child : configuration.asSequence()) {
					Class<?> elementType = parameterizedTypes.get(0);
					list.add(FieldInitFactory.create(child, elementType, Collections.<Class<?>> emptyList(), elementType.getClassLoader()).create(null));
				}
				return list;
			}
		};
	}

	private static Factory<Object, Void> getFactoryFromMap(final Document.Element configuration, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		return new Factory<Object, Void>() {
			@Override
			public Object create(Void context) {
				Map<String, Object> map = new LinkedHashMap<>();
				for (Document.Element child : configuration.asSequence()) {
					Document.MappingEntry tag = child.asMappingEntry();
					Class<?> elementType = parameterizedTypes.get(1);
					map.put(tag.getKey().asString(), FieldInitFactory.create(tag.getValue(), elementType, Collections.<Class<?>> emptyList(), elementType.getClassLoader()).create(null));
				}
				return map;
			}
		};
	}

	private static Factory<Object, Void> getFactoryFromSimpleTextualValue(final String value, final Class<?> type, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		return new Factory<Object, Void>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object create(Void context) {
				try {
					// Try to retrieve an Identifiable provider
					return Service.getProvider((Class<? extends Identifiable>) type, value);
				} catch (ClassCastException | NoSuchElementException exception) {
					try {
						// Second chance, try to retrieve a SpiFactory<type, String>
						return Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
							@Override
							public boolean evaluate(SpiFactory element) {
								if (element.getParameterType().equals(String.class) && element.getTypes().length > 0) {
									for (Class<?> t : element.getTypes()) {
										if (t.equals(type)) { return true; }
									}
								}
								return false;
							}
						}).create(value);
					} catch (NoSuchElementException nse) {
						throw new NoSuchElementException("Unable to find " + SpiFactory.class.getName() + " for " + type.getName() + " with " + value);
					}
				}
			}
		};
	}

	@Provider(SpiFactory.class)
	public static class StringFactory implements
	      SpiFactory<String, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { String.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public String create(String value) {
			return value;
		}
	}

	@Provider(SpiFactory.class)
	public static class BooleanFactory implements
	      SpiFactory<Boolean, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { Boolean.class, boolean.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public Boolean create(String value) {
			return Boolean.valueOf(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class IntegerFactory implements
	      SpiFactory<Integer, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { Integer.class, int.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public Integer create(String value) {
			return Integer.valueOf(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class LongFactory implements
	      SpiFactory<Long, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { Long.class, long.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public Long create(String value) {
			return Long.valueOf(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class FloatFactory implements
	      SpiFactory<Float, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { Float.class, float.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public Float create(String value) {
			return Float.valueOf(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class DoubleFactory implements
	      SpiFactory<Double, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { Double.class, double.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public Double create(String value) {
			return Double.valueOf(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class BigIntegerFactory implements
	      SpiFactory<BigInteger, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { BigInteger.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public BigInteger create(String value) {
			return new BigInteger(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class BigDecimalFactory implements
	      SpiFactory<BigDecimal, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { BigDecimal.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public BigDecimal create(String value) {
			return new BigDecimal(value);
		}
	}

	@Provider(SpiFactory.class)
	public static class InetAddressFactory implements
	      SpiFactory<InetAddress, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { InetAddress.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public InetAddress create(String value) {
			try {
				return InetAddress.getByName(value);
			} catch (UnknownHostException ex) {
				throw new IllegalArgumentException(ex);
			}
		}
	}

	@Provider(SpiFactory.class)
	public static class InetSocketFactory implements
	      SpiFactory<InetSocketAddress, String> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { InetSocketAddress.class };
		}

		@Override
		public Class<String> getParameterType() {
			return String.class;
		}

		@Override
		public InetSocketAddress create(String value) {
			try {
				String[] socket = value.split(":");
				return new InetSocketAddress(InetAddress.getByName(socket[0]), Integer.valueOf(socket[1]));
			} catch (UnknownHostException ex) {
				throw new IllegalArgumentException(value, ex);
			}
		}
	}

	@Provider(SpiFactory.class)
	public static class InputStreamFactory extends StreamFactory<InputStream> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { InputStream.class };
		}
	}

	@Provider(SpiFactory.class)
	public static class OutputStreamFactory extends StreamFactory<OutputStream> {

		@Override
		public Class<?>[] getTypes() {
			return new Class[] { OutputStream.class };
		}
	}

	public static abstract class StreamFactory<T> implements
	      SpiFactory<T, Document.Element> {

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
					return type.getConstructor(getTypes()[0]).newInstance(FieldInitFactory.create(underlying.getValue(), getTypes()[0], Collections.<Class<?>> emptyList(), Thread.currentThread().getContextClassLoader()).create(null));
				}
				if (configuration.valueType != null) {
					return (T) FieldInitFactory.create(configuration, type, Collections.<Class<?>> emptyList(), Thread.currentThread().getContextClassLoader()).create(null);
				}
				throw new IllegalArgumentException("Unable to create " + configuration);
			} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new IllegalArgumentException("Unable to create " + configuration, ex);
			}
		}
	}
}
