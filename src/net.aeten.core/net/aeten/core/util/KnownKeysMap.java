package net.aeten.core.util;

import net.aeten.core.Factory;

public class KnownKeysMap<K, V> extends
		AbstractKnownKeysMap <K, V> {
	private static final long serialVersionUID = 7581236987680272653L;
	private final Object[] values;
	private int size = 0;

	public KnownKeysMap (K[] keys,
								Factory <Integer, K> ordinal,
								final V initialValue) {
		this (keys, ordinal, new Factory <V, K> () {
			@Override
			public V create (K context) {
				return initialValue;
			}
		});
	}

	public KnownKeysMap (K[] keys,
								Factory <Integer, K> ordinal,
								Factory <V, ? super K> initialValue) {
		super (keys, ordinal);
		values = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = initialValue.create (keys[i]);
		}
	}

	public KnownKeysMap (K[] keys,
								Factory <Integer, K> ordinal) {
		this (keys, ordinal, (V) null);
	}

	private KnownKeysMap (KnownKeysMap <K, V> map) {
		super (map.keys, map.ordinal);
		values = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = map.values[i];
		}
	}

	@Override
	public int size () {
		return size;
	}

	@Override
	public boolean isEmpty () {
		return size == 0;
	}

	@Override
	protected void incrementSize () {
		size++;
	}

	@Override
	protected void decrementSize () {
		size--;
	}

	@Override
	protected void setValue (	int keyOrdinal,
										V newValue) {
		values[keyOrdinal] = newValue;
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected V getValue (int keyOrdinal) {
		return (V) values[keyOrdinal];
	}

	@Override
	protected V getAndSetValue (	int keyOrdinal,
											V newValue) {
		@SuppressWarnings ("unchecked")
		V pevious = (V) values[keyOrdinal];
		values[keyOrdinal] = newValue;
		return pevious;
	}

	@Override
	protected boolean compareAndSetValue (	int keyOrdinal,
														V expect,
														V update) {
		if ((expect == update) && (expect == values[keyOrdinal])) {
			// No change
			return true;
		}
		if ((update == null) || (expect == null) || (expect.equals (values[keyOrdinal]))) {
			values[keyOrdinal] = update;
			return true;
		}
		return false;
	}

	@Override
	public Object clone () {
		return new KnownKeysMap <K, V> (this);
	}

}
