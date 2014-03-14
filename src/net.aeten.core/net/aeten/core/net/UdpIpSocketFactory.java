package net.aeten.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.SpiInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Pérennou
 */
public class UdpIpSocketFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpIpSocketFactory.class);

	@FieldInit(alias = "destination")
	private final InetSocketAddress destinationInetSocketAddress;
	@FieldInit
	private final int maxPacketSize;
	@FieldInit(required = false)
	private final boolean reuse;
	@FieldInit(required = false)
	private final boolean bind;
	@FieldInit(	alias = { "source",
								"interface" // When destination is multicast
					},
					required = false)
	private final InetAddress sourceInetAddress;
	/**
	 * @see {@link MulticastSocket#setTrafficClass(int)}
	 */
	@FieldInit(required = false)
	private final String trafficClass;
	/**
	 * @see {@link DatagramSocket#setTimeout(int)}
	 */
	@FieldInit(	alias = "time out",
					required = false)
	private final Integer timeout;
	/**
	 * @see {@link MulticastSocket#setTimeToLive(int)}
	 */
	@FieldInit(	alias = { "ttl",
								"TTL"
					},
					required = false)
	private final Integer timeToLive;
	private DatagramSocket socket;

	public UdpIpSocketFactory(@SpiInitializer(generate = false) UdpIpSocketFactoryInitializer init) throws IOException {
		this.destinationInetSocketAddress = init.getDestinationInetSocketAddress();
		this.sourceInetAddress = init.hasSourceInetAddress()? init.getSourceInetAddress(): null;
		this.bind = init.hasBind()? init.getBind(): false;
		this.reuse = init.hasReuse()? init.getReuse(): false;
		this.maxPacketSize = init.getMaxPacketSize();
		this.trafficClass = init.hasTrafficClass()? init.getTrafficClass(): "0";
		this.timeout = init.hasTimeout()? init.getTimeout(): -1;
		this.timeToLive = init.hasTimeToLive()? init.getTimeToLive(): -1;
		createSocket();
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

		createSocket();
	}

	public final DatagramSocket createSocket() throws IOException {
		if ((socket != null) && !socket.isClosed()) { throw new IOException("Socket not closed"); }
		if (destinationInetSocketAddress.getAddress().isMulticastAddress()) {
			MulticastSocket multicastSocket = new MulticastSocket((SocketAddress) null);
			if (sourceInetAddress != null) {
				multicastSocket.setInterface(sourceInetAddress);
			}
			multicastSocket.setReuseAddress(true);
			if (bind) {
				if (!multicastSocket.isBound()) {
					LOGGER.info("Bind on {}", destinationInetSocketAddress);
					multicastSocket.bind(new InetSocketAddress(destinationInetSocketAddress.getPort()));
				} else {
					LOGGER.warn("Inet socket address {} already bound", destinationInetSocketAddress);
				}
				multicastSocket.joinGroup(destinationInetSocketAddress.getAddress());
			}
			if (this.timeToLive != -1) {
				multicastSocket.setTimeToLive(timeToLive);
			}
			socket = multicastSocket;
		} else {
			socket = new DatagramSocket(null);
			if (bind) {
				if (!socket.isBound()) {
					LOGGER.info("Bind on {}", destinationInetSocketAddress);
					socket.bind(destinationInetSocketAddress);
				} else {
					LOGGER.warn("Inet socket address {} already bound", destinationInetSocketAddress);
				}
			}
		}
		socket.setTrafficClass(Integer.parseInt(trafficClass, 2));
		if (timeout != -1) {
			socket.setSoTimeout(timeout);
		}
		socket.setReuseAddress(reuse);
		return socket;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void closeSocket() {
		if (socket instanceof MulticastSocket) {
			try {
				((MulticastSocket) socket).leaveGroup(destinationInetSocketAddress.getAddress());
			} catch (IOException exception) {
				LOGGER.error("Unable to leave multicast group " + destinationInetSocketAddress.getAddress(), exception);
			}
		}
		this.socket.close();
	}

	public InetSocketAddress getDestinationInetSocketAddress() {
		return destinationInetSocketAddress;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public int getTrafficClass() {
		return Integer.parseInt(trafficClass, 2);
	}

	public boolean isReuse() {
		return reuse;
	}

	public boolean isBind() {
		return bind;
	}

	public InetAddress getSourceInetAddress() {
		return sourceInetAddress;
	}

	@Override
	public String toString() {
		return destinationInetSocketAddress.toString();
	}
}
