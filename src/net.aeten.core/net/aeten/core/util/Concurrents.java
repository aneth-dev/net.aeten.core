package net.aeten.core.util;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import net.aeten.core.EnumElement;
import net.aeten.core.Factory;

public class Concurrents {
	private Concurrents() {}

	// /////////// AtomicValue

	public enum AtomicComparator {
		EQUALS,
		REFESENCE
	}

	/**
	 * Creates a new AtomicValue with the given initial value.
	 * 
	 * @param initialValue
	 *           the initial value
	 * @param comparator
	 *           the value comparator
	 */
	public static <V>AtomicValue<V> atomicValue(V initialValue, Comparator<V> comparator) {
		return new AtomicValueByComparator<V>(comparator, initialValue);
	}

	public static <V>AtomicValue<V> atomicValue(V initialValue, AtomicComparator comparator) {
		switch (comparator) {
		case EQUALS:
			return new AtomicValueByComparator<V>(new Comparator<V>() {
				@Override
				public int compare(V o1, V o2) {
					return (o1 == null)? ((o2 == null)? 0: 1): (o1.equals(o2)? 0: 1);
				}
			}, initialValue);
		case REFESENCE:
		default:
			return new AtomicValueByReference<V>(initialValue);
		}
	}

	public static <V extends Comparable<V>>AtomicValue<V> atomicValue(V initialValue) {
		return new AtomicValueByComparator<V>(new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return (o1 == null)? ((o2 == null)? 0: 1): o1.compareTo(o2);
			}
		}, initialValue);
	}

	public static <V>AtomicValue<V> atomicValue(V initialValue) {
		return new AtomicValueByReference<V>(initialValue);
	}

	/**
	 * Creates a new AtomicValue with null initial value.
	 */
	public static <V>AtomicValue<V> atomicValue(AtomicComparator comparator) {
		switch (comparator) {
		case EQUALS:
			return new AtomicValueByComparator<V>(new Comparator<V>() {
				@Override
				public int compare(V o1, V o2) {
					return (o1 == null)? ((o2 == null)? 0: 1): (o1.equals(o2)? 0: 1);
				}
			}, null);
		case REFESENCE:
		default:
			return new AtomicValueByReference<V>(null);
		}
	}

	/**
	 * Creates a new AtomicValue with null initial value.
	 * 
	 * @param comparator
	 *           the value comparator
	 */
	public static <V>AtomicValue<V> atomicValue(Comparator<V> comparator) {
		return new AtomicValueByComparator<V>(comparator, null);
	}

	// AtomicArray /////////////////////////////

	public static <V>AtomicArray<V> atomicArray(V[] initialElements, Comparator<V> comparator) {
		return new AtomicArrayByComparator<V>(comparator, initialElements);
	}

	public static <V>AtomicArray<V> atomicArray(V[] initialElements, AtomicComparator comparator) {
		switch (comparator) {
		case EQUALS:
			return new AtomicArrayByComparator<V>(new Comparator<V>() {
				@Override
				public int compare(V o1, V o2) {
					return (o1 == null)? ((o2 == null)? 0: 1): (o1.equals(o2)? 0: 1);
				}
			}, initialElements);
		case REFESENCE:
		default:
			return new AtomicArrayByReference<V>(initialElements);
		}
	}

	public static <V extends Comparable<V>>AtomicArray<V> atomicArray(V[] initialElements) {
		return new AtomicArrayByComparator<V>(new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return (o1 == null)? ((o2 == null)? 0: 1): o1.compareTo(o2);
			}
		}, initialElements);
	}

	public static <V>AtomicArray<V> atomicArray(V[] initialElements) {
		return new AtomicArrayByReference<V>(initialElements);
	}

	/**
	 * Creates a new AtomicValue with null initial value.
	 */
	public static <V>AtomicArray<V> atomicArray(AtomicComparator comparator, int length) {
		switch (comparator) {
		case EQUALS:
			return new AtomicArrayByComparator<V>(new Comparator<V>() {
				@Override
				public int compare(V o1, V o2) {
					return (o1 == null)? ((o2 == null)? 0: 1): (o1.equals(o2)? 0: 1);
				}
			}, length);
		case REFESENCE:
		default:
			return new AtomicArrayByReference<V>(length);
		}
	}

	/**
	 * Creates a new AtomicValue with null initial value.
	 * 
	 * @param comparator
	 *           the value comparator
	 */
	public static <V>AtomicArray<V> atomicArray(Comparator<V> comparator, int length) {
		return new AtomicArrayByComparator<V>(comparator, length);
	}

	// ////////// Map

	public static <K, V>V putIfAbsentAndGet(ConcurrentMap<K, V> map, K key, V value) {
		V previous = map.putIfAbsent(key, value);
		return (previous == null)? value: previous;
	}

	public static <K, V>V putIfAbsentAndGet(ConcurrentMap<K, V> map, K key, Factory<V, ? super K> valueFactory) {
		V previous = map.get(key);
		if (previous == null) {
			V update = valueFactory.create(key);
			previous = map.putIfAbsent(key, update);
			return (previous == null)? update: previous;
		}
		return previous;
	}

	/** Thread unsafe */
	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, Factory<V, ? super K> valueFactory) {
		V previous = map.get(key);
		if (previous == null) {
			V update = valueFactory.create(key);
			map.put(key, update);
			return update;
		}
		return previous;
	}

	/** Thread unsafe */
	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, V update) {
		V previous = map.get(key);
		if (previous == null) {
			map.put(key, update);
			return update;
		}
		return previous;
	}

	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, V update, Object lock) {
		synchronized (lock) {
			return putIfAbsentAndGet(map, key, update);
		}
	}

	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, V update, Lock lock) {
		lock.lock();
		try {
			return putIfAbsentAndGet(map, key, update);
		} finally {
			lock.unlock();
		}
	}

	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, Factory<V, ? super K> valueFactory, Object lock) {
		synchronized (lock) {
			return putIfAbsentAndGet(map, key, valueFactory);
		}
	}

	public static <K, V>V putIfAbsentAndGet(Map<K, V> map, K key, Factory<V, ? super K> valueFactory, Lock lock) {
		lock.lock();
		try {
			return putIfAbsentAndGet(map, key, valueFactory);
		} finally {
			lock.unlock();
		}
	}

	public static <K extends Enum<?>, V>ConcurrentMap<K, V> concurrentMap(AtomicComparator comparator, Class<K> enumClass) {
		return concurrentMap(comparator, enumClass.getEnumConstants());
	}

	public static <K extends Enum<?>, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, Class<K> enumClass, final V initialValue) {
		return new ConcurrentKnownKeysMap<K, V>(enumClass.getEnumConstants(), new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, comparator, initialValue);
	}

	public static <K extends Enum<?>, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, Class<K> enumClass, Factory<V, ? super K> initialValue) {
		return new ConcurrentKnownKeysMap<K, V>(enumClass.getEnumConstants(), new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, comparator, initialValue);
	}

	public static <K, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, K[] keys, final V initialValue) {
		Class<?> type = keys.getClass().getComponentType();
		if (type.isEnum()) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else if (EnumElement.class.isAssignableFrom(type)) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((EnumElement<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else {
			ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>(keys.length);
			for (K key: keys) {
				map.put(key, initialValue);
			}
			return map;
		}
	}

	public static <K, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, K[] keys, Factory<V, ? super K> initialValue) {
		Class<?> type = keys.getClass().getComponentType();
		if (type.isEnum()) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else if (EnumElement.class.isAssignableFrom(type)) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((EnumElement<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else {
			ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>(keys.length);
			for (K key: keys) {
				map.put(key, initialValue.create(key));
			}
			return map;
		}
	}

	public static <K extends EnumElement<K>, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, K[] keys, Factory<V, ? super K> initialValue) {
		return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((EnumElement<?>) context).ordinal();
			}
		}, comparator, initialValue);
	}

	public static <K, V>ConcurrentMap<K, V> concurrentMap(AtomicComparator comparator, K[] keys, Factory<Integer, K> ordinal) {
		return new ConcurrentKnownKeysMap<K, V>(keys, ordinal, comparator);
	}

	public static <K, V>ConcurrentMap<K, V> concurrentFilledMap(AtomicComparator comparator, K[] keys, Factory<Integer, K> ordinal, V initialValue) {
		return new ConcurrentKnownKeysMap<K, V>(keys, ordinal, comparator, initialValue);
	}

	public static <K, V>ConcurrentMap<K, V> concurrentMap(AtomicComparator comparator, K[] keys) {
		Class<?> type = keys.getClass().getComponentType();
		Factory<V, K> initialValue = new Factory<V, K>() {
			@Override
			public V create(K context) {
				return null;
			}
		};
		if (type.isEnum()) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else if (EnumElement.class.isAssignableFrom(type)) return new ConcurrentKnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((EnumElement<?>) context).ordinal();
			}
		}, comparator, initialValue);
		else return new ConcurrentHashMap<K, V>(keys.length);
	}

}
