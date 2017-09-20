package com.oy.u920.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.oy.u920.imageloader.imageaware.ImageAware;
import com.oy.u920.imageloader.shape.IImageShapeFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 加载图片任务
 * 
 * @author chendongcheng
 * 
 */
public abstract class AbstractImageLoadTask implements Runnable {
	protected Context mContext;
	protected String mUri;
	protected String mCacheKey;
	protected ReentrantLock mReentrantLock;
	protected Handler mHandler;
	protected ImageAware mImageAware;
	protected ImageLoaderEngine mEngine;
	protected IImageCache mCache;
	protected BitmapDisplayer mBitmapDisplayer;
	protected IImageShapeFactory mShapeFactory;

	AbstractImageLoadTask(Builder builder) {
		mUri = builder.mUri;
		mCacheKey = builder.mCacheKey;
		if (mCacheKey == null) {
			// 不应该出现为空的情况， 但是太多地方可能传入错误数据，暂时加保护，以防NullPointerException
			mCacheKey = "";
		}
		mImageAware = builder.mImageAware;
		mReentrantLock = builder.mReentrantLock;
		mHandler = builder.mHandler;
		mEngine = builder.mEngine;
		mContext = builder.mContext;
		mCache = builder.mCache;
		mBitmapDisplayer = builder.mBitmapDisplayer;
	}

	public void setShapeFactory(IImageShapeFactory factory) {
		mShapeFactory = factory;
	}

	@Override
	public void run() {
		if (waitIfPaused()) {
			return;
		}
		if (delayIfNeed()) {
			return;
		}
		mReentrantLock.lock();
		Bitmap bitmap = null;
		try {
			checkTaskNotActual();
			bitmap = mCache.get(mCacheKey);
			if (bitmap == null || bitmap.isRecycled()) {
				Bitmap temp = tryLoadBitmap();
				if (temp == null) {
					return;
				}
				if (mShapeFactory != null) {
					bitmap = mShapeFactory.ps(temp);
				} else {
					bitmap = temp;
				}

				checkTaskNotActual();
				checkTaskInterrupted();

				if (bitmap != null) {
					mCache.set(mCacheKey, bitmap);
				}
			}

			checkTaskNotActual();
			checkTaskInterrupted();

		} catch (Exception e) {

		} catch (OutOfMemoryError e) {

		} finally {
			mReentrantLock.unlock();
		}

		if (bitmap != null) {
			DisplayImageTask task = new DisplayImageTask(bitmap,
					mBitmapDisplayer, mCacheKey, mImageAware, mEngine);
			mHandler.post(task);
		}
	}

	protected abstract Bitmap tryLoadBitmap() throws TaskCancelledException;

	/**
	 * @throws TaskCancelledException if task is not actual (target ImageAware
	 *             is collected by GC or the image URI of this task doesn't
	 *             match to image URI which is actual for current ImageAware at
	 *             this moment)
	 */
	protected void checkTaskNotActual() throws TaskCancelledException {
		checkViewCollected();
		checkViewReused();
	}

	/**
	 * @throws TaskCancelledException if target ImageAware is collected
	 */
	private void checkViewCollected() throws TaskCancelledException {
		if (isViewCollected()) {
			throw new TaskCancelledException();
		}
	}

	/**
	 * @throws TaskCancelledException if target ImageAware is collected by GC
	 */
	private void checkViewReused() throws TaskCancelledException {
		if (isViewReused()) {
			throw new TaskCancelledException();
		}
	}

	/**
	 * @throws TaskCancelledException if current task was interrupted
	 */
	private void checkTaskInterrupted() throws TaskCancelledException {
		if (isTaskInterrupted()) {
			throw new TaskCancelledException();
		}
	}

	/**
	 * @return <b>true</b> - if task should be interrupted; <b>false</b> -
	 *         otherwise
	 */
	private boolean waitIfPaused() {
		AtomicBoolean pause = mEngine.getPause();
		if (pause.get()) {
			synchronized (mEngine.getPauseLock()) {
				if (pause.get()) {
					try {
						mEngine.getPauseLock().wait();
					} catch (InterruptedException e) {
						return true;
					}
				}
			}
		}
		return isTaskNotActual();
	}

	/**
	 * @return <b>true</b> - if task should be interrupted; <b>false</b> -
	 *         otherwise
	 */
	private boolean delayIfNeed() {
		return false;
	}

	/**
	 * @return <b>true</b> - if task is not actual (target ImageAware is
	 *         collected by GC or the image URI of this task doesn't match to
	 *         image URI which is actual for current ImageAware at this
	 *         moment)); <b>false</b> - otherwise
	 */
	private boolean isTaskNotActual() {
		return isViewCollected() || isViewReused();
	}

	/**
	 * @return <b>true</b> - if current task was interrupted; <b>false</b> -
	 *         otherwise
	 */
	private boolean isTaskInterrupted() {
		if (Thread.interrupted()) {
			return true;
		}
		return false;
	}

	/**
	 * @return <b>true</b> - if target ImageAware is collected by GC;
	 *         <b>false</b> - otherwise
	 */
	private boolean isViewCollected() {
		if (mImageAware.isCollected()) {
			return true;
		}
		return false;
	}

	/**
	 * @return <b>true</b> - if current ImageAware is reused for displaying
	 *         another image; <b>false</b> - otherwise
	 */
	private boolean isViewReused() {
		String currentCacheKey = mEngine.getLoadingUriForView(mImageAware);
		// Check whether memory cache key (image URI) for current ImageAware is
		// actual.
		// If ImageAware is reused for another task then current task should be
		// cancelled.
		boolean imageAwareWasReused = !mCacheKey.equals(currentCacheKey);
		if (imageAwareWasReused) {
			return true;
		}
		return false;
	}

	/**
	 * Exceptions for case when task is cancelled (thread is interrupted, image
	 * view is reused for another task, view is collected by GC).
	 * 
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 * @since 1.9.1
	 */
	class TaskCancelledException extends Exception {

	}

	/**
	 * 加载图片任务 - 构造器
	 * @author chenbenbin
	 * 
	 */
	static class Builder {
		protected Context mContext;
		protected String mUri;
		protected String mCacheKey;
		protected ReentrantLock mReentrantLock;
		protected Handler mHandler;
		protected ImageAware mImageAware;
		protected ImageLoaderEngine mEngine;
		protected IImageCache mCache;
		protected BitmapDisplayer mBitmapDisplayer;

		public Builder setContext(Context context) {
			mContext = context;
			return this;
		}

		public Builder(String uri, ImageAware aware) {
			mUri = uri;
			mImageAware = aware;
		}

		public Builder setCacheKey(String cacheKey) {
			mCacheKey = cacheKey;
			return this;
		}

		public Builder setReentrantLock(ReentrantLock reentrantLock) {
			mReentrantLock = reentrantLock;
			return this;
		}

		public Builder setHandler(Handler handler) {
			mHandler = handler;
			return this;
		}

		public Builder setEngine(ImageLoaderEngine engine) {
			mEngine = engine;
			return this;
		}

		public Builder setCache(IImageCache cache) {
			mCache = cache;
			return this;
		}

		public Builder setBitmapDisplayer(BitmapDisplayer bitmapDisplayer) {
			mBitmapDisplayer = bitmapDisplayer;
			return this;
		}

	}
}
