package com.oy.u920.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.oy.u920.imageloader.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用/APK图标的图片加载器
 * 
 * @author chendongcheng
 * 
 */
public class IconLoader {
	private ImageLoaderEngine mEngine;
	private IImageCache mCache;
	private BitmapDisplayer mDisplayer;

	private volatile static IconLoader sInstance;

	private Handler mHandler;

	private Context mContext;

	private final List<Object> mClients = new ArrayList<Object>();

	/**
	 * 确认初始化单例，若未初始化则初始化<br>
	 * 
	 * @param context
	 */
	public static void ensureInitSingleton(Context context) {
		if (!isSingletonInit()) {
			sInstance = new IconLoader(context);
		}
	}

	/**
	 * 单例是否已始化<br>
	 * 
	 * @return
	 */
	public static boolean isSingletonInit() {
		return sInstance != null;
	}

	/**
	 * Returns singleton class instance <br>
	 * can only call when {@link #isSingletonInit()} is <em>true</em>.
	 */
	public static IconLoader getInstance() {
		if (sInstance == null) {
			throw new IllegalStateException(
					"make sure has called ensureInitSingleton()");
		}
		return sInstance;
	}

	protected IconLoader(Context context) {
		mEngine = new ImageLoaderEngine();
		mCache = DefaultConfigurationFactory.createMemoryCache(0);
		mDisplayer = DefaultConfigurationFactory.createBitmapDisplayer();
		mHandler = new Handler(Looper.getMainLooper());
		mContext = context.getApplicationContext();
	}

	/**
	 * 显示应用图标或者APK图标
	 * 
	 * @param uri APK包名或者APK路径
	 * @param imageView
	 */
	public void displayImage(String uri, ImageView imageView) {
		displayImage(uri, new ImageViewAware(imageView));
	}

	/**
	 * 显示应用图标或者APK图标
	 * 
	 * @param uri APK包名或者APK路径
	 * @param imageViewAware
	 */
	public void displayImage(String uri, ImageViewAware imageViewAware) {
		mEngine.prepareDisplayTaskFor(imageViewAware, uri);
		Bitmap bitmap = mCache.get(uri);
		if (bitmap != null && !bitmap.isRecycled()) {
			mDisplayer.display(bitmap, imageViewAware);
		} else {
			imageViewAware.setImageDrawable(null);
			IconLoadTask.Builder builder = new IconLoadTask.Builder(uri,
					imageViewAware);
			builder.setContext(mContext).setCacheKey(uri).setEngine(mEngine)
					.setCache(mCache).setBitmapDisplayer(mDisplayer)
					.setReentrantLock(mEngine.getLockForUri(uri))
					.setHandler(mHandler);
			mEngine.submit(new IconLoadTask(builder));
		}
	}

	/**
	 * 取消View对象显示图片的任务(异步加载任然继续，但不显示)
	 */
	public void cancelShowImage(View view) {
		mEngine.cancelDisplayTaskFor(view);
	}

	/**
	 * Pause ImageLoader. All new "load&display" tasks won't be executed until
	 * ImageLoader is {@link #resume() resumed}. <br />
	 * Already running tasks are not paused.
	 */
	public void pause() {
		mEngine.pause();
	}

	/** Resumes waiting "load&display" tasks */
	public void resume() {
		mEngine.resume();
	}

	/**
	 * Cancels all running and scheduled display image tasks.<br />
	 * <b>NOTE:</b> This method doesn't shutdown
	 * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#taskExecutor(java.util.concurrent.Executor)
	 * custom task executors} if you set them.<br />
	 * ImageLoader still can be used after calling this method.
	 */
	public void stop() {
		mEngine.stop();
	}

	/**
	 * 当需要使用时,登记一个客户, 在客户数为零时销毁单例.<br>
	 * 
	 * @param client
	 * @see #unbindServicer(Object)
	 */
	public void bindServicer(Object client) {
		if (null == client) {
			throw new IllegalArgumentException("bad client: null");
		}
		if (mClients.contains(client)) {
			throw new IllegalStateException("client already exist");
		}
		mClients.add(client);
	}

	/**
	 * 当不再使用时,撤销一个客户, 在客户数为零时销毁单例.<br>
	 * 
	 * @param client
	 * @see #bindServicer(Object)
	 */
	public void unbindServicer(Object client) {
		if (null == client) {
			throw new IllegalArgumentException("bad client: null");
		}
		if (!mClients.contains(client)) {
			throw new IllegalStateException(
					"client is not bind before, do you give the wrong client?");
		}
		mClients.remove(client);
		if (mClients.size() == 0) {
			destroySingleton();
		}
	}

	/**
	 * 销毁单例<br>
	 * 
	 * @param context
	 */
	private static void destroySingleton() {
		if (isSingletonInit()) {
			sInstance.mCache.clear();
			sInstance.mEngine.stop();
			sInstance.mContext = null;
			sInstance = null;
		}
	}

}
