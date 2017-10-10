package com.oy.u920;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author   : xiaoyu
 * Date     : 2017/9/28 18:39
 * Describe :
 */

public final class DataHub {

    private final ConcurrentHashMap<String, Object> mHashMap = new ConcurrentHashMap<>();

    private static DataHub INSTANCE = new DataHub();

    public static void put(String key, Object value) {
        INSTANCE.mHashMap.put(key, value);
    }

    public static Object get(String key) {
        return INSTANCE.mHashMap.remove(key);
    }
}
