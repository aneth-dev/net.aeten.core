package net.aeten.core;

/**
 *
 * @author Thomas Pérennou
 */
public interface Configurable<T> {
	public void configure(T configuration) throws ConfigurationException ;
}
