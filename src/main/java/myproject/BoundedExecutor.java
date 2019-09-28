package myproject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Utility class to use executor service with boundaries to avoid overflow of
 * messages for executor. Usually value of bound should be double the number if
 * threads in the executor service
 *
 */
public class BoundedExecutor {
	private final ExecutorService exec;
	private final Semaphore semaphore;
	private final int bound;
	public static final int DEFAULT_BOUND_MULTIPLIER = 2;

	public BoundedExecutor(ExecutorService exec, int bound) {
		this.exec = exec;
		semaphore = new Semaphore(bound);
		this.bound = bound;
	}

	public void submitTask(final Runnable command) throws InterruptedException {
		semaphore.acquire();
		try {
			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						command.run();
					} finally {
						semaphore.release();
					}
				}
			});
		} catch (final RejectedExecutionException e) {
			semaphore.release();
		}
	}

	/**
	 * Check if all the semaphores are released and executor is ready to be
	 * terminated
	 *
	 * @return
	 */
	public boolean isReadyToTerminate() {
		return semaphore.availablePermits() == bound;
	}

	public void shutdown() {
		exec.shutdown();
	}
}
