package net.aeten.core.util;

import java.util.HashMap;
import java.util.Map;

import net.aeten.core.EnumElement;
import net.aeten.core.Factory;

public final class Collections {
	private Collections() {}

	// ////////// Map

	public static <K extends Enum<?>, V>Map<K, V> map(Class<K> enumClass) {
		return map(enumClass.getEnumConstants());
	}

	public static <K extends Enum<?>, V>Map<K, V> filledMap(Class<K> enumClass, final V initialValue) {
		// XXX Should use EnumMap ?
		return new KnownKeysMap<K, V>(enumClass.getEnumConstants(), new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, initialValue);
	}

	public static <K extends Enum<?>, V>Map<K, V> filledMap(Class<K> enumClass, Factory<V, ? super K> initialValue) {
		return new KnownKeysMap<K, V>(enumClass.getEnumConstants(), new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((Enum<?>) context).ordinal();
			}
		}, initialValue);
	}

	public static <K, V>Map<K, V> filledMap(K[] keys, final V initialValue) {
		Class<?> type = keys.getClass().getComponentType();
		if (type.isEnum()) {
			// XXX Should use EnumMap ?
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((Enum<?>) context).ordinal();
				}
			}, initialValue);
		} else if (EnumElement.class.isAssignableFrom(type)) {
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((EnumElement<?>) context).ordinal();
				}
			}, initialValue);
		} else {
			HashMap<K, V> map = new HashMap<K, V>(keys.length);
			for (K key: keys) {
				map.put(key, initialValue);
			}
			return map;
		}
	}

	public static <K, V>Map<K, V> filledMap(K[] keys, Factory<V, ? super K> initialValue) {
		Class<?> type = keys.getClass().getComponentType();
		if (type.isEnum()) {
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((Enum<?>) context).ordinal();
				}
			}, initialValue);
		} else if (EnumElement.class.isAssignableFrom(type)) {
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((EnumElement<?>) context).ordinal();
				}
			}, initialValue);
		} else {
			HashMap<K, V> map = new HashMap<K, V>(keys.length);
			for (K key: keys) {
				map.put(key, initialValue.create(key));
			}
			return map;
		}
	}

	public static <K extends EnumElement<K>, V>Map<K, V> filledMap(K[] keys, Factory<V, ? super K> initialValue) {
		return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
			@Override
			public Integer create(K context) {
				return ((EnumElement<?>) context).ordinal();
			}
		}, initialValue);
	}

	public static <K, V>Map<K, V> map(K[] keys, Factory<Integer, K> ordinal) {
		return new KnownKeysMap<K, V>(keys, ordinal);
	}

	public static <K, V>Map<K, V> filledMap(K[] keys, Factory<Integer, K> ordinal, V initialValue) {
		return new KnownKeysMap<K, V>(keys, ordinal, initialValue);
	}

	public static <K, V>Map<K, V> map(K[] keys) {
		Class<?> type = keys.getClass().getComponentType();
		Factory<V, K> initialValue = new Factory<V, K>() {
			@Override
			public V create(K context) {
				return null;
			}
		};
		if (type.isEnum()) {
			// XXX Should use EnumMap ?
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((Enum<?>) context).ordinal();
				}
			}, initialValue);
		} else if (EnumElement.class.isAssignableFrom(type)) {
			return new KnownKeysMap<K, V>(keys, new Factory<Integer, K>() {
				@Override
				public Integer create(K context) {
					return ((EnumElement<?>) context).ordinal();
				}
			}, initialValue);
		} else {
			return new HashMap<K, V>(keys.length);
		}
	}

}
