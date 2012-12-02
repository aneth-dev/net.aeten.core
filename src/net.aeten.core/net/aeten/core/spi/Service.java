package net.aeten.core.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.aeten.core.Identifiable;
import net.aeten.core.Predicate;
import net.aeten.core.util.Concurrents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Pérennou
 */
public class Service {

	private static final Logger LOGGER = LoggerFactory.getLogger (Service.class);
	private static final java.util.ServiceLoader <ServiceLoader> implementations = java.util.ServiceLoader.load (ServiceLoader.class);
	/**
	 * Values guarded by getMutex(key)
	 */
	private static final ConcurrentMap <Class <?>, Collection <?>> hotpluggedProvidersMap = new ConcurrentHashMap <> ();
	private static final ConcurrentMap <Class <?>, Object> mutexMap = new ConcurrentHashMap <> ();

	static {
		boolean atLeastOne = false;
		for (ServiceLoader loader: implementations) {
			LOGGER.info ("ServiceLoader implementation: {}", loader.getClass ());
			atLeastOne = true;
		}
		if (!atLeastOne) {
			LOGGER.warn ("None ServiceLoader implementation");
		}
	}

	public static <S> void reload (Class <S> service) {
		Collection <?> services = hotpluggedProvidersMap.get (service);
		synchronized (getMutex (service)) {
			if (services != null) {
				services.clear ();
			}
			for (ServiceLoader loader: implementations) {
				loader.reload (service);
			}
		}
	}

	public static void reload () {
		hotpluggedProvidersMap.clear ();
		for (Map.Entry <Class <?>, Object> entry: mutexMap.entrySet ()) {
			synchronized (entry.getValue ()) {
				for (ServiceLoader loader: implementations) {
					loader.reload (entry.getKey ());
				}
			}
		}
	}

	private static final Object getMutex (Class <?> service) {
		return Concurrents.putIfAbsentAndGet (mutexMap, service, new Object ());
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service the provided service
	 */
	public static <S extends Identifiable> S getProvider (Class <S> service,
																			ClassLoader classLoader,
																			String identifier) {
		for (S provider: getProviders (service, classLoader, new Predicate <Class <S>> () {

			@Override
			public boolean evaluate (Class <S> element) {
				return true;
			}
		})) {
			if (identifier.equals (provider.getIdentifier ())) {
				return provider;
			}
		}
		throw new NoSuchElementException ("Unable to find provider for service " + service.getName () + " witch is identify by " + identifier);
	}

	public static <S extends Identifiable> S getProvider (Class <S> service,
																			String identifier) {
		return getProvider (service, Thread.currentThread ().getContextClassLoader (), identifier);
	}

	public static <S> S getProvider (Class <S> service,
												ClassLoader classLoader,
												Predicate <Class <S>> classPredicate,
												Predicate <S> instancePredicate) {
		for (S provider: getProviders (service, classLoader, classPredicate)) {
			if (instancePredicate.evaluate (provider)) {
				return provider;
			}
		}
		throw new NoSuchElementException ("Unable to find provider for service " + service.getName ());
	}

	public static <S> S getProvider (Class <S> service,
												Predicate <S> instancePredicate) {
		return getProvider (service, Thread.currentThread ().getContextClassLoader (), new Predicate <Class <S>> () {

			@Override
			public boolean evaluate (Class <S> element) {
				return true;
			}
		}, instancePredicate);
	}

	public static <S> S getProvider (Class <S> service,
												Predicate <Class <S>> classPredicate,
												Predicate <S> instancePredicate) {
		return getProvider (service, Thread.currentThread ().getContextClassLoader (), classPredicate, instancePredicate);
	}

	public static <S> List <S> getProviders (Class <S> service) {
		return getProviders (service, Thread.currentThread ().getContextClassLoader ());
	}

	public static <S> List <S> getProviders (	Class <S> service,
															ClassLoader classLoader) {
		return getProviders (service, classLoader, new Predicate <Class <S>> () {

			@Override
			public boolean evaluate (Class <S> element) {
				return true;
			}
		});
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service the provided service
	 */
	public static <S> S getProvider (Class <S> service) {
		for (S provider: getProviders (service, Thread.currentThread ().getContextClassLoader ())) {
			return provider;
		}
		throw new NoSuchElementException ("Unable to find provider for service " + service.getName ());
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service the provided service
	 * @param predicate the class filter
	 */
	public static <S> List <S> getProviders (	Class <S> service,
															Predicate <Class <S>> predicate) {
		return getProviders (service, Thread.currentThread ().getContextClassLoader (), predicate);
	}

	/**
	 * @return The registered providers for a given service.
	 * @param service the provided service
	 * @param classLoader the class loader
	 * @param predicate the class filter
	 */
	public static <S> List <S> getProviders (	Class <S> service,
															ClassLoader classLoader,
															Predicate <Class <S>> predicate) {
		ArrayList <S> providers = new ArrayList <> ();
		synchronized (getMutex (service)) {
			for (S provider: new ServiceIterableAdapter <S> (service, classLoader, predicate)) {
				providers.add (provider);
			}
		}
		return providers;
	}

	@SuppressWarnings ("unchecked")
	public static <T> void registerProvider (	Class <T> service,
															T provider) {
		Collection <T> providers = (Collection <T>) hotpluggedProvidersMap.get (service);
		if (providers == null) {
			providers = new HashSet <T> ();
			Collection <?> previous = hotpluggedProvidersMap.putIfAbsent (service, providers);
			if (previous != null) {
				providers = (Collection <T>) previous;
			}
		}
		synchronized (getMutex (service)) {
			providers.add (provider);
		}
	}

	private static class ServiceIterableAdapter<T> implements
			Iterable <T> {

		private final Iterator <T> hotpluggedServices;
		private final Class <T> service;
		private final ClassLoader classLoader;
		private final Predicate <Class <T>> predicate;

		@SuppressWarnings ("unchecked")
		public ServiceIterableAdapter (	Class <T> service,
													ClassLoader classLoader,
													Predicate <Class <T>> predicate) {
			Collection <T> hotpluggedProviders = (Collection <T>) hotpluggedProvidersMap.get (service);
			this.hotpluggedServices = (hotpluggedProviders == null)? (Iterator <T>) Collections.EMPTY_LIST.iterator (): hotpluggedProviders.iterator ();
			this.service = service;
			this.classLoader = classLoader;
			this.predicate = predicate;
		}

		@Override
		public Iterator <T> iterator () {
			return new Iterator <T> () {

				private final Iterator <Iterator <T>> loadersIterator;
				private volatile Iterator <T> iterator = hotpluggedServices;
				private T next = null;

				{
					Collection <Iterator <T>> loaders = new ArrayList <Iterator <T>> ();
					for (ServiceLoader loader: implementations) {
						loaders.add (loader.getProviders (service, classLoader, predicate).iterator ());
					}
					loadersIterator = loaders.iterator ();
				}

				@Override
				public boolean hasNext () {
					if (iterator.hasNext ()) {
						next = _next ();
						if (next == null) {
							return hasNext ();
						}
						return true;
					}
					if (!loadersIterator.hasNext ()) {
						return false;
					}
					iterator = loadersIterator.next ();
					return hasNext ();
				}

				@Override
				public T next () {
					return next;
				}

				private T _next () {
					try {
						return iterator.next ();
					} catch (Error error) {
						LOGGER.error ("Service loader could not instanciate a provider for " + service, error);
						if (hasNext ()) {
							return next ();
						}
						throw error;
					}
				}

				@Override
				public void remove () {
					throw new UnsupportedOperationException ();
				}
			};
		}
	}
}
