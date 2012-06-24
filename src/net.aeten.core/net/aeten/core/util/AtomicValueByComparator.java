package net.aeten.core.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.aeten.core.Factory;

public final class AtomicValueByComparator<V> implements AtomicValue<V>, Serializable {
	private static final long serialVersionUID = -3328578771876336508L;
	private final Comparator<V> comparator;
	private final Lock lock;
	private V value;

	public AtomicValueByComparator(Comparator<V> comparator, V initialValue) {
		this.comparator = comparator;
		value = initialValue;
		lock = new ReentrantLock();
	}

	@Override
	public boolean compareAndSet(V expect, V update) {
		lock.lock();
		try {
			if (comparator.compare(value, expect) != 0) {
				value = update;
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V get() {
		lock.lock();
		try {
			return value;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V getAndSet(V newValue) {
		lock.lock();
		try {
			V oldValue = value;
			value = newValue;
			return oldValue;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void lazySet(V newValue) {
		if (lock.tryLock()) {
			try {
				value = newValue;
			} finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void set(V newValue) {
		lock.lock();
		try {
			value = newValue;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean weakCompareAndSet(V expect, V update) {
		if (lock.tryLock()) {
			try {
				if (comparator.compare(value, expect) == 0) {
					value = update;
					return true;
				}
				return false;
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	@Override
	public final String toString() {
		return String.valueOf(get());
	}

	@Override
	public boolean compareAndSet(V expect, Factory<V, Void> update) {
		lock.lock();
		try {
			if (comparator.compare(value, expect) == 0) {
				value = update.create(null);
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean weakCompareAndSet(V expect, Factory<V, Void> update) {
		if (lock.tryLock()) {
			try {
				if (comparator.compare(value, expect) == 0) {
					value = update.create(null);
					return true;
				}
				return false;
			} finally {
				lock.unlock();
			}
		}
		return false;
	}
}
