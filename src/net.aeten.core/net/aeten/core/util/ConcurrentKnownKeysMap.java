package net.aeten.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.aeten.core.Factory;
import net.aeten.core.util.Concurrents.AtomicComparator;

public class ConcurrentKnownKeysMap<K, V> extends AbstractKnownKeysMap<K, V> implements ConcurrentMap<K, V> {
	private static final long serialVersionUID = 1347528148487196108L;
	private final AtomicComparator comparator;
	private final AtomicArray<V> values;
	private final transient AtomicInteger size = new AtomicInteger();

	public ConcurrentKnownKeysMap(K[] keys, Factory<Integer, K> ordinal, AtomicComparator comparator, final V initialValue) {
		this(keys, ordinal, comparator, new Factory<V, K>() {
			@Override
			public V create(K context) {
				return initialValue;
			}
		});
	}

	public ConcurrentKnownKeysMap(K[] keys, Factory<Integer, K> ordinal, AtomicComparator comparator, Factory<V, ? super K> initialValue) {
		super(keys, ordinal);
		this.comparator = comparator;
		values = Concurrents.<V>atomicArray(comparator, keys.length);
		for (int i = 0; i < keys.length; i++) {
			setValue(i, initialValue.create(keys[i]));
		}
	}

	public ConcurrentKnownKeysMap(K[] keys, Factory<Integer, K> ordinal, AtomicComparator comparator) {
		this(keys, ordinal, comparator, (V) null);
	}

	private ConcurrentKnownKeysMap(ConcurrentKnownKeysMap<K, V> map) {
		super(map.keys, map.ordinal);
		comparator = map.comparator;
		values = Concurrents.<V>atomicArray(comparator, keys.length);
		for (int i = 0; i < keys.length; i++) {
			setValue(i, map.values.get(i));
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		int index = ordinal.create(key);
		return compareAndSetValue(index, null, value)? null: getValue(index);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return compareAndSetValue(ordinal.create(key), oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		return getAndSetValue(ordinal.create(key), value);
	}

	@Override
	protected void setValue(int keyOrdinal, V newValue) {
		updateSize(values.getAndSet(keyOrdinal, newValue), newValue);
	}

	@Override
	protected V getValue(int keyOrdinal) {
		return values.get(keyOrdinal);
	}

	@Override
	protected V getAndSetValue(int keyOrdinal, V newValue) {
		V previous = values.getAndSet(keyOrdinal, newValue);
		updateSize(previous, newValue);
		return previous;
	}

	@Override
	protected boolean compareAndSetValue(int keyOrdinal, V expect, V update) {
		if (values.compareAndSet(keyOrdinal, expect, update)) {
			updateSize(expect, update);
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		return size.get();
	}

	@Override
	public boolean isEmpty() {
		return size.get() == 0;
	}

	@Override
	protected void incrementSize() {
		size.decrementAndGet();
	}

	@Override
	protected void decrementSize() {
		size.decrementAndGet();
	}

	@Override
	public Object clone() {
		return new ConcurrentKnownKeysMap<K, V>(this);
	}

	public static void main(String[] args) {
		test("warmup");
		test("warmup2");
		for (int i = 1; i <= 1024; i += i) {
			test(i + " concurrency");
		}
	}

	private static void test(String description) {
		Integer[] ints = new Integer[2000];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = i;
		}
		Map<Integer, Integer> map = new ConcurrentKnownKeysMap<Integer, Integer>(ints, new Factory<Integer, Integer>() {
			@Override
			public Integer create(Integer context) {
				return context;
			}
		}, AtomicComparator.EQUALS);
		// Map<Integer, Integer> map = new ConcurrentHashMap<Integer,
		// Integer>();
		long start = System.nanoTime();
		for (int i = 0; i < 20 * 1000 * 1000; i += ints.length) {
			for (Integer j: ints) {
				map.put(j, 1);
				map.get(j);
			}
		}
		long time = System.nanoTime() - start;
		System.out.println(description + ": Average access time " + (time / 20 / 1000 / 1000 / 2) + " ns.");
	}
}
