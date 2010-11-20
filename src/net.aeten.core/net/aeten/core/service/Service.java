package net.aeten.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.aeten.core.Identifiable;
import net.aeten.core.Predicate;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;

/** 
 * @author Thomas Pérennou
 */
public class Service {
	private static final java.util.ServiceLoader<ServiceLoader> implementations = java.util.ServiceLoader.load(ServiceLoader.class);
	private static ConcurrentMap<Class<?>, Collection<?>> hotpluggedProvidersMap = new ConcurrentHashMap<Class<?>, Collection<?>>();

	static {
		boolean atLeastOne = false; 
		for (ServiceLoader loader : implementations) {
			Logger.log(Service.class, LogLevel.INFO, "ServiceLoader implementation: " + loader.getClass());
			atLeastOne = true;
		}
		if (!atLeastOne) {
			Logger.log(Service.class, LogLevel.WARN, "None ServiceLoader implementation");
		}
	}
	
	public static <S> void reload(Class<S> service) {
		Collection<?> services = hotpluggedProvidersMap.get(service);
		if (services != null) {
			services.clear();
		}
		for (ServiceLoader loader : implementations) {
			loader.reload(service);
		}
	}
	public static <S> void reload() {
		hotpluggedProvidersMap.clear();
		for (ServiceLoader loader : implementations) {
			loader.reloadAll();
		}
	}
	
	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public static <S extends Identifiable> S getProvider(Class<S> service, ClassLoader classLoader, String identifier) {
		for (S provider : getProviders(service, classLoader)) {
			if (identifier.equals(provider.getIdentifier())) {
				return provider;
			}
		}
		throw new NoSuchElementException("Unable to find provider for service " + service.getName() + " witch is identify by " + identifier);
	}
	
	public static <S extends Identifiable> S getProvider(Class<S> service, String identifier) {
		return getProvider(service, Thread.currentThread().getContextClassLoader(), identifier);
	}
		
	public static <S> S getProvider(Class<S> service, ClassLoader classLoader, Predicate<S> predicate) {
		for (S provider : getProviders(service, classLoader)) {
			if (predicate.matches(provider)) {
				return provider;
			}
		}
		throw new NoSuchElementException("Unable to find provider for service " + service.getName());
	}
	public static <S> S getProvider(Class<S> service, Predicate<S> predicate) {
		return getProvider(service, Thread.currentThread().getContextClassLoader(), predicate);
	}
	
	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public static <S> S getProvider(Class<S> service) {
		for (S provider : getProviders(service, Thread.currentThread().getContextClassLoader())) {
			return provider;
		}
		throw new NoSuchElementException("Unable to find provider for service " + service.getName());
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public static <S> Iterable<S> getProviders(Class<S> service, ClassLoader classLoader) {
		return new ServiceIterableAdapter<S>(service, classLoader);
	}

	@SuppressWarnings("unchecked")
	public static <T> void registerProvider(Class<T> service, T provider) {
		Collection<T> providers = (Collection<T>) hotpluggedProvidersMap.get(service);
		if (providers == null) {
			providers = Collections.synchronizedSet(new HashSet<T>());
			Collection<?> previous = hotpluggedProvidersMap.putIfAbsent(service, providers);
			if (previous != null) {
				providers = (Collection<T>) previous;
			}
		}
		providers.add(provider);
	}
	
	private static class ServiceIterableAdapter<T> implements Iterable<T> {
		private final Iterator<T> hotpluggedServices;
		private final Class<T> service;
		private final ClassLoader classLoader;
		
		@SuppressWarnings("unchecked")
		public ServiceIterableAdapter(Class<T> service, ClassLoader classLoader) {
			Collection<T> hotpluggedProviders = (Collection<T>)hotpluggedProvidersMap.get(service);
			this.hotpluggedServices = (hotpluggedProviders == null) ? (Iterator<T>)Collections.EMPTY_LIST.iterator() : hotpluggedProviders.iterator();
			this.service = service;
			this.classLoader = classLoader;
		}
		
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T> () {
				private final Iterator<Iterator<T>> loadersIterator;
				private volatile Iterator<T> iterator = hotpluggedServices;
				{
					Collection<Iterator<T>> loaders = new ArrayList<Iterator<T>>();
					for (ServiceLoader loader : implementations) {
						loaders.add(loader.getProviders(service, classLoader).iterator());
					}
					loadersIterator = loaders.iterator();
				}
				@Override
				public boolean hasNext() {
					if (iterator.hasNext()) {
						return true;
					}
					if (!loadersIterator.hasNext()) {
						return false;
					}
					iterator = loadersIterator.next();
					return hasNext();
				}

				@Override
				public T next() {
					try {
						return iterator.next();
					} catch (Error error) {
						Logger.log(ServiceIterableAdapter.class, LogLevel.ERROR, error);
						if (hasNext()) {
							return next();
						}
						throw error;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}


}
