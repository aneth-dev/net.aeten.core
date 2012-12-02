package net.aeten.core.spi.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import net.aeten.core.parsing.Document;
import net.aeten.core.spi.FieldInit;
import net.aeten.core.spi.Provider;
import net.aeten.core.spi.SpiConfiguration;
import net.aeten.core.spi.SpiFactory;
import net.aeten.core.spi.SpiInitializer;

@Provider (SpiFactory.class)
public class ScheduledExecutorFactory implements
		SpiFactory <ScheduledExecutorService, Document.Element> {

	@SuppressWarnings ("unused")
	private static class Init {
		@FieldInit
		private final int corePoolSize;
		@FieldInit (required = false)
		private final ThreadFactory threadFactory;

		/* TODO */
		//	RejectedExecutionHandler handler

		Init (@SpiInitializer (generate = false) ScheduledExecutorFactoryInit init) {
			corePoolSize = init.getCorePoolSize ();
			threadFactory = init.hasThreadFactory ()? init.getThreadFactory (): null;
		}
	}

	@Override
	public Class <?>[] getTypes () {
		return new Class[] {
			ScheduledExecutorService.class
		};
	}

	@Override
	public Class <Document.Element> getParameterType () {
		return Document.Element.class;
	}

	@Override
	public ScheduledExecutorService create (Document.Element configuration) {
		ScheduledExecutorFactoryInit init = new ScheduledExecutorFactoryInit (new SpiConfiguration (configuration));
		return Executors.newScheduledThreadPool (init.getCorePoolSize (), init.getThreadFactory ());
	}
}