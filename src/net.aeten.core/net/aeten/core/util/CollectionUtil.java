package net.aeten.core.util;

import java.util.concurrent.ConcurrentMap;

public class CollectionUtil {
	private CollectionUtil() {};
	
	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
		V previous = map.putIfAbsent(key, value);
		if (previous != null) {
			value = previous;
		}
		return value;
	}

}
