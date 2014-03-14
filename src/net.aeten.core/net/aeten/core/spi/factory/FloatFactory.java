package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class FloatFactory implements SpiFactory<Float, String> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { Float.class,
									float.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}

	@Override
	public Float create(String value) {
		return Float.valueOf(value);
	}
}