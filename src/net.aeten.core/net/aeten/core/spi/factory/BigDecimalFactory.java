package net.aeten.core.spi.factory;

import java.math.BigDecimal;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class BigDecimalFactory implements SpiFactory<BigDecimal, String> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { BigDecimal.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}

	@Override
	public BigDecimal create(String value) {
		return new BigDecimal(value);
	}
}