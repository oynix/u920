package com.oy.u920.imageloader;

import android.graphics.Bitmap;

import com.oy.u920.imageloader.imageaware.ImageAware;

/**
 * 显示图片
 * @author chendongcheng
 *
 */
public class DisplayImageTask implements Runnable {
	
	private Bitmap mBitmap;
	private BitmapDisplayer mBitmapDisplayer;
	private String mCacheKey;
	private ImageAware mImageAware;
	private ImageLoaderEngine mEngine;
	
	public DisplayImageTask(Bitmap bitmap, BitmapDisplayer bitmapDisplayer, String cacheKey, ImageAware imageAware,
                            ImageLoaderEngine engine) {
		mBitmap = bitmap;
		mBitmapDisplayer = bitmapDisplayer;
		mCacheKey = cacheKey;
		mImageAware = imageAware;
		mEngine = engine;
	}

	@Override
	public void run() {
		if (mImageAware.isCollected()) {

		} else if (isViewWasReused()) {
	
		} else {
			mBitmapDisplayer.display(mBitmap, mImageAware);
			mEngine.cancelDisplayTaskFor(mImageAware);
		}
	}
	
	/** Checks whether memory cache key (image URI) for current ImageAware is actual */
	private boolean isViewWasReused() {
		String currentCacheKey = mEngine.getLoadingUriForView(mImageAware);
		return !mCacheKey.equals(currentCacheKey);
	}

}
