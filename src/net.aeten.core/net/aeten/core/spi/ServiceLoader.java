package net.aeten.core.spi;

import java.util.function.Predicate;

/**
 * Calling methods are guarded by each service mutex. Implementation must be
 * thread safe on all non specific service and must be referenced as service
 * (see {@link java.util.ServiceLoader java.util.ServiceLoader})
 * 
 * @author Thomas Pérennou
 */
public interface ServiceLoader {

	/**
	 * @param service
	 *           the provided service
	 */
	public <S>void reload(Class<S> service);

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *           the provided service
	 */
	public <S>Iterable<S> getProviders(Class<S> service, ClassLoader classLoader, Predicate<Class<S>> predicate);

}
