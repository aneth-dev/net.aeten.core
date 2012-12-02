package net.aeten.core.spi.factory;

import java.io.File;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class FileFactory implements
		SpiFactory <File, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			File.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public File create (String value) {
		return new File (value);
	}
}