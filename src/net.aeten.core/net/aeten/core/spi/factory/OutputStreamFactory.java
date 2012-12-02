package net.aeten.core.spi.factory;

import java.io.OutputStream;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class OutputStreamFactory extends
		StreamFactory <OutputStream> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			OutputStream.class
		};
	}
}