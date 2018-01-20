package com.pycredit.h5sdk.impl;

import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.pycredit.h5sdk.js.BaseBridge;
import com.pycredit.h5sdk.js.JsCallAppRunner;


/**
 * 天下信用通用JS调用APP 注入对像
 * Created by huangx on 2017/2/14.
 */

public class PYCreditJsBridge<T extends PYCreditJsCallAppProcess> extends BaseBridge<T> {

    protected PYCreditJsParser parser = new PYCreditJsParser();

    public PYCreditJsBridge(WebView webView, @NonNull T jsCallAppProcess) {
        super(webView, jsCallAppProcess);
    }

    /**
     * 拍照
     *
     * @param params
     */
    @JavascriptInterface
    public void cameraGetImage(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.cameraGetImage(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 原图预览
     *
     * @param params
     */
    @JavascriptInterface
    public void previewImage(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.previewImage(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 图片上传
     *
     * @param params
     */
    @JavascriptInterface
    public void uploadImage(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.uploadImage(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 拉起 App
     *
     * @param params
     */
    @JavascriptInterface
    public void openPayApp(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.openPayApp(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 获取广告图片地址
     *
     * @param params
     */
    @JavascriptInterface
    public void getAdBannerURL(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.getAdBannerURL(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 广告图片点击事件
     *
     * @param params
     */
    @JavascriptInterface
    public void clickAd(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.adClick(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 获取SDK版本信息
     *
     * @param params
     */
    @JavascriptInterface
    public void getAppInfo(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.getAppInfo(parser.decode(params), parser, callback);
                }
            }
        });
    }

    /**
     * 申请权根（拍照、拍视频）
     *
     * @param params
     */
    @JavascriptInterface
    public void authorization(String params) {
        runOnUiThread(new JsCallAppRunner<PYCreditJsCallAppProcess>(getJsCallAppProcess(), params, this) {
            @Override
            public void run() {
                if (process != null) {
                    process.authorization(parser.decode(params), parser, callback);
                }
            }
        });
    }
}
