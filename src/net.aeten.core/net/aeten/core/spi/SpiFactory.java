package net.aeten.core.spi;

import net.aeten.core.Factory;

/**
 *
 * @author Thomas Pérennou
 */
public interface SpiFactory<T, P> extends
		Factory <T, P> {
	Class <?>[] getTypes ();

	Class <P> getParameterType ();
}
