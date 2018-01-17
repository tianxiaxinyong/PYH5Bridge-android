package com.pycredit.h5sdk.capture;

import android.content.Context;

/**
 * 拍照
 *
 * @author huangx
 * @date 2017/11/28
 */

public interface Capture {

    /**
     * 开始拍照
     *
     * @param context
     * @param captureConfig   拍照配置
     * @param savePath        照片保存路径
     * @param captureCallback 拍照回调
     */
    void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback captureCallback);
}
