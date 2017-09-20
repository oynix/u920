package com.oy.u920.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.oy.u920.imageloader.imageaware.ImageViewAware;
import com.oy.u920.imageloader.shape.RoundImageFactory;

/**
 * 本地图片的图片加载器 <br>
 * 加载时候若显示为色块，则是由于还未onLayout获取不到ImageView的尺寸，ImageViewAware已经封装解决了这部分的问题。
 * 只要给ImageView设置maxHeight和maxWidth，那么在获取不到宽高时会将最大值作为默认值
 *
 * @author chenbenbin
 */
public class ImageLoader {
    /**
     * 不显示默认图片
     */
    public static final int NONE_DEFAULT_IMAGE = -10000;
    private Context mContext;
    private volatile static ImageLoader sInstance;

    private ImageLoaderEngine mEngine;
    private IImageCache mCache;
    private BitmapDisplayer mDisplayer;
    private Handler mHandler;

    /**
     * Returns singleton class instance
     */
    public static ImageLoader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader(context);
                }
            }
        }
        return sInstance;
    }

    protected ImageLoader(Context context) {
        mEngine = new ImageLoaderEngine();
        mCache = DefaultConfigurationFactory.createMemoryCache((int) (Runtime
                .getRuntime().maxMemory() / 5));
        mDisplayer = DefaultConfigurationFactory.createBitmapDisplayer();
        mHandler = new Handler(Looper.getMainLooper());
        mContext = context.getApplicationContext();
    }

    /**
     * 显示本地图片
     *
     * @param uri       图片路径
     * @param imageView 视图
     */
    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, NONE_DEFAULT_IMAGE, 1);
    }

    /**
     * 显示本地图片
     *
     * @param uri        图片路径
     * @param imageView  视图
     * @param drawableId 默认图片的资源引用ID,不显示则传入{@link #NONE_DEFAULT_IMAGE}
     */
    public void displayImage(String uri, ImageView imageView, int drawableId) {
        displayImage(uri, imageView, drawableId, 1);
    }

    /**
     * 显示本地图片
     *
     * @param uri         图片路径
     * @param imageView   视图
     * @param drawableId  默认图片的资源引用ID,不显示则传入{@link #NONE_DEFAULT_IMAGE}
     * @param scaleFactor 缩放因子,根据传入的值加载为原本的1/scaleFactor
     */
    public void displayImage(String uri, ImageView imageView, int drawableId,
                             int scaleFactor) {
        displayImage(uri, uri, new ImageViewAware(imageView), drawableId,
                scaleFactor);
    }

    /**
     * 显示本地图片
     *
     * @param uri            图片路径
     * @param cacheKey       缓存Key
     * @param imageViewAware 视图包装器
     * @param drawableId     默认图片的资源引用ID
     * @param scaleFactor    缩放因子,根据传入的值加载为原本的1/scaleFactor
     */
    public void displayImage(String uri, String cacheKey,
                             ImageViewAware imageViewAware, int drawableId, int scaleFactor) {
        PictureLoadTask.Builder builder = new PictureLoadTask.Builder(uri,
                imageViewAware);
        builder.setScaleFactor(scaleFactor).setContext(mContext)
                .setCacheKey(cacheKey).setEngine(mEngine).setCache(mCache)
                .setBitmapDisplayer(mDisplayer)
                .setReentrantLock(mEngine.getLockForUri(uri))
                .setHandler(mHandler);
        displayImage(uri, cacheKey, imageViewAware, drawableId, scaleFactor,
                new PictureLoadTask(builder));
    }

    /**
     * 显示本地图片
     *
     * @param bean 显示对象
     */
    public void displayImage(ImageLoaderBean bean) {
        AbstractImageLoadTask task;
        // 判断图片加载方案
        switch (bean.getImageType()) {
            case ImageLoaderBean.IMAGE_TYPE_VIDEO:
                task = createVideoTask(bean);
                break;
            case ImageLoaderBean.IMAGE_TYPE_MUSIC:
                task = createMusicTask(bean);
                break;
            default:
                task = createPicTask(bean);
                break;
        }
        // 判断图形类型
        switch (bean.getShapeType()) {
            case ImageLoaderBean.SHAPE_TYPE_ROUND:
                task.setShapeFactory(new RoundImageFactory());
                break;
            default:
                break;
        }
        displayImage(bean.getUri(), bean.getCacheKey(),
                bean.getImageViewAware(), bean.getDrawableId(),
                bean.getScaleFactor(), task);
    }

    private VideoLoadTask createVideoTask(ImageLoaderBean bean) {
        VideoLoadTask.Builder builder = new VideoLoadTask.Builder(
                bean.getUri(), bean.getImageViewAware());
        builder.setScaleFactor(bean.getScaleFactor());
        wrapCommonTask(bean, builder);
        return new VideoLoadTask(builder);
    }

    private MusicLoadTask createMusicTask(ImageLoaderBean bean) {
        MusicLoadTask.Builder builder = new MusicLoadTask.Builder(
                bean.getUri(), bean.getImageViewAware());
        builder.setScaleFactor(bean.getScaleFactor());
        wrapCommonTask(bean, builder);
        return new MusicLoadTask(builder);
    }

    private PictureLoadTask createPicTask(ImageLoaderBean bean) {
        PictureLoadTask.Builder builder = new PictureLoadTask.Builder(
                bean.getUri(), bean.getImageViewAware());
        builder.setScaleFactor(bean.getScaleFactor());
        wrapCommonTask(bean, builder);
        return new PictureLoadTask(builder);
    }

    private void wrapCommonTask(ImageLoaderBean bean,
                                AbstractImageLoadTask.Builder builder) {
        builder.setContext(mContext).setCacheKey(bean.getCacheKey())
                .setEngine(mEngine).setCache(mCache)
                .setBitmapDisplayer(mDisplayer)
                .setReentrantLock(mEngine.getLockForUri(bean.getUri()))
                .setHandler(mHandler);
    }

    private void displayImage(String uri, String cacheKey,
                              ImageViewAware imageViewAware, int drawableId, int scaleFactor,
                              AbstractImageLoadTask task) {
        mEngine.prepareDisplayTaskFor(imageViewAware, cacheKey);
        Bitmap bitmap = mCache.get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            mDisplayer.display(bitmap, imageViewAware);
        } else {
            Bitmap bm = null;
            if (drawableId != NONE_DEFAULT_IMAGE) {
                try {
                    bm = BitmapFactory.decodeResource(mContext.getResources(),
                            drawableId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            imageViewAware.setImageBitmap(bm);
            mEngine.submit(task);
        }
    }

    /**
     * Pause ImageLoader. All new "load&display" tasks won't be executed until
     * ImageLoader is {@link #resume() resumed}. <br />
     * Already running tasks are not paused.
     */
    public void pause() {
        mEngine.pause();
    }

    /**
     * Resumes waiting "load&display" tasks
     */
    public void resume() {
        mEngine.resume();
    }

    /**
     * Cancels all running and scheduled display image tasks.<br />
     * <b>NOTE:</b> This method doesn't shutdown
     * custom task executors} if you set them.<br />
     * ImageLoader still can be used after calling this method.
     */
    public void stop() {
        mEngine.stop();
    }

    /**
     * 取消View对象显示图片的任务(异步加载任然继续，但不显示)
     */
    public void cancelShowImage(View view) {
        mEngine.cancelDisplayTaskFor(view);
    }

    public static void clear() {
        if (sInstance != null) {
            sInstance.mCache.clear();
            sInstance.mEngine.stop();
            sInstance.mContext = null;
        }
        sInstance = null;
    }

    /**
     * 图片加载器的显示构造对象
     *
     * @author chenbenbin
     */
    public static class ImageLoaderBean {
        private String mUri;
        private String mCacheKey;
        private ImageViewAware mImageViewAware;
        private int mDrawableId;
        private int mScaleFactor = 1;
        private int mImageType = IMAGE_TYPE_PICTURE;
        private int mShapeType = SHAPE_TYPE_NONE;
        // 图片类型
        public static final int IMAGE_TYPE_PICTURE = 0;
        public static final int IMAGE_TYPE_VIDEO = 1;
        public static final int IMAGE_TYPE_MUSIC = 2;
        // 形状类型
        public static final int SHAPE_TYPE_NONE = 0;
        public static final int SHAPE_TYPE_ROUND = 1;

        public ImageLoaderBean(String uri, ImageView iv) {
            mUri = uri;
            mCacheKey = uri;
            mImageViewAware = new ImageViewAware(iv);
        }

        public ImageLoaderBean(String uri, ImageViewAware iv) {
            mUri = uri;
            mCacheKey = uri;
            mImageViewAware = iv;
        }

        public String getUri() {
            return mUri;
        }

        public String getCacheKey() {
            return mCacheKey;
        }

        public void setCacheKey(String cacheKey) {
            mCacheKey = cacheKey;
        }

        public ImageViewAware getImageViewAware() {
            return mImageViewAware;
        }

        public int getDrawableId() {
            return mDrawableId;
        }

        public void setDrawableId(int drawableId) {
            mDrawableId = drawableId;
        }

        public int getScaleFactor() {
            return mScaleFactor;
        }

        public void setScaleFactor(int scaleFactor) {
            mScaleFactor = scaleFactor;
        }

        public int getImageType() {
            return mImageType;
        }

        public void setImageType(int loadType) {
            mImageType = loadType;
        }

        public int getShapeType() {
            return mShapeType;
        }

        public void setShapeType(int shapeType) {
            mShapeType = shapeType;
        }

        @Override
        public String toString() {
            return "ImageLoaderBean [mUri=" + mUri + ", mCacheKey=" + mCacheKey
                    + ", mImageViewAware=" + mImageViewAware + ", mDrawableId="
                    + mDrawableId + ", mScaleFactor=" + mScaleFactor
                    + ", mImageType=" + mImageType + ", mShapeType="
                    + mShapeType + "]";
        }
    }

}