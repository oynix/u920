package com.oy.u920.imageloader;

import android.view.View;

import com.oy.u920.imageloader.imageaware.ImageAware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link IconLoader} engine which responsible for {@linkplain IconLoadTask display task} execution.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.7.1
 */
class ImageLoaderEngine {

	/** {@value} */
	public static final int DEFAULT_THREAD_POOL_SIZE = 3;
	/** {@value} */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
	/** {@value} */
	public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;

	private Executor mTaskExecutor;
	private Executor mTaskDistributor;

	private final Map<Integer, String> mCacheKeysForImageAwares = Collections
			.synchronizedMap(new HashMap<Integer, String>());
	private final Map<String, ReentrantLock> mUriLocks = new WeakHashMap<String, ReentrantLock>();

	private final AtomicBoolean mPaused = new AtomicBoolean(false);

	private final Object mPauseLock = new Object();

	ImageLoaderEngine() {
		mTaskDistributor = DefaultConfigurationFactory.createTaskDistributor();
		initExecutorsIfNeed();
	}

	/** Submits task to execution pool */
	void submit(final Runnable task) {
		mTaskDistributor.execute(new Runnable() {
			@Override
			public void run() {
				initExecutorsIfNeed();
				mTaskExecutor.execute(task);
			}
		});
	}


	private void initExecutorsIfNeed() {
		if (mTaskExecutor == null || ((ExecutorService) mTaskExecutor).isShutdown()) {
			mTaskExecutor = createTaskExecutor();
		}
	}

	private Executor createTaskExecutor() {
		return DefaultConfigurationFactory
				.createExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_PRIORITY,
				QueueProcessingType.LIFO);
	}

	/**
	 * Returns URI of image which is loading at this moment into passed {@link com.nostra13.universalimageloader.core.imageaware.ImageAware}
	 */
	String getLoadingUriForView(ImageAware imageAware) {
		return mCacheKeysForImageAwares.get(imageAware.getId());
	}

	/**
	 * Associates <b>memoryCacheKey</b> with <b>imageAware</b>. Then it helps to define image URI is loaded into View at
	 * exact moment.
	 */
	void prepareDisplayTaskFor(ImageAware imageAware, String memoryCacheKey) {
		mCacheKeysForImageAwares.put(imageAware.getId(), memoryCacheKey);
	}
	
	/**
	 * Cancels the task of loading and displaying image for incoming <b>imageAware</b>.
	 *
	 * @param imageAware {@link com.nostra13.universalimageloader.core.imageaware.ImageAware} for which display task
	 *                   will be cancelled
	 */
	void cancelDisplayTaskFor(ImageAware imageAware) {
		mCacheKeysForImageAwares.remove(imageAware.getId());
	}

	void cancelDisplayTaskFor(View view) {
		mCacheKeysForImageAwares.remove(view.hashCode());
	}

	/**
	 * Pauses engine. All new "load&display" tasks won't be executed until ImageLoader is {@link #resume() resumed}.<br
	 * /> Already running tasks are not paused.
	 */
	void pause() {
		mPaused.set(true);
	}

	/** Resumes engine work. Paused "load&display" tasks will continue its work. */
	void resume() {
		mPaused.set(false);
		synchronized (mPauseLock) {
			mPauseLock.notifyAll();
		}
	}

	/**
	 * Stops engine, cancels all running and scheduled display image tasks. Clears internal data.
	 * <br />
	 * <b>NOTE:</b> This method doesn't shutdown
	 * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#taskExecutor(java.util.concurrent.Executor)
	 * custom task executors} if you set them.
	 */
	void stop() {
		((ExecutorService) mTaskExecutor).shutdownNow();
		mCacheKeysForImageAwares.clear();
		mUriLocks.clear();
	}

	void fireCallback(Runnable r) {
		mTaskDistributor.execute(r);
	}

	ReentrantLock getLockForUri(String uri) {
		ReentrantLock lock = mUriLocks.get(uri);
		if (lock == null) {
			lock = new ReentrantLock();
			mUriLocks.put(uri, lock);
		}
		return lock;
	}

	AtomicBoolean getPause() {
		return mPaused;
	}

	Object getPauseLock() {
		return mPauseLock;
	}
	
}
