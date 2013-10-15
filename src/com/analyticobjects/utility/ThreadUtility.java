package com.analyticobjects.utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class for threads.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ThreadUtility {
	
	/**
	 * Create new executor with a thread for each processor core.
	 *
	 * @return A new executor with a thread for each processor core.
	 */
	public static ExecutorService allAvailableProcessors() {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
}
