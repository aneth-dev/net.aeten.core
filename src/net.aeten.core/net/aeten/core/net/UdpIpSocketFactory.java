package net.aeten.core.net;

import java.io.IOException;
import java.net.*;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;

/**
 *
 * @author Thomas Pérennou
 */
public class UdpIpSocketFactory {

	@FieldInit(alias = "destination")
	private final InetSocketAddress destinationInetSocketAddress;
	@FieldInit
	private final int maxPacketSize;
	@FieldInit(required = false)
	private final boolean reuse;
	@FieldInit(required = false)
	private final boolean bind;
	@FieldInit(alias = {
		"source",
		"interface" // When destination is multicast
	}, required = false)
	private final InetAddress sourceInetAddress;
	/**
	 * @see {@link MulticastSocket#setTrafficClass(int)}
	 */
	@FieldInit(required = false)
	private final String trafficClass;
	/**
	 * @see {@link DatagramSocket#setTimeout(int)}
	 */
	@FieldInit(alias = "time out", required = false)
	private final Integer timeout;
	/**
	 * @see {@link MulticastSocket#setTimeToLive(int)}
	 */
	@FieldInit(alias = {"ttl", "TTL"}, required = false)
	private final Integer timeToLive;
	private DatagramSocket socket;

	public UdpIpSocketFactory(@SpiInitializer UdpIpSocketFactoryInitializer init) throws IOException {
		this.destinationInetSocketAddress = init.getDestinationInetSocketAddress();
		this.sourceInetAddress = init.hasSourceInetAddress() ? init.getSourceInetAddress() : null;
		this.bind = init.hasBind() ? init.getBind() : false;
		this.reuse = init.hasReuse() ? init.getReuse() : false;
		this.maxPacketSize = init.getMaxPacketSize();
		this.trafficClass = init.hasTrafficClass() ? init.getTrafficClass() : "0";
		this.timeout = init.hasTimeout() ? init.getTimeout() : -1;
		this.timeToLive = init.hasTimeToLive() ? init.getTimeToLive() : -1;
		this.createSocket();
	}

	public UdpIpSocketFactory(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize) throws IOException {
		this(destinationInetSocketAddress, sourceInetAddress, bind, reuse, maxPacketSize, 0, -1, -1);
	}

	public UdpIpSocketFactory(InetSocketAddress destinationInetSocketAddress, InetAddress sourceInetAddress, boolean bind, boolean reuse, int maxPacketSize, int trafficClass, int timeout, int timeToLive) throws IOException {
		this.destinationInetSocketAddress = destinationInetSocketAddress;
		this.sourceInetAddress = sourceInetAddress;
		this.bind = bind;
		this.reuse = reuse;
		this.maxPacketSize = maxPacketSize;
		this.trafficClass = Integer.toBinaryString(trafficClass);
		this.timeout = timeout;
		this.timeToLive = timeToLive;

		this.createSocket();
	}

	public final DatagramSocket createSocket() throws IOException {
		if ((this.socket != null) && !this.socket.isClosed()) {
			throw new IOException("Socket not closed");
		}
		if (this.destinationInetSocketAddress.getAddress().isMulticastAddress()) {
			MulticastSocket multicastSocket = new MulticastSocket((SocketAddress) null);
			if (this.sourceInetAddress != null) {
				multicastSocket.setInterface(this.sourceInetAddress);
			}
			multicastSocket.setReuseAddress(true);
			if (this.bind) {
				if (!multicastSocket.isBound()) {
					Logger.log(this, LogLevel.INFO, "Bind on " + this.destinationInetSocketAddress);
					multicastSocket.bind(new InetSocketAddress(this.destinationInetSocketAddress.getPort()));
				} else {
					Logger.log(this, LogLevel.WARN, "Inet socket address" + this.destinationInetSocketAddress + " already bound");
				}
				multicastSocket.joinGroup(this.destinationInetSocketAddress.getAddress());
			}
			if (this.timeToLive != -1) {
				multicastSocket.setTimeToLive(this.timeToLive);
			}
			this.socket = multicastSocket;
		} else {
			this.socket = new DatagramSocket(null);
			if (this.bind) {
				if (!this.socket.isBound()) {
					Logger.log(this, LogLevel.INFO, "Bind on " + this.destinationInetSocketAddress);
					this.socket.bind(this.destinationInetSocketAddress);
				} else {
					Logger.log(this, LogLevel.WARN, "Inet socket address" + this.destinationInetSocketAddress + " already bound");
				}
			}
		}
		this.socket.setTrafficClass(Integer.parseInt(trafficClass, 2));
		if (this.timeout != -1) {
			this.socket.setSoTimeout(this.timeout);
		}
		this.socket.setReuseAddress(this.reuse);
		return this.socket;
	}

	public DatagramSocket getSocket() {
		return this.socket;
	}

	public void closeSocket() {
		if (this.socket instanceof MulticastSocket) {
			try {
				((MulticastSocket) this.socket).leaveGroup(this.destinationInetSocketAddress.getAddress());
			} catch (IOException exception) {
				Logger.log(this, LogLevel.ERROR, "Unable to leave multicast group " + this.destinationInetSocketAddress.getAddress(), exception);
			}
		}
		this.socket.close();
	}

	public InetSocketAddress getDestinationInetSocketAddress() {
		return this.destinationInetSocketAddress;
	}

	public int getMaxPacketSize() {
		return this.maxPacketSize;
	}

	public int getTrafficClass() {
		return Integer.parseInt(trafficClass, 2);
	}

	public boolean isReuse() {
		return this.reuse;
	}

	public boolean isBind() {
		return this.bind;
	}

	public InetAddress getSourceInetAddress() {
		return this.sourceInetAddress;
	}

	@Override
	public String toString() {
		return this.destinationInetSocketAddress.toString();
	}
}
