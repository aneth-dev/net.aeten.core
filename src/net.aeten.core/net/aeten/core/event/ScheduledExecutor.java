package net.aeten.core.event;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Identifiable;
import net.aeten.core.util.StringUtil;

public class ScheduledExecutor implements ScheduledExecutorService, Identifiable {
	private static final String IDENTIFIER_OPTION = "--identifier";
	private static final String CORE_POOL_SIZE_OPTION = "--core-pool-size";
	private final String identifier;
	private final ScheduledExecutorService executor;

	private static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger threadCount = new AtomicInteger(0);
		private final String prefix;

		public DefaultThreadFactory(String prefix) {
			this.prefix = prefix + "-" + threadCount.incrementAndGet();
		}

		@Override
		public Thread newThread(Runnable runnable) {
			return new Thread(runnable, this.prefix);
		}
	};
	
	public ScheduledExecutor(String configuration) throws ConfigurationException {
		String[] arguments = StringUtil.splitWithQuote(configuration);
		String id = null;
		int corePoolSize = 1;
		try {
			for (int i = 0; i < arguments.length; i++) {
				if (arguments[i].equals(IDENTIFIER_OPTION)) {
					id = arguments[++i];
				} else if (arguments[i].equals(CORE_POOL_SIZE_OPTION)) {
					corePoolSize = Integer.parseInt(arguments[++i]);
				}
			}
		} catch (Exception exception) {
			throw new ConfigurationException(configuration, exception);
		}
		if (id == null) {
			throw new ConfigurationException(configuration);
		}
		this.identifier = id;
		this.executor = Executors.newScheduledThreadPool(corePoolSize, new DefaultThreadFactory(this.identifier));
	}

	public ScheduledExecutor(String identifier, int corePoolSize) {
		this.identifier = identifier;
		this.executor = Executors.newScheduledThreadPool(corePoolSize, new DefaultThreadFactory(this.identifier));
	}

	public ScheduledExecutor(String identifier, int corePoolSize, ThreadFactory threadFactory) {
		this.identifier = identifier;
		this.executor = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
	}

	public ScheduledExecutor(String identifier, int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		this.identifier = identifier;
		this.executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return this.executor.schedule(command, delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return this.executor.schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return this.executor.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return this.executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.executor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.executor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.executor.invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		return this.executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return this.executor.isTerminated();
	}

	@Override
	public void shutdown() {
		this.executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return shutdownNow();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.executor.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.executor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return this.executor.submit(task, result);
	}

	@Override
	public void execute(Runnable command) {
		this.executor.execute(command);
	}
}
