package net.aeten.core.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayUtil {
	private ArrayUtil() {}

	public static <T>Enumeration<T> enumeration(final T[] array) {
		return new Enumeration<T>() {
			private int index;

			@Override
			public boolean hasMoreElements() {
				return index < array.length;
			}

			@Override
			public T nextElement() {
				if (index < array.length) { return array[index++]; }
				throw new NoSuchElementException();
			}
		};
	}

	public static <Y, T extends Y>Iterator<Y> iterator(final T[] array) {
		return new Iterator<Y>() {
			private int index;

			@Override
			public boolean hasNext() {
				return index < array.length;
			}

			@Override
			public Y next() {
				if (index < array.length) { return array[index++]; }
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
