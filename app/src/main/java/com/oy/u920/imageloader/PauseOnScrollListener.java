package com.oy.u920.imageloader;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Listener-helper for {@linkplain AbsListView list views} ({@link ListView}, {@link GridView}) which can
 * {@linkplain IconLoader#pause() pause ImageLoader's tasks} while list view is scrolling (touch scrolling and/or
 * fling). It prevents redundant loadings.<br />
 * Set it to your list view's {@link AbsListView#setOnScrollListener(OnScrollListener) setOnScrollListener(...)}.<br />
 * This listener can wrap your custom {@linkplain OnScrollListener listener}.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.7.0
 */
public class PauseOnScrollListener implements OnScrollListener {

	private IconLoader mImageLoader;

	private final boolean mPauseOnScroll;
	private final boolean mPauseOnFling;
	private final OnScrollListener mExternalListener;

	/**
	 * Constructor
	 *
	 * @param imageLoader   {@linkplain IconLoader} instance for controlling
	 * @param pauseOnScroll Whether {@linkplain IconLoader#pause() pause ImageLoader} during touch scrolling
	 * @param pauseOnFling  Whether {@linkplain IconLoader#pause() pause ImageLoader} during fling
	 */
	public PauseOnScrollListener(IconLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
		this(imageLoader, pauseOnScroll, pauseOnFling, null);
	}

	/**
	 * Constructor
	 *
	 * @param imageLoader    {@linkplain IconLoader} instance for controlling
	 * @param pauseOnScroll  Whether {@linkplain IconLoader#pause() pause ImageLoader} during touch scrolling
	 * @param pauseOnFling   Whether {@linkplain IconLoader#pause() pause ImageLoader} during fling
	 * @param customListener Your custom {@link OnScrollListener} for {@linkplain AbsListView list view} which also
	 *                       will be get scroll events
	 */
	public PauseOnScrollListener(IconLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling,
			OnScrollListener customListener) {
		this.mImageLoader = imageLoader;
		this.mPauseOnScroll = pauseOnScroll;
		this.mPauseOnFling = pauseOnFling;
		mExternalListener = customListener;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				mImageLoader.resume();
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				if (mPauseOnScroll) {
					mImageLoader.pause();
				}
				break;
			case OnScrollListener.SCROLL_STATE_FLING:
				if (mPauseOnFling) {
					mImageLoader.pause();
				}
				break;
		}
		if (mExternalListener != null) {
			mExternalListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mExternalListener != null) {
			mExternalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}
}
