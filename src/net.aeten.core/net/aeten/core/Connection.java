package net.aeten.core;

/**
 *
 * @author Thomas Pérennou
 */
import java.io.IOException;

public interface Connection {
	public void connect() throws IOException;

	public void disconnect() throws IOException;

	public boolean isConnected();

}
