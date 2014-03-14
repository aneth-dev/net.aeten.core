package net.aeten.core.spi.factory;

import java.math.BigInteger;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class BigIntegerFactory implements SpiFactory<BigInteger, String> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { BigInteger.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}

	@Override
	public BigInteger create(String value) {
		return new BigInteger(value);
	}
}