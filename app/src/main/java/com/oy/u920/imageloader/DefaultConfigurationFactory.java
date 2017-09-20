package com.oy.u920.imageloader;

import com.oy.u920.deque.LIFOLinkedBlockingDeque;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for providing of default options for {@linkplain ImageLoaderConfiguration configuration}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.5.6
 */
public class DefaultConfigurationFactory {

	/** Creates default implementation of task executor */
	public static Executor createExecutor(int threadPoolSize, int threadPriority,
                                          QueueProcessingType tasksProcessingType) {
		boolean lifo = tasksProcessingType == QueueProcessingType.LIFO;
		BlockingQueue<Runnable> taskQueue =
				lifo ? new LIFOLinkedBlockingDeque<Runnable>() : new LinkedBlockingQueue<Runnable>();
		return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue,
				createThreadFactory(threadPriority, "uil-pool-"));
	}

	/** Creates default implementation of task distributor */
	public static Executor createTaskDistributor() {
		return Executors.newCachedThreadPool(createThreadFactory(Thread.NORM_PRIORITY, "uil-pool-d-"));
	}


	/**
	 * Creates default implementation of {@link MemoryCache} - {@link LruMemoryCache}<br />
	 * Default cache size = 1/8 of available app memory.
	 */
	public static IImageCache createMemoryCache(int memoryCacheSize) {
		if (memoryCacheSize == 0) {
			memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 8);
		}
		return new LruImageCache(memoryCacheSize);
	}



	/** Creates default implementation of {@link BitmapDisplayer} - {@link SimpleBitmapDisplayer} */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}

	/** Creates default implementation of {@linkplain ThreadFactory thread factory} for task executor */
	private static ThreadFactory createThreadFactory(int threadPriority, String threadNamePrefix) {
		return new DefaultThreadFactory(threadPriority, threadNamePrefix);
	}

	/**
	 * 
	 */
	private static class DefaultThreadFactory implements ThreadFactory {

		private static final AtomicInteger POOLNUMBER = new AtomicInteger(1);

		private final ThreadGroup mGroup;
		private final AtomicInteger mThreadNumber = new AtomicInteger(1);
		private final String mNamePrefix;
		private final int mThreadPriority;

		DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
			this.mThreadPriority = threadPriority;
			mGroup = Thread.currentThread().getThreadGroup();
			mNamePrefix = threadNamePrefix + POOLNUMBER.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(mGroup, r, mNamePrefix + mThreadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			t.setPriority(mThreadPriority);
			return t;
		}
	}
}
