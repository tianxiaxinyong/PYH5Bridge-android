package com.pycredit.h5sdk.utils;

import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UploadManager;

/**
 * Created by huangx on 2017/10/14.
 */

public class UploadUtils {
    private static UploadManager uploadManager;

    public static synchronized UploadManager getUploadManager() {
        if (uploadManager == null) {
            synchronized (UploadUtils.class) {
                if (uploadManager == null) {
                    Configuration configuration = new Configuration.Builder().build();
                    uploadManager = new UploadManager(configuration);
                }
            }
        }
        return uploadManager;
    }
}
