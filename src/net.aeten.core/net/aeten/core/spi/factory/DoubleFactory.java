package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class DoubleFactory implements SpiFactory<Double, String> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { Double.class,
									double.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}

	@Override
	public Double create(String value) {
		return Double.valueOf(value);
	}
}