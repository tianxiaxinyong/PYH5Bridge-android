package com.pycredit.h5sdk.capture;

import android.content.Context;

import com.pycredit.h5sdk.ui.CameraActivity;

/**
 * 自定义相机
 *
 * @author huangx
 * @date 2017/11/29
 */

public class CustomCaptureImpl implements Capture {
    /**
     * 开始拍照
     *
     * @param context
     * @param captureConfig   拍照配置
     * @param savePath        照片保存路径
     * @param captureCallback 拍照回调
     */
    @Override
    public void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback captureCallback) {
        CameraActivity.startCapture(context, captureConfig, savePath, captureCallback);
    }
}
