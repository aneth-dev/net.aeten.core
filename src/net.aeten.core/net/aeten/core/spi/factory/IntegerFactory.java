package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class IntegerFactory implements
		SpiFactory <Integer, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
				Integer.class,
				int.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public Integer create (String value) {
		return Integer.valueOf (value);
	}
}