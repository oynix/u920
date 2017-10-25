package com.oy.u920;

import org.junit.Assert;
import org.junit.Test;

/**
 * Author   : xiaoyu
 * Date     : 2017/10/13 16:04
 * Describe :
 */
public class FileUtilsTest {

    @Test
    public void getFileNameFromUrl() throws Exception {
        String name = FileUtils.getFileNameFromUrl("http://gank.io/api/image/beautifulgirl.jpg");
        Assert.assertEquals("beautifulgirl.jpg", name);
    }

}