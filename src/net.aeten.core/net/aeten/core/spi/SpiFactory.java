package net.aeten.core.spi;

import net.aeten.core.Factory;

/**
 *
 * @author Thomas Pérennou
 */
public interface SpiFactory<T> extends Factory<T, String> {
	Class<?> getType();
}
