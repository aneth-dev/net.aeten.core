package net.aeten.core.event;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransmissionTaskPriorityQueue extends
		AbstractQueue <Runnable> implements
		BlockingQueue <Runnable> {

	private final BlockingQueue <TransmissionTask <?>>[] queues;
	private final ReentrantLock lock = new ReentrantLock ();
	private final Condition notEmpty = lock.newCondition ();
	private final Object[] priorityArray;
	private boolean empty = true;

	public TransmissionTaskPriorityQueue () {
		this (Priority.values ());
	}

	@SuppressWarnings ("unchecked")
	public <P> TransmissionTaskPriorityQueue (P[] priorityArray) {
		this.priorityArray = priorityArray.clone ();
		queues = new LinkedBlockingQueue[priorityArray.length];
		for (int i = 0; i < this.priorityArray.length; i++) {
			queues[i] = new LinkedBlockingQueue <TransmissionTask <?>> ();
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public Iterator <Runnable> iterator () {

		return new Iterator <Runnable> () {
			private final Iterator <TransmissionTask <?>>[] iterators;
			private int iterator = 0;
			{
				iterators = new Iterator[priorityArray.length];
				for (Priority priority: Priority.values ()) {
					iterators[priority.ordinal ()] = queues[priority.ordinal ()].iterator ();
				}
			}

			@Override
			public boolean hasNext () {
				return hasNext (0);
			}

			private boolean hasNext (int index) {
				iterator = index;
				if (iterators[iterator].hasNext ()) {
					return true;
				}
				if (++iterator == iterators.length) {
					return false;
				}
				return hasNext (iterator);
			}

			@Override
			public TransmissionTask <?> next () {
				return iterators[iterator].next ();
			}

			@Override
			public void remove () {
				iterators[iterator].remove ();
			}

		};
	}

	@Override
	public int size () {
		int size = 0;
		for (int i = 0; i < priorityArray.length; i++) {
			size += queues[i].size ();
		}
		return size;
	}

	@Override
	public int drainTo (Collection <? super Runnable> collection) {
		int count = 0;
		for (Runnable task: this) {
			// TODO remove element
			collection.add (task);
			count++;
		}
		return count;
	}

	@Override
	public int drainTo (	Collection <? super Runnable> collection,
								int maxElements) {
		int count = 0;
		for (Runnable task: this) {
			if ((count + 1) == maxElements) {
				return count;
			}
			// TODO remove element
			collection.add (task);
			count++;
		}
		return count;
	}

	@Override
	public boolean offer (Runnable task) {
		if (task instanceof TransmissionTask) {
			TransmissionTask <?> transmissionTask = (TransmissionTask <?>) task;
			boolean added = queues[transmissionTask.priority].offer (transmissionTask);
			lock.lock ();
			try {
				empty = false;
				notEmpty.signal ();
			} finally {
				lock.unlock ();
			}
			return added;
		}
		return false;
	}

	@Override
	public boolean offer (	Runnable task,
									long timeout,
									TimeUnit unit) throws InterruptedException {
		if (task instanceof TransmissionTask) {
			TransmissionTask <?> transmissionTask = (TransmissionTask <?>) task;
			boolean added = queues[transmissionTask.priority].offer (transmissionTask, timeout, unit);
			lock.lock ();
			try {
				if (empty) {
					notEmpty.signal ();
				}
			} finally {
				lock.unlock ();
			}
			return added;
		}
		return false;
	}

	@Override
	public Runnable poll (	long timeout,
									TimeUnit unit) throws InterruptedException {
		lock.lockInterruptibly ();
		try {
			if (empty) {
				notEmpty.await (timeout, unit);
			}
		} finally {
			lock.unlock ();
		}
		Runnable task = null;
		for (int i = 0; i < priorityArray.length; i++) {
			task = queues[i].poll ();
			if (task != null) {
				break;
			}
		}
		return task;
	}

	@Override
	public void put (Runnable task) throws InterruptedException {
		if (task instanceof TransmissionTask) {
			TransmissionTask <?> transmissionTask = (TransmissionTask <?>) task;
			queues[transmissionTask.priority].put (transmissionTask);
			lock.lock ();
			try {
				empty = false;
				notEmpty.signal ();
			} finally {
				lock.unlock ();
			}
		}
		throw new IllegalArgumentException ("Element must be instanceof " + TransmissionTask.class.getName ());
	}

	@Override
	public int remainingCapacity () {
		int remainingCapacity = 0;
		for (int i = 0; i < priorityArray.length; i++) {
			remainingCapacity += queues[i].remainingCapacity ();
		}
		return remainingCapacity;
	}

	@Override
	public Runnable take () throws InterruptedException {
		lock.lockInterruptibly ();
		try {
			if (empty) {
				notEmpty.await ();
			}
		} finally {
			lock.unlock ();
		}
		Runnable task = null;
		for (int i = 0; i < priorityArray.length; i++) {
			task = queues[i].poll ();
			if (task != null) {
				break;
			}
		}
		return task;
	}

	@Override
	public Runnable peek () {
		Runnable task = null;
		for (int i = 0; i < priorityArray.length; i++) {
			task = queues[i].peek ();
			if (task != null) {
				break;
			}
		}
		return task;
	}

	@Override
	public Runnable poll () {
		Runnable task = null;
		for (int i = 0; i < priorityArray.length; i++) {
			task = queues[i].poll ();
			if (task != null) {
				break;
			}
		}
		return task;
	}
}
