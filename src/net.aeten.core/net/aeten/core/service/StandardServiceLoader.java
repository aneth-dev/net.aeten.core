package net.aeten.core.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @author Thomas Pérennou
 */
@Provider(ServiceLoader.class)
public class StandardServiceLoader implements ServiceLoader {
	private ConcurrentMap<Class<?>, java.util.ServiceLoader<?>> serviceLoader = new ConcurrentHashMap<Class<?>, java.util.ServiceLoader<?>>();

	@SuppressWarnings("unchecked")
	@Override
	public <S> Iterable<S> getProviders(Class<S> service, ClassLoader classLoader) {
		// XXX Make a key with service and class loader ?
		java.util.ServiceLoader<?> loader = serviceLoader.get(service);
		if (loader == null) {
			loader = java.util.ServiceLoader.load(service, classLoader);
			serviceLoader.put(service, loader);
		}
		return (Iterable<S>) loader;
	}

	@Override
	public <S> void reload(Class<S> service) {
		java.util.ServiceLoader<?> loader = serviceLoader.get(service);
		if (loader != null) {
			loader.reload();
		}
	}

	@Override
	public <S> void reloadAll() {
		for (java.util.ServiceLoader<?> loader : serviceLoader.values()) {
			loader.reload();
		}
	}

}
