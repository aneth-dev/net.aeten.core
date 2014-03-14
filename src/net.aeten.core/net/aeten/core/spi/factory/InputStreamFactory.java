package net.aeten.core.spi.factory;

import java.io.InputStream;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class InputStreamFactory extends StreamFactory<InputStream> {

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { InputStream.class
		};
	}
}