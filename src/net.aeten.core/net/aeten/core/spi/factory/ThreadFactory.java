package net.aeten.core.spi.factory;

import java.util.concurrent.atomic.AtomicInteger;

import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiFactory;

@Provider(SpiFactory.class)
public class ThreadFactory implements SpiFactory<java.util.concurrent.ThreadFactory, String> {
	private static final AtomicInteger threadCount = new AtomicInteger(0);

	@Override
	public java.util.concurrent.ThreadFactory create(String prefix) {
		final String name = prefix + "-" + threadCount.incrementAndGet();
		return new java.util.concurrent.ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, name);
			}
		};
	}

	@Override
	public Class<?>[] getTypes() {
		return new Class[] { java.util.concurrent.ThreadFactory.class
		};
	}

	@Override
	public Class<String> getParameterType() {
		return String.class;
	}
}
