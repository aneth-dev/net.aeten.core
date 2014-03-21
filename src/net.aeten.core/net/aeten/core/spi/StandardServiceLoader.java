package net.aeten.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.aeten.core.Factory;
import java.util.function.Predicate;
import net.aeten.core.util.Concurrents;

/**
 * 
 * @author Thomas Pérennou
 */
@Provider(ServiceLoader.class)
public class StandardServiceLoader implements ServiceLoader {
	private final ConcurrentMap<Key<?>, Loader<?>> loaders = new ConcurrentHashMap<>();
	private final ConcurrentMap<Class<?>, Map<String, ?>> providers = new ConcurrentHashMap<>();
	private static final Factory<Map<String, ?>, Class<?>> PROVIDER_MAP_FACTORY = new Factory<Map<String, ?>, Class<?>>() {
		@Override
		public Map<String, ?> create(Class<?> context) {
			return new LinkedHashMap<String, Object>();
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public <S>Iterable<S> getProviders(Class<S> service, ClassLoader classLoader, Predicate<Class<S>> predicate) {
		Key<S> key = new Key<>(service, classLoader, predicate);
		Iterable<S> loader = (Iterable<S>) loaders.get(key);
		if (loader != null) return loader;

		Map<String, S> providersMap = (Map<String, S>) Concurrents.putIfAbsentAndGet(providers, service, PROVIDER_MAP_FACTORY);
		// There will not have another loader for this service due to mutex use of the caller
		loader = new Loader<>(service, providersMap, classLoader, predicate);
		loaders.put(key, (Loader<?>) loader);
		return loader;
	}

	@Override
	public <S>void reload(Class<S> service) {
		Loader<?> loader = loaders.get(service);
		if (loader != null) {
			loader.reload();
		}
	}

	private static class Key<S> {
		private final Class<?> service;
		private final ClassLoader loader;
		private Predicate<Class<S>> predicate;

		Key(Class<?> service, ClassLoader classLoader, Predicate<Class<S>> predicate) {
			this.service = service;
			this.loader = classLoader;
			this.predicate = predicate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((loader == null)? 0: loader.hashCode());
			result = (prime * result) + ((predicate == null)? 0: predicate.hashCode());
			result = (prime * result) + ((service == null)? 0: service.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Key<?> other = (Key<?>) obj;
			if (loader == null) {
				if (other.loader != null) return false;
			} else if (!loader.equals(other.loader)) return false;
			if (predicate == null) {
				if (other.predicate != null) return false;
			} else if (!predicate.equals(other.predicate)) return false;
			if (service == null) {
				if (other.service != null) return false;
			} else if (!service.equals(other.service)) return false;
			return true;
		}
	}
}

/**
 * This class is inspired by java.util.ServiceLoader but manage class filtering
 */
final class Loader<S> implements Iterable<S> {

	private static final String PREFIX = "META-INF/services/";

	private final Class<S> service;
	private final ClassLoader loader;
	private final Map<String, S> providers;
	private LazyIterator lookupIterator;
	private Predicate<Class<S>> predicate;

	public void reload() {
		providers.clear();
		lookupIterator = new LazyIterator();
	}

	Loader(Class<S> service, Map<String, S> providers, ClassLoader loader, Predicate<Class<S>> predicate) {
		this.service = service;
		this.loader = loader;
		this.predicate = predicate;
		this.providers = providers;
		this.lookupIterator = new LazyIterator();
	}

	private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
	}

	private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg);
	}

	private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
		fail(service, u + ":" + line + ": " + msg);
	}

	private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names) throws IOException, ServiceConfigurationError {
		String ln = r.readLine();
		if (ln == null) return -1;
		int ci = ln.indexOf('#');
		if (ci >= 0) {
			ln = ln.substring(0, ci);
		}
		ln = ln.trim();
		int n = ln.length();
		if (n != 0) {
			if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
				fail(service, u, lc, "Illegal configuration-file syntax");
			}
			int cp = ln.codePointAt(0);
			if (!Character.isJavaIdentifierStart(cp)) {
				fail(service, u, lc, "Illegal provider-class name: " + ln);
			}
			for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
				cp = ln.codePointAt(i);
				if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
					fail(service, u, lc, "Illegal provider-class name: " + ln);
				}
			}
			if (!providers.containsKey(ln) && !names.contains(ln)) {
				names.add(ln);
			}
		}
		return lc + 1;
	}

	private Iterator<String> parse(Class<?> service, URL u) throws ServiceConfigurationError {
		InputStream in = null;
		BufferedReader r = null;
		ArrayList<String> names = new ArrayList<>();
		try {
			in = u.openStream();
			r = new BufferedReader(new InputStreamReader(in, "utf-8"));
			int lc = 1;
			while ((lc = parseLine(service, u, r, lc, names)) >= 0) {
				; // Pass
			}
		} catch (IOException x) {
			fail(service, "Error reading configuration file", x);
		} finally {
			try {
				if (r != null) {
					r.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException y) {
				fail(service, "Error closing configuration file", y);
			}
		}
		return names.iterator();
	}

	private class LazyIterator implements Iterator<S> {

		Enumeration<URL> configs = null;
		Iterator<String> pending = null;
		String nextName = null;
		Set<String> setAsideProviders = new HashSet<>();

		@Override
		public boolean hasNext() {
			if (nextName != null) return true;
			if (configs == null) {
				try {
					String fullName = PREFIX + service.getName();
					if (loader == null) {
						configs = ClassLoader.getSystemResources(fullName);
					} else {
						configs = loader.getResources(fullName);
					}
				} catch (IOException x) {
					fail(service, "Error locating configuration files", x);
				}
			}

			while ((pending == null) || !pending.hasNext()) {
				if (!configs.hasMoreElements()) {
					if (setAsideProviders != null) {
						pending = setAsideProviders.iterator();
						setAsideProviders = null;
					}
					return false;
				}
				pending = parse(service, configs.nextElement());
			}
			nextName = pending.next();
			return true;
		}

		@Override
		public S next() {
			if (!hasNext()) throw new NoSuchElementException();
			String cn = nextName;
			nextName = null;
			try {
				@SuppressWarnings("unchecked")
				Class<S> providerClass = (Class<S>) Class.forName(cn, true, loader);
				if (predicate.test(providerClass)) {
					S provider = service.cast(providerClass.newInstance());
					providers.put(cn, provider);
					return provider;
				}
				if (setAsideProviders == null) {
					setAsideProviders = new HashSet<>();
				}
				setAsideProviders.add(cn);
				return null;
			} catch (ClassNotFoundException x) {
				fail(service, "Provider " + cn + " not found");
			} catch (Throwable x) {
				fail(service, "Provider " + cn + " could not be instantiated: " + x, x);
			}
			throw new Error(); // This cannot happen
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Iterator<S> iterator() {
		return new Iterator<S>() {

			Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

			@Override
			public boolean hasNext() {
				if (knownProviders.hasNext()) return true;
				return lookupIterator.hasNext();
			}

			@Override
			public S next() {
				if (knownProviders.hasNext()) return knownProviders.next().getValue();
				return lookupIterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}
}
