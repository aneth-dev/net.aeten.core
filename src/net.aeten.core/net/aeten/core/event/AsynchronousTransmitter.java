package net.aeten.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.aeten.core.Factory;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Thomas Pérennou
 */
@ThreadSafe
public class AsynchronousTransmitter<Event, Data extends EventData <?, Event>> implements
		TransmitterService <Event, Data> {

	private final static Logger LOGGER = LoggerFactory.getLogger (AsynchronousTransmitter.class);

	private final String identifier;
	private final Factory <ExecutorService, Void> executorServiceFactory;
	private volatile ExecutorService executorService;
	private final RegisterableTransmitter <Event, Data> transmitter;

	AsynchronousTransmitter (	String identifier,
										Event[] events) {
		this (identifier, new SynchronousRegisterableTransmitter <Event, Data> (events), true);
	}

	AsynchronousTransmitter (	String identifier,
										RegisterableTransmitter <Event, Data> transmitter) {
		this (identifier, transmitter, true);
	}

	AsynchronousTransmitter (	String identifier,
										RegisterableTransmitter <Event, Data> transmitter,
										boolean autoStart,
										ExecutorService executorService) {
		this.identifier = identifier;
		this.executorServiceFactory = null;
		this.executorService = executorService;
		this.transmitter = transmitter;
	}

	AsynchronousTransmitter (	String identifier,
										RegisterableTransmitter <Event, Data> transmitter,
										boolean autoStart,
										Factory <ExecutorService, Void> executorServiceFactory) {
		this.identifier = identifier;
		this.executorServiceFactory = executorServiceFactory;
		this.executorService = this.executorServiceFactory.create (null);
		this.transmitter = transmitter;
	}

	AsynchronousTransmitter (	String identifier,
										RegisterableTransmitter <Event, Data> transmitter,
										boolean autoStart,
										int corePoolSize,
										int maximumPoolSize,
										long keepAliveTime,
										TimeUnit unit,
										BlockingQueue <Runnable> workQueue,
										ThreadFactory threadFactory,
										RejectedExecutionHandler rejectedExecutionHandler) {
		this (identifier, transmitter, autoStart, new ThreadPoolExecutor (corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, rejectedExecutionHandler));
	}

	AsynchronousTransmitter (	final String identifier,
										RegisterableTransmitter <Event, Data> transmitter,
										boolean autoStart) {
		this (identifier, transmitter, autoStart, 0, 1, 0L, TimeUnit.MILLISECONDS, new TransmissionTaskPriorityQueue (), new ThreadFactory () {
			private final AtomicInteger counter = new AtomicInteger (0);

			@Override
			public Thread newThread (Runnable runnable) {
				return new Thread (runnable, identifier + " (" + counter.incrementAndGet () + ")");
			}
		}, new RejectedExecutionHandler () {
			@Override
			public void rejectedExecution (	Runnable runnable,
														ThreadPoolExecutor executor) {
				LOGGER.error ("Task {} has been regected", runnable);
			}
		});
	}

	@Override
	public String getIdentifier () {
		return identifier;
	}

	@Override
	public void addEventHandler (	Handler <Data> eventHandler,
											Event... eventList) {
		transmitter.addEventHandler (eventHandler, eventList);
	}

	@Override
	public void removeEventHandler (	Handler <Data> eventHandler,
												Event... eventList) {
		transmitter.removeEventHandler (eventHandler, eventList);
	}

	@Override
	public Future <Data> transmit (Data data) {
		TransmissionTask <Data> task = new TransmissionTask <> (data, transmitter);
		executorService.execute (task);
		return task;
	}

	@Override
	public void shutdown () {
		executorService.shutdown ();
	}

	@SuppressWarnings ("unchecked")
	@Override
	public List <Data> shutdownNow () {
		List <Runnable> tasks = executorService.shutdownNow ();
		List <Data> datas = new ArrayList <> (tasks.size ());
		for (Runnable task: tasks) {
			if (task instanceof TransmissionTask) {
				datas.add (((TransmissionTask <Data>) task).data);
			}
		}
		return datas;
	}

	@Override
	public boolean isShutdown () {
		return executorService.isShutdown ();
	}

	@Override
	public boolean isTerminated () {
		return executorService.isTerminated ();
	}

	@Override
	public boolean awaitTermination (long timeout,
												TimeUnit unit) throws InterruptedException {
		return executorService.awaitTermination (timeout, unit);
	}

	@Override
	public void start () {
		if (executorService.isShutdown ()) {
			throw new IllegalStateException ("Transmitter service is already running");
		}
		if (executorServiceFactory == null) {
			throw new UnsupportedOperationException ("None executor service factory given");
		}
		executorService = executorServiceFactory.create (null);
	}

}
