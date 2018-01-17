package com.pycredit.h5sdk.capture;

import android.content.Context;

import com.pycredit.h5sdk.ui.CaptureActivity;

/**
 * 系统相机
 *
 * @author huangx
 * @date 2017/11/28
 */

public class SystemCaptureImpl implements Capture {

    @Override
    public void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback captureCallback) {
        CaptureActivity.startCapture(context, savePath, captureCallback);
    }
}
