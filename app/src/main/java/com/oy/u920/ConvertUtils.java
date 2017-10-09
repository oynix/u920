package com.oy.u920;

import android.content.res.Resources;

/**
 * Author   : xiaoyu
 * Date     : 2017/10/9 15:45
 * Describe :
 */

public final class ConvertUtils {

    private static final String TAG = ConvertUtils.class.getSimpleName();

    private ConvertUtils() {
        throw new IllegalStateException("don't instantiate me.");
    }

    /**
     * dip/dp 向 px转换
     *
     * @param dipValue dip或/dp的值
     * @return 对应的px的值
     */
    public static int dip2px(float dipValue) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (density * dipValue + 0.5f);
    }

}
