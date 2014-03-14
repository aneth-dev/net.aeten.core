package net.aeten.core.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import net.aeten.core.Factory;
import net.aeten.core.Identifiable;
import net.aeten.core.Predicate;
import net.aeten.core.parsing.Document;
import net.aeten.core.parsing.Document.ElementType;

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

	public static Factory<Object, Void> create(final Document.Element configuration, Class<?> fieldType, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {

		final Class<?> type;
		if (fieldType.equals(AtomicReference.class)) {
			type = parameterizedTypes.get(0);
			parameterizedTypes.clear();
			for (TypeVariable<?> parameterizedType: type.getTypeParameters()) {
				parameterizedTypes.add((Class<?>) parameterizedType.getGenericDeclaration());
			}
		} else {
			type = fieldType;
		}

		// Simple class name given, try default constructor
		if (configuration.value == null || configuration.elementType == ElementType.STRING && configuration.asString().isEmpty()) {
			if (configuration.valueType == null) { throw new IllegalArgumentException("Empty configuration"); }
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
			} catch (IllegalArgumentException
						| NoSuchElementException argumentException) {
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

	@SuppressWarnings({	"unchecked",
								"rawtypes"
	})
	private static Factory<Object, Void> getFactoryWithSpiFactory(final Document.Element configuration, final Class<?> type) {
		return new FieldInitFactory<>(Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
			@Override
			public boolean evaluate(SpiFactory element) {
				if (element.getParameterType().equals(Document.Element.class) && element.getTypes().length > 0) {
					for (Class<?> t: element.getTypes()) {
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
		} catch (ClassNotFoundException
					| NoSuchMethodException
					| InstantiationException
					| IllegalAccessException
					| InvocationTargetException ex) {
			throw new IllegalArgumentException(String.format("Unable to load %s (%s) as %s. Configuration = %s", configuration.value, configuration.valueType, type, configuration), ex);
		}
	}

	private static Factory<Object, Void> getFactoryWithSpiInitializer(Class<?> type, Document.Element configuration) throws NoSuchElementException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Constructor<?> constructor: type.getConstructors()) {
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
				} catch (InstantiationException
							| IllegalAccessException
							| InvocationTargetException ex) {
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
				} catch (ClassNotFoundException
							| InstantiationException
							| IllegalAccessException ex) {
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
				for (Document.Element child: configuration.asSequence()) {
					Class<?> elementType = parameterizedTypes.get(0);
					list.add(FieldInitFactory.create(child, elementType, Collections.<Class<?>>emptyList(), elementType.getClassLoader()).create(null));
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
				for (Document.Element child: configuration.asSequence()) {
					Document.MappingEntry tag = child.asMappingEntry();
					Class<?> elementType = parameterizedTypes.get(1);
					map.put(tag.getKey().asString(), FieldInitFactory.create(tag.getValue(), elementType, Collections.<Class<?>>emptyList(), elementType.getClassLoader()).create(null));
				}
				return map;
			}
		};
	}

	private static Factory<Object, Void> getFactoryFromSimpleTextualValue(final String value, final Class<?> type, final List<Class<?>> parameterizedTypes, final ClassLoader classLoader) {
		return new Factory<Object, Void>() {
			@SuppressWarnings({	"unchecked",
										"rawtypes"
			})
			@Override
			public Object create(Void context) {
				try {
					// Try to retrieve an Identifiable provider
					return Service.getProvider((Class<? extends Identifiable>) type, value);
				} catch (ClassCastException
							| NoSuchElementException exception) {
					try {
						// Second chance, try to retrieve a SpiFactory<type, String>
						return Service.getProvider(SpiFactory.class, new Predicate<SpiFactory>() {
							@Override
							public boolean evaluate(SpiFactory element) {
								if (element.getParameterType().equals(String.class) && element.getTypes().length > 0) {
									for (Class<?> t: element.getTypes()) {
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
	};

}
