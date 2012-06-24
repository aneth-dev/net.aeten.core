package net.aeten.core.spi;

import net.aeten.core.Predicate;

/**
 * Implementation must be thread safe and must be referenced as service (see
 * {@link java.util.ServiceLoader java.util.ServiceLoader})
 * 
 * @author Thomas Pérennou
 */
public interface ServiceLoader {

	/**
	 * @param service
	 *            the provided service
	 */
	public <S> void reload(Class<S> service);

	public <S> void reloadAll();

	/**
	 * @return The registered providers for a given service.
	 * @param service
	 *            the provided service
	 */
	public <S> Iterable<S> getProviders(Class<S> service, ClassLoader classLoader, Predicate<Class<S>> predicate);

}
