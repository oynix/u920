package com.oy.u920.imageloader;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.text.TextUtils;

import com.oy.u920.imageloader.imageaware.ImageAware;

/**
 * 音乐文件专辑图加载任务
 * @author chenbenbin
 * 
 */
public class MusicLoadTask extends AbstractImageLoadTask {
	/**
	 * 缩放比例因子,根据传入的值加载为原本的1/scaleFactor
	 */
	protected int mScaleFactor = 1;

	MusicLoadTask(Builder builder) {
		super(builder);
		mScaleFactor = builder.mScaleFactor;
	}

	@Override
	protected Bitmap tryLoadBitmap() throws TaskCancelledException {
		// 降低清晰度为原本的N分之一，提高内存缓存的图片数量
		int width = mImageAware.getWidth() / mScaleFactor;
		int height = mImageAware.getHeight() / mScaleFactor;
		return getImage(width, height);
	}

	private Bitmap getImage(int reqWidth, int reqHeight) {
		Cursor currentCursor = getCursor(mUri);
		if (currentCursor == null) {
			return null;
		}
		int albumId = 0;
		try {
			albumId = currentCursor.getInt(currentCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			currentCursor.close();
		}
		if (albumId == 0) {
			return null;
		}
		String albumArt = getAlbumArt(albumId);
		if (TextUtils.isEmpty(albumArt)) {
			return null;
		} else {
			return ImageDecodeUtil.decodeSampledBitmapFromResource(albumArt,
					reqWidth, reqHeight);
		}
	}

	private Cursor getCursor(String filePath) {
		String path;
		Cursor cursor = mContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				path = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				if (path.equals(filePath)) {
					break;
				}
			} while (cursor.moveToNext());
		}
		return cursor;
	}

	private String getAlbumArt(int albumId) {
		Cursor cur = mContext.getContentResolver().query(
				Uri.parse(Albums.EXTERNAL_CONTENT_URI + "/"
						+ Integer.toString(albumId)),
				new String[] { "album_art" }, null, null, null);
		String album_art = null;
		if (cur != null) {
			if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
				cur.moveToNext();
				album_art = cur.getString(0);
			}
			cur.close();
		}
		return album_art;
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
