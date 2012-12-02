package net.aeten.core.spi.factory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class InetAddressFactory implements
		SpiFactory <InetAddress, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			InetAddress.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public InetAddress create (String value) {
		try {
			return InetAddress.getByName (value);
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException (ex);
		}
	}
}