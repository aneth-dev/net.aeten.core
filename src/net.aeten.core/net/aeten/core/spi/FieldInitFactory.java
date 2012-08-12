package net.aeten.core.spi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.aeten.core.Factory;
import net.aeten.core.Identifiable;
import net.aeten.core.Predicate;
import net.aeten.core.parsing.Document;

/**
 *
 * @author Thomas Pérennou
 */
public class FieldInitFactory<T, P> implements Factory<T, Void> {

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

	public static Factory<Object, Void> create(final Document.Tag tag, final Class<?> type, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		if (tag.value == null) {
			return new Factory<Object, Void>() {

				@Override
				public Object create(Void context) {
					try {
						return classLoader.loadClass(tag.type).newInstance();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
						throw new IllegalArgumentException(ex);
					}
				}
			};
		} else if (tag.value instanceof String) {
			return new Factory<Object, Void>() {

				@Override
				public Object create(Void context) {
					try {
						return Service.getProvider((Class<? extends Identifiable>)type, (String) tag.value);
					} catch (ClassCastException | NoSuchElementException exception) {
						return Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
							@Override
							public boolean evaluate(SpiFactory element) {
								if (element.getParameterType().equals(String.class) && element.getTypes().length > 0) {
									for (Class<?> t : element.getTypes()) {
										if (t.equals(type)) {
											return true;
										}
									}
								}
								return false;
							}
						}).create(tag.value);
					}
				}
			};
		} else if (tag.value instanceof Document.Entry) {
			final Document.Entry entry = (Document.Entry) tag.value;
			try {
				return new FieldInitFactory<>(Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {

					@Override
					public boolean evaluate(SpiFactory element) {
						if (element.getParameterType().equals(Document.Tag.class) && element.getTypes().length > 0) {
							for (Class<?> t : element.getTypes()) {
								if (t.equals(type)) {
									return true;
								}
							}
						}
						return false;
					}
				}), tag);
			} catch (NoSuchElementException exception) {
				if (type == List.class) {
					return new Factory<Object, Void>() {

						@Override
						public Object create(Void context) {
							List<Object> list = new ArrayList<>();
							for (Document.Tag tag : entry.tags) {
								Class<?> elementType = parameterizedTypes.get(0);
								list.add(FieldInitFactory.create(tag, elementType, Collections.<Class<?>>emptyList(), elementType.getClassLoader()).create(null));
							}
							return list;
						}
					};
				} else if (type == Map.class && parameterizedTypes.get(0) == String.class) {
					return new Factory<Object, Void>() {

						@Override
						public Object create(Void context) {
							Map<String, Object> map = new LinkedHashMap<>();
							for (Document.Tag tag : entry.tags) {
								Class<?> elementType = parameterizedTypes.get(1);
								map.put(tag.name, FieldInitFactory.create(tag, elementType, Collections.<Class<?>>emptyList(), elementType.getClassLoader()).create(null));
							}
							return map;
						}
					};
				} else {
					try {
						if (tag.type == null) {
							for (Constructor<?> constructor : type.getConstructors()) {
								Annotation[][] annotations = constructor.getParameterAnnotations();
								if (annotations.length == 1 && annotations[0].length == 1 && annotations[0][0] instanceof SpiInitializer) {
									Class<?> initializerClass = constructor.getParameterTypes()[0];
									Object initializer = initializerClass.getConstructor(SpiConfiguration.class).newInstance(new SpiConfiguration(entry));
									return getFactory(constructor, initializer);
								}
							}
						} else {
//							return getFactory(classLoader.loadClass(tag.type).getConstructor(SpiConfiguration.class), new SpiConfiguration(entry));

							for (Constructor<?> constructor : classLoader.loadClass(tag.type).getConstructors()) {
								Annotation[][] annotations = constructor.getParameterAnnotations();
								if (annotations.length == 1 && annotations[0].length == 1 && annotations[0][0] instanceof SpiInitializer) {
									Class<?> initializerClass = constructor.getParameterTypes()[0];
									Object initializer = initializerClass.getConstructor(SpiConfiguration.class).newInstance(new SpiConfiguration(entry));
									return getFactory(constructor, initializer);
								}
							}
						}
					} catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
						throw new IllegalArgumentException(String.format("Unable to load %s (%s) as %s", tag.name, tag.type, type), ex);
					}
				}
			}

		}
		throw new IllegalArgumentException(tag.toString()); // Should not happend
	}

	private static Factory<Object, Void> getFactory(final Constructor<?> constructor, final Object parameter) {
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

	@Provider(SpiFactory.class)
	public static class StringFactory implements SpiFactory<String, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{String.class};
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
	public static class BooleanFactory implements SpiFactory<Boolean, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{Boolean.class, boolean.class};
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
	public static class IntegerFactory implements SpiFactory<Integer, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{Integer.class, int.class};
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
	public static class LongFactory implements SpiFactory<Long, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{Long.class, long.class};
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
	public static class FloatFactory implements SpiFactory<Float, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{Float.class, float.class};
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
	public static class DoubleFactory implements SpiFactory<Double, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{Double.class, double.class};
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
	public static class BigIntegerFactory implements SpiFactory<BigInteger, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{BigInteger.class};
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
	public static class BigDecimalFactory implements SpiFactory<BigDecimal, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{BigDecimal.class};
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
	public static class InetAddressFactory implements SpiFactory<InetAddress, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{InetAddress.class};
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
	public static class InetSocketFactory implements SpiFactory<InetSocketAddress, String> {

		@Override
		public Class[] getTypes() {
			return new Class[]{InetSocketAddress.class};
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
				throw new IllegalArgumentException(ex);
			}
		}
	}

	@Provider(SpiFactory.class)
	public static class InputStreamFactory extends StreamFactory<InputStream> {

		@Override
		public Class[] getTypes() {
			return new Class[]{InputStream.class};
		}
	}

	@Provider(SpiFactory.class)
	public static class OutputStreamFactory extends StreamFactory<OutputStream> {

		@Override
		public Class[] getTypes() {
			return new Class[]{OutputStream.class};
		}
	}

	public static abstract class StreamFactory<T> implements SpiFactory<T, Document.Tag> {

		@Override
		public Class<Document.Tag> getParameterType() {
			return Document.Tag.class;
		}

		@Override
		public T create(Document.Tag configuration) {
//			return (T) FieldInitFactory.create(configuration, configuration.type, Collections.<Class<?>>emptyList(), Thread.currentThread().getContextClassLoader()).;
			try {
				Class<? extends T> type = (Class<? extends T>) Class.forName(configuration.type);
				Document.Tag underlying = ((Document.Entry) configuration.value).tags.get(0);
				if (!underlying.name.equals("underlying")) {
					throw new IllegalArgumentException("Unexpected argument " + underlying.name + ". underlying was expected");
				}
				return type.getConstructor(getTypes()[0]).newInstance(FieldInitFactory.create(underlying, type, Collections.<Class<?>>emptyList(), Thread.currentThread().getContextClassLoader()).create(null));
			} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new IllegalArgumentException("Unable to create " + configuration, ex);
			}
		}
	}
}