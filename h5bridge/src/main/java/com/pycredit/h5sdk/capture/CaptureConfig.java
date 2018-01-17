package com.pycredit.h5sdk.capture;

import java.io.Serializable;

/**
 * 拍照配置
 *
 * @author huangx
 * @date 2017/11/28
 */

public class CaptureConfig implements Serializable {
    /**
     * 手持身份证
     */
    public static final int IMAGE_TYPE_IN_HAND = 0;
    /**
     * 人像面
     */
    public static final int IMAGE_TYPE_FRONT = 1;
    /**
     * 国徽面
     */
    public static final int IMAGE_TYPE_BACK = 2;

    /**
     * 是否默认前置摄像头
     */
    public boolean defaultFrontCamera;
    /**
     * 是否横屏拍照
     */
    public boolean landscape;
    /**
     * 照片类型
     */
    public int imageType;

    public CaptureConfig(boolean defaultFrontCamera, boolean landscape, int imageType) {
        this.defaultFrontCamera = defaultFrontCamera;
        this.landscape = landscape;
        this.imageType = imageType;
    }
}
