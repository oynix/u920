package com.oy.u920.imageloader;

import android.graphics.Bitmap;

import com.oy.u920.imageloader.imageaware.ImageAware;

/**
 * 加载图片任务：本地图片加载
 * 
 * @author chenbenbin
 */
public class PictureLoadTask extends AbstractImageLoadTask {
	/**
	 * 缩放比例因子,根据传入的值加载为原本的1/scaleFactor
	 */
	protected int mScaleFactor = 1;

	PictureLoadTask(Builder builder) {
		super(builder);
		mScaleFactor = builder.mScaleFactor;
	}

	@Override
	protected Bitmap tryLoadBitmap() throws TaskCancelledException {
		// 降低清晰度为原本的四分之一，提高内存缓存的图片数量
		int width = mImageAware.getWidth() / mScaleFactor;
		int height = mImageAware.getHeight() / mScaleFactor;
		return ImageDecodeUtil.decodeSampledBitmapFromResource(mUri, width,
				height);
	}

	/**
	 * 照片 - 加载图片任务 - 构造器
	 * @author chenbenbin
	 * 
	 */
	public static class Builder extends AbstractImageLoadTask.Builder {
		private int mScaleFactor = 1;

		public Builder(String uri, ImageAware aware) {
			super(uri, aware);
		}

		/**
		 * 设置缩放比例因子
		 * @param scaleFactor 根据传入的值加载为原本的1/scaleFactor
		 */
		public Builder setScaleFactor(int scaleFactor) {
			mScaleFactor = scaleFactor;
			return this;
		}
	}

}
