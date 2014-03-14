package net.aeten.core.spi.factory;

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
class ScheduledExecutorFactoryInit {
	private final Map<String, Factory<Object, Void>> fieldsFactories;

	public ScheduledExecutorFactoryInit(SpiConfiguration configuration) {
		fieldsFactories = new HashMap<>();
		for (Document.Element element: configuration.root.asSequence()) {
			final String field;
			final Class<?> type;
			final List<Class<?>> parameterizedTypes = new ArrayList<>();
			final Document.MappingEntry entry = element.asMappingEntry();
			switch (entry.getKey().asString()) {
			case "corePoolSize":
			case "core pool size":
			case "core-pool-size":
			case "core_pool_size":
				field = "corePoolSize";
				type = int.class;
				break;
			case "threadFactory":
			case "thread factory":
			case "thread-factory":
			case "thread_factory":
				field = "threadFactory";
				type = java.util.concurrent.ThreadFactory.class;
				break;
			default:
				throw new IllegalArgumentException(String.format("No field named %s", entry.getKey()));
			}
			fieldsFactories.put(field, FieldInitFactory.create(entry.getValue(), type, parameterizedTypes, ScheduledExecutorFactoryInit.class.getClassLoader()));
		}
	}

	public int getCorePoolSize() {
		return (int) fieldsFactories.get("corePoolSize").create(null);
	}

	public java.util.concurrent.ThreadFactory getThreadFactory() {
		return (java.util.concurrent.ThreadFactory) fieldsFactories.get("threadFactory").create(null);
	}

	public boolean hasThreadFactory() {
		return fieldsFactories.containsKey("threadFactory");
	}
}