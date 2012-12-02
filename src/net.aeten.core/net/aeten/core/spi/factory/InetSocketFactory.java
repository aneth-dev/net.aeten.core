package net.aeten.core.spi.factory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider (SpiFactory.class)
public class InetSocketFactory implements
		SpiFactory <InetSocketAddress, String> {

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			InetSocketAddress.class
		};
	}

	@Override
	public Class <String> getParameterType () {
		return String.class;
	}

	@Override
	public InetSocketAddress create (String value) {
		try {
			String[] socket = value.split (":");
			switch (socket.length) {
			case 1:
				return new InetSocketAddress (Integer.valueOf (socket[0]));
			case 2:
				return new InetSocketAddress (InetAddress.getByName (socket[0]), Integer.valueOf (socket[1]));
			default:
				throw new IllegalArgumentException (value);
			}
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException (value, ex);
		}
	}
}