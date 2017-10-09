package com.oy.u920;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Author   : xiaoyu
 * Date     : 2017/10/9 15:49
 * Describe :
 */
public class ConvertUtilsTest {
    @Test
    public void dip2px() throws Exception {
        int px = ConvertUtils.dip2px(2);
        Assert.assertEquals(3, px);
    }

}