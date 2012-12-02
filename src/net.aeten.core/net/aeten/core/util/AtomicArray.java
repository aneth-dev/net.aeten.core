package net.aeten.core.util;

import net.aeten.core.Factory;

public interface AtomicArray<E> {

	/**
	 * Returns the length of the array.
	 * 
	 * @return the length of the array
	 */
	public int length ();

	/**
	 * Gets the current value at position {@code i}.
	 * 
	 * @param i
	 *            the index
	 * @return the current value
	 */
	public E get (int i);

	/**
	 * Sets the element at position {@code i} to the given value.
	 * 
	 * @param i
	 *            the index
	 * @param newValue
	 *            the new value
	 */
	public void set (	int i,
							E newValue);

	/**
	 * Eventually sets the element at position {@code i} to the given value.
	 * 
	 * @param i
	 *            the index
	 * @param newValue
	 *            the new value
	 */
	public void lazySet (int i,
								E newValue);

	/**
	 * Atomically sets the element at position {@code i} to the given value and
	 * returns the old value.
	 * 
	 * @param i
	 *            the index
	 * @param newValue
	 *            the new value
	 * @return the previous value
	 */
	public E getAndSet (	int i,
								E newValue);

	/**
	 * Atomically sets the element at position {@code i} to the given updated
	 * value if the current value equals the expected value. "Equals" means
	 * depends of implementation (like {@code ==} or {@code equals(Object)}).
	 * 
	 * @param i
	 *            the index
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful. False return indicates that the actual value
	 *         was not equal to the expected value.
	 */
	public boolean compareAndSet (int i,
											E expect,
											E update);

	public boolean compareAndSet (int i,
											E expect,
											Factory <E, Void> update);

	/**
	 * Atomically sets the element at position {@code i} to the given updated
	 * value if the current value equals the expected value. "Equals" means
	 * depends of implementation (like {@code ==} or {@code equals(Object)}).
	 * 
	 * <p>
	 * May fail spuriously and does not provide ordering guarantees, so is only
	 * rarely an appropriate alternative to {@code compareAndSet}.
	 * 
	 * @param i
	 *            the index
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful.
	 */
	public boolean weakCompareAndSet (	int i,
													E expect,
													E update);

	public boolean weakCompareAndSet (	int i,
													E expect,
													Factory <E, Void> update);

	/**
	 * Returns the String representation of the current values of array.
	 * 
	 * @return the String representation of the current values of array.
	 */
	@Override
	public String toString ();

}
