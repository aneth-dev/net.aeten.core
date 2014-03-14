package net.aeten.core.event;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

class TransmissionTask<Data extends EventData<?, ?>> extends FutureTask<Data> implements Comparable<TransmissionTask<Data>> {
	public final Data data;
	public final int priority;
	private final long time;

	public TransmissionTask(final Data data, final Transmitter<Data> transmitter) {
		super(new Callable<Data>() {
			@Override
			public Data call() throws Exception {
				transmitter.transmit(data);
				return data;
			}
		});
		this.time = System.nanoTime();
		this.data = data;
		this.priority = this.data.getPriority().ordinal();
	}

	@Override
	public int compareTo(TransmissionTask<Data> other) {
		long delta = other.time - time;
		return (priority == other.priority)? ((other.time == time)? 0: ((time > 0)? (delta < 0): (delta > 0))? 1: -1): (priority > other.priority)? 1: -1;
	}

}
