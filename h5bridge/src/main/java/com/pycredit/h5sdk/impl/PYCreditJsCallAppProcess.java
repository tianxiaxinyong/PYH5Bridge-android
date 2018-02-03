package com.pycredit.h5sdk.impl;


import com.pycredit.h5sdk.js.JsCallAppCallback;
import com.pycredit.h5sdk.js.JsCallAppProcess;

/**
 * Created by huangx on 2017/2/17.
 */

public interface PYCreditJsCallAppProcess extends JsCallAppProcess {
    /**
     * 拍照
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void cameraGetImage(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 原图预览
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void previewImage(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 图片上传
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void uploadImage(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 拉起 App
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void openPayApp(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 获取广告图片地址
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void getAdBannerURL(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 广告图片点击事件
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void adClick(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 获取SDK版本信息
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void getAppInfo(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 申请权根（拍照、拍视频）
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void authorization(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);

    /**
     * 检查是否支持视频录制
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void checkVideoRecording(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback);
}