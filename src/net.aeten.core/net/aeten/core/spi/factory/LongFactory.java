package net.aeten.core.spi.factory;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class LongFactory implements SpiFactory<Long, String> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { Long.class,
									long.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}

	@Override
	public Long create(String value) {
		return Long.valueOf(value);
	}
}