package com.oy.u920.imageloader;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.oy.u920.imageloader.imageaware.ImageAware;

/**
 * 视频文件预览图加载任务
 *
 * @author chenbenbin
 */
public class VideoLoadTask extends AbstractImageLoadTask {
    /**
     * 缩放比例因子,根据传入的值加载为原本的1/scaleFactor
     */
    protected int mScaleFactor = 1;

    VideoLoadTask(Builder builder) {
        super(builder);
        mScaleFactor = builder.mScaleFactor;
    }

    @Override
    protected Bitmap tryLoadBitmap() throws TaskCancelledException {
        return getVideoThumbnail();
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public Bitmap getVideoThumbnail() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = ThumbnailUtils.createVideoThumbnail(mUri, MediaStore.Images.Thumbnails.MINI_KIND);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 视频 - 加载图片任务 - 构造器
     *
     * @author chenbenbin
     */
    public static class Builder extends AbstractImageLoadTask.Builder {
        private int mScaleFactor = 1;

        public Builder(String uri, ImageAware aware) {
            super(uri, aware);
        }

        /**
         * 设置缩放比例因子
         *
         * @param scaleFactor 根据传入的值加载为原本的1/scaleFactor
         */
        public Builder setScaleFactor(int scaleFactor) {
            mScaleFactor = scaleFactor;
            return this;
        }
    }

}
