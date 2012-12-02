package net.aeten.core.util;

import net.aeten.core.Factory;
import net.aeten.core.Getter;
import net.aeten.core.Setter;

/**
 * An object reference that may be updated atomically.
 * 
 * @author Thomas Pérennou
 * @param <V>
 *            The type of object referred to by this reference
 */
public interface AtomicValue<V> extends
		Getter <V>,
		Setter <V> {

	/**
	 * Eventually sets to the given value.
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void lazySet (V newValue);

	/**
	 * Atomically sets the value to the given updated value if the current value
	 * equals the expected value. "Equals" means depends of implementation (like
	 * {@code ==} or {@code equals(Object)}).
	 * 
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful. False return indicates that the actual value
	 *         was not equal to the expected value.
	 */
	public boolean compareAndSet (V expect,
											V update);

	/**
	 * Atomically sets the value to the given updated value if the current value
	 * equals the expected value. "Equals" means depends of implementation (like
	 * {@code ==} or {@code equals(Object)}).
	 * 
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the factory of the new value
	 * @return true if successful. False return indicates that the actual value
	 *         was not equal to the expected value.
	 */
	public boolean compareAndSet (V expect,
											Factory <V, Void> update);

	/**
	 * Atomically sets the value to the given updated value if the current value
	 * equals the expected value. "Equals" means depends of implementation (like
	 * {@code ==} or {@code equals(Object)}).
	 * 
	 * <p>
	 * May fail spuriously and does not provide ordering guarantees, so is only
	 * rarely an appropriate alternative to {@code compareAndSet}.
	 * 
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful.
	 */
	public boolean weakCompareAndSet (	V expect,
													V update);

	/**
	 * Atomically sets the value to the given updated value if the current value
	 * equals the expected value. "Equals" means depends of implementation (like
	 * {@code ==} or {@code equals(Object)}).
	 * 
	 * <p>
	 * May fail spuriously and does not provide ordering guarantees, so is only
	 * rarely an appropriate alternative to {@code compareAndSet}.
	 * 
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the factory of the new value
	 * @return true if successful.
	 */
	public boolean weakCompareAndSet (	V expect,
													Factory <V, Void> update);

	/**
	 * Atomically sets to the given value and returns the old value.
	 * 
	 * @param newValue
	 *            the new value
	 * @return the previous value
	 */
	public V getAndSet (V newValue);

}