package com.oy.u920;

/**
 * Author   : xiaoyu
 * Date     : 2017/10/13 15:55
 * Describe :
 */

public final class FileUtils {

    private static final String UNIX_SEPARATOR = "/";

    /**
     * 从Url获取文件名, 包含扩展名
     * @param path 路径
     * @return name
     */
    public static String getFileNameFromUrl(String path) {
        if (path == null)
            return "";
        int index = path.lastIndexOf(UNIX_SEPARATOR);
        if (index == -1)
            return path;
        return path.substring(index + 1);
    }

}
