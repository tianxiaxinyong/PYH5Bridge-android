package com.pycredit.h5sdk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author huangx
 * @date 2018/3/20
 */

public class DeviceUtils {
    /**
     * 获取缓存目录，先看SD应用对应缓存目录，没有的话就用应用内部缓存目录
     *
     * @param context
     * @return
     */
    public static File getCacheDir(Context context) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                return externalCacheDir;
            }
        }
        return context.getCacheDir();
    }
}
