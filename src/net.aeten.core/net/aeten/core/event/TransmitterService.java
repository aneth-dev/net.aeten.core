package net.aeten.core.event;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.aeten.core.Identifiable;

public interface TransmitterService<Event, Data extends EventData <?, Event>> extends
		Transmitter <Data>,
		HandlerRegister <Event, Data>,
		Identifiable {
	/**
	 * Initiates an orderly shutdown in which previously submitted
	 * signals are transmitted, but no new signals will be accepted.
	 * Invocation has no additional effect if already shut down.
	 *
	 * @throws SecurityException if a security manager exists and
	 *         shutting down this ExecutorService may manipulate
	 *         threads that the caller is not permitted to modify
	 *         because it does not hold {@link
	 *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
	 *         or the security manager's <tt>checkAccess</tt> method
	 *         denies access.
	 */
	void shutdown ();

	/**
	 * Attempts to stop all actively executing tasks, halts the
	 * processing of waiting tasks, and returns a list of the tasks that were
	 * awaiting execution.
	 *
	 * <p>There are no guarantees beyond best-effort attempts to stop
	 * processing actively executing tasks.  For example, typical
	 * implementations will cancel via {@link Thread#interrupt}, so any
	 * task that fails to respond to interrupts may never terminate.
	 *
	 * @return list of tasks that never commenced execution
	 * @throws SecurityException if a security manager exists and
	 *         shutting down this ExecutorService may manipulate
	 *         threads that the caller is not permitted to modify
	 *         because it does not hold {@link
	 *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
	 *         or the security manager's <tt>checkAccess</tt> method
	 *         denies access.
	 */
	List <Data> shutdownNow ();

	/**
	 * Returns <tt>true</tt> if this executor has been shut down.
	 *
	 * @return <tt>true</tt> if this executor has been shut down
	 */
	boolean isShutdown ();

	/**
	 * Returns <tt>true</tt> if all tasks have completed following shut down.
	 * Note that <tt>isTerminated</tt> is never <tt>true</tt> unless
	 * either <tt>shutdown</tt> or <tt>shutdownNow</tt> was called first.
	 *
	 * @return <tt>true</tt> if all tasks have completed following shut down
	 */
	boolean isTerminated ();

	/**
	 * Blocks until all tasks have completed execution after a shutdown
	 * request, or the timeout occurs, or the current thread is
	 * interrupted, whichever happens first.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * @return <tt>true</tt> if this executor terminated and
	 *         <tt>false</tt> if the timeout elapsed before termination
	 * @throws InterruptedException if interrupted while waiting
	 */
	boolean awaitTermination (	long timeout,
										TimeUnit unit) throws InterruptedException;

	public void start ();

}
