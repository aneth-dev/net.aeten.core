package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class BooleanFactory implements
		SpiFactory <Boolean, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
				Boolean.class,
				boolean.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public Boolean create (String value) {
		return Boolean.valueOf (value);
	}
}