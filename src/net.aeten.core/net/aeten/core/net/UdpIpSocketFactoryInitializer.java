package net.aeten.core.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import net.aeten.core.Factory;
import net.aeten.core.parsing.Document;
import net.aeten.core.spi.FieldInitFactory;
import net.aeten.core.spi.SpiConfiguration;

@Generated("net.aeten.core.spi.FieldInitializationProcessor")
public class UdpIpSocketFactoryInitializer {
	private final Map<String, Factory<Object, Void>> fieldsFactories;

	public UdpIpSocketFactoryInitializer(SpiConfiguration configuration) {
		fieldsFactories = new HashMap<>();
		for (Document.Element element: configuration.root.asSequence()) {
			final String field;
			final Class<?> type;
			final List<Class<?>> parameterizedTypes = new ArrayList<>();
			final Document.MappingEntry entry = element.asMappingEntry();
			switch (entry.getKey().asString()) {
			case "bind":
				field = "bind";
				type = boolean.class;
				break;
			case "destinationInetSocketAddress":
			case "destination inet socket address":
			case "destination-inet-socket-address":
			case "destination_inet_socket_address":
			case "destination":
				field = "destinationInetSocketAddress";
				type = java.net.InetSocketAddress.class;
				break;
			case "maxPacketSize":
			case "max packet size":
			case "max-packet-size":
			case "max_packet_size":
				field = "maxPacketSize";
				type = int.class;
				break;
			case "reuse":
				field = "reuse";
				type = boolean.class;
				break;
			case "sourceInetAddress":
			case "source inet address":
			case "source-inet-address":
			case "source_inet_address":
			case "source":
			case "interface":
				field = "sourceInetAddress";
				type = java.net.InetAddress.class;
				break;
			case "timeToLive":
			case "time to live":
			case "time-to-live":
			case "time_to_live":
			case "ttl":
			case "TTL":
				field = "timeToLive";
				type = java.lang.Integer.class;
				break;
			case "timeout":
			case "time out":
			case "time-out":
			case "time_out":
				field = "timeout";
				type = java.lang.Integer.class;
				break;
			case "trafficClass":
			case "traffic class":
			case "traffic-class":
			case "traffic_class":
				field = "trafficClass";
				type = java.lang.String.class;
				break;
			default:
				throw new IllegalArgumentException(String.format("No field named %s", entry.getKey()));
			}
			fieldsFactories.put(field, FieldInitFactory.create(entry.getValue(), type, parameterizedTypes, UdpIpSocketFactoryInitializer.class.getClassLoader()));
		}
	}

	public boolean getBind() {
		return (boolean) fieldsFactories.get("bind").create(null);
	}

	public boolean hasBind() {
		return fieldsFactories.containsKey("bind");
	}

	public java.net.InetSocketAddress getDestinationInetSocketAddress() {
		return (java.net.InetSocketAddress) fieldsFactories.get("destinationInetSocketAddress").create(null);
	}

	public int getMaxPacketSize() {
		return (int) fieldsFactories.get("maxPacketSize").create(null);
	}

	public boolean getReuse() {
		return (boolean) fieldsFactories.get("reuse").create(null);
	}

	public boolean hasReuse() {
		return fieldsFactories.containsKey("reuse");
	}

	public java.net.InetAddress getSourceInetAddress() {
		return (java.net.InetAddress) fieldsFactories.get("sourceInetAddress").create(null);
	}

	public boolean hasSourceInetAddress() {
		return fieldsFactories.containsKey("sourceInetAddress");
	}

	public java.lang.Integer getTimeToLive() {
		return (java.lang.Integer) fieldsFactories.get("timeToLive").create(null);
	}

	public boolean hasTimeToLive() {
		return fieldsFactories.containsKey("timeToLive");
	}

	public java.lang.Integer getTimeout() {
		return (java.lang.Integer) fieldsFactories.get("timeout").create(null);
	}

	public boolean hasTimeout() {
		return fieldsFactories.containsKey("timeout");
	}

	public java.lang.String getTrafficClass() {
		return (java.lang.String) fieldsFactories.get("trafficClass").create(null);
	}

	public boolean hasTrafficClass() {
		return fieldsFactories.containsKey("trafficClass");
	}
}
