package com.oy.u920.imageloader;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.oy.u920.R;

/**
 * 加载图片任务：应用图标&APK
 * 
 * @author chenhewen
 */
class IconLoadTask extends AbstractImageLoadTask {
	public IconLoadTask(IconLoadTask.Builder builder) {
		super(builder);
	}

	protected Bitmap tryLoadBitmap() throws TaskCancelledException {
		Bitmap bitmap = null;
		try {

			//checkTaskNotActual();
			Drawable drawable = getApplicationDrawable(mUri);
			if (drawable != null) {
				if (drawable instanceof BitmapDrawable) {
					BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
					bitmap = bitmapDrawable.getBitmap();
				} else {
					final int badgedWidth = drawable.getIntrinsicWidth();
					final int badgedHeight = drawable.getIntrinsicHeight();
					if (badgedWidth > 0 || badgedHeight > 0) {
						bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(bitmap);
						drawable.setBounds(0, 0, badgedWidth, badgedHeight);
						drawable.draw(canvas);
					}
				}
			} else if (drawable == null || bitmap == null) {
				// 兼容未安装应用
				BitmapDrawable drawable2 = (BitmapDrawable) getApplicationDrawableIfNotInstalled(mUri);
				if (drawable2 != null) {
					bitmap = drawable2.getBitmap();
				} else {
					BitmapDrawable drawable3 = (BitmapDrawable) mContext
							.getResources().getDrawable(
									R.drawable.apk);
					bitmap = drawable3.getBitmap();
				}

			}

		} catch (Exception e) {

		}
		return bitmap;
	}

	private Drawable getApplicationDrawable(String pkgName) {
		PackageManager pm = mContext.getPackageManager();
		Drawable drawable = null;
		try {
			drawable = pm.getApplicationIcon(pkgName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return drawable;
	}

	private Drawable getApplicationDrawableIfNotInstalled(String path) {
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo = pm.getPackageArchiveInfo(path,
				PackageManager.GET_ACTIVITIES);

		if (packageInfo != null) {
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			appInfo.sourceDir = path;
			appInfo.publicSourceDir = path;
			try {
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e) {
			}
		}
		return null;
	}

}