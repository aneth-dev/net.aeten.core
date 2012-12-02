package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class StringFactory implements
		SpiFactory <String, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			String.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public String create (String value) {
		return value;
	}
}