package net.aeten.core.event;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FutureDone<Data extends EventData<?, ?>> implements Future<Data> {
	private final Data data;

	public FutureDone(Data data) {
		this.data = data;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public Data get() throws InterruptedException, ExecutionException {
		return data;
	}

	@Override
	public Data get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return data;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

}
