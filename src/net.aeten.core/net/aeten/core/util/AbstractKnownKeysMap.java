package net.aeten.core.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.aeten.core.Factory;

public abstract class AbstractKnownKeysMap<K, V> implements Map<K, V>, Cloneable, Serializable {
	private static final long serialVersionUID = -7242319824890394363L;
	protected final K[] keys;
	protected final Factory<Integer, K> ordinal;

	public AbstractKnownKeysMap(K[] keys, Factory<Integer, K> ordinal) {
		this.keys = keys.clone();
		this.ordinal = ordinal;
	}

	protected abstract void incrementSize();

	protected abstract void decrementSize();

	protected abstract void setValue(int keyOrdinal, V newValue);

	protected abstract V getValue(int keyOrdinal);

	protected abstract V getAndSetValue(int keyOrdinal, V newValue);

	protected abstract boolean compareAndSetValue(int keyOrdinal, V expect, V update);

	@SuppressWarnings("unchecked")
	public boolean remove(Object key, Object value) {
		if (!keys.getClass().getComponentType().isAssignableFrom(key.getClass())) { return false; }
		return compareAndSetValue(ordinal.create((K) key), (V) value, null);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K, V>>() {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry)) { return false; }
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				V v = AbstractKnownKeysMap.this.get(e.getKey());
				return (v != null) && v.equals(e.getValue());
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry)) { return false; }
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				return AbstractKnownKeysMap.this.remove(e.getKey(), e.getValue());
			}

			@Override
			public int size() {
				return AbstractKnownKeysMap.this.size();
			}

			@Override
			public void clear() {
				AbstractKnownKeysMap.this.clear();
			}

		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		if (!keys.getClass().getComponentType().isAssignableFrom(key.getClass())) { return false; }
		return getValue(ordinal.create((K) key)) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (int i = 0; i < keys.length; i++) {
			if (value.equals(getValue(i))) { return true; }
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		if (!keys.getClass().getComponentType().isAssignableFrom(key.getClass())) { return null; }
		return getValue(ordinal.create((K) key));
	}

	@Override
	public V put(K key, V value) {
		return getAndSetValue(ordinal.create(key), value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		if (!keys.getClass().getComponentType().isAssignableFrom(key.getClass())) { return null; }
		return getAndSetValue(ordinal.create((K) key), null);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry: m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < keys.length; i++) {
			setValue(i, null);
		}
	}

	@Override
	public abstract Object clone();

	/**
	 * Returns a string representation of this map. The string representation
	 * consists of a list of key-value mappings in the order returned by the
	 * map's <tt>entrySet</tt> view's iterator, enclosed in braces (
	 * <tt>"{}"</tt>). Adjacent mappings are separated by the characters
	 * <tt>", "</tt> (comma and space). Each key-value mapping is rendered as
	 * the key followed by an equals sign (<tt>"="</tt>) followed by the
	 * associated value. Keys and values are converted to strings as by
	 * {@link String#valueOf(Object)}.
	 * 
	 * @return a string representation of this map
	 */
	@Override
	public String toString() {
		Iterator<Map.Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext()) { return "{}"; }

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;) {
			Map.Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this? "(this Map)": key);
			sb.append('=');
			sb.append(value == this? "(this Map)": value);
			if (!i.hasNext()) { return sb.append('}').toString(); }
			sb.append(", ");
		}
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				return new KeyIterator();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry)) { return false; }
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				V v = AbstractKnownKeysMap.this.get(e.getKey());
				return (v != null) && v.equals(e.getValue());
			}

			@Override
			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry)) { return false; }
				Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
				return AbstractKnownKeysMap.this.remove(e.getKey(), e.getValue());
			}

			@Override
			public int size() {
				return AbstractKnownKeysMap.this.size();
			}

			@Override
			public void clear() {
				AbstractKnownKeysMap.this.clear();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				return new ValueIterator();
			}

			@Override
			public int size() {
				return AbstractKnownKeysMap.this.size();
			}

			@Override
			public boolean contains(Object o) {
				return AbstractKnownKeysMap.this.containsValue(o);
			}

			@Override
			public void clear() {
				AbstractKnownKeysMap.this.clear();
			}
		};
	}

	private class KeyIterator implements Iterator<K> {
		private int index;
		private K next, current;

		KeyIterator() {
			findNext();
		}

		private K findNext() {
			if (index < keys.length) {
				int i = index++;
				if (getValue(i) == null) { return findNext(); }
				return next = keys[i];
			}
			return next = null;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public K next() {
			if (next == null) { throw new NoSuchElementException(); }
			current = next;
			findNext();
			return current;
		}

		@Override
		public void remove() {
			setValue(index, null);
		}
	}

	private class ValueIterator implements Iterator<V> {
		private int index;
		private V next, current;

		ValueIterator() {
			findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public V next() {
			if (next == null) { throw new NoSuchElementException(); }
			current = next;
			findNext();
			return current;
		}

		private V findNext() {
			V value;
			if (index++ < keys.length) {
				value = getValue(index);
				if (value == null) { return findNext(); }
				return value;
			}
			return value = null;
		}

		@Override
		public void remove() {
			setValue(index, null);
		}
	}

	private class EntryIterator implements Iterator<Map.Entry<K, V>> {
		private int index;
		private Entry next, current;

		EntryIterator() {
			findNext();
		}

		private Entry findNext() {
			V value;
			if (index < keys.length) {
				int i = index++;
				value = getValue(i);
				if (value == null) { return findNext(); }
				return next = new Entry(i, value);
			}
			return next = null;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Map.Entry<K, V> next() {
			if (next == null) { throw new NoSuchElementException(); }
			current = next;
			findNext();
			return current;
		}

		@Override
		public void remove() {
			current.setValue(null);
		}
	}

	private class Entry implements Map.Entry<K, V> {
		private final int ordinal;
		private V value;

		Entry(int ordinal, V value) {
			this.ordinal = ordinal;
			this.value = value;
		}

		@Override
		public K getKey() {
			return keys[ordinal];
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V update) {
			V previous = getAndSetValue(ordinal, update);
			value = update;
			return previous;
		}
	}

	protected void updateSize(V oldValue, V newValue) {
		if (oldValue == null) {
			if (newValue != null) {
				incrementSize();
			}
		} else {
			if (newValue == null) {
				decrementSize();
			}
		}
	}
}
