package com.pycredit.h5sdk.impl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.pycredit.h5sdk.H5JsHelper;
import com.pycredit.h5sdk.H5SDKHelper;
import com.pycredit.h5sdk.capture.Capture;
import com.pycredit.h5sdk.capture.CaptureCallback;
import com.pycredit.h5sdk.capture.CaptureConfig;
import com.pycredit.h5sdk.capture.CustomCaptureImpl;
import com.pycredit.h5sdk.js.JsCallAppCallback;
import com.pycredit.h5sdk.perm.PermChecker;
import com.pycredit.h5sdk.ui.PermRequestActivity;
import com.pycredit.h5sdk.ui.PhotoPreviewActivity;
import com.pycredit.h5sdk.ui.WebActivity;
import com.pycredit.h5sdk.utils.DeviceUtils;
import com.pycredit.h5sdk.utils.EncodeUtils;
import com.pycredit.h5sdk.utils.ImageUtils;
import com.pycredit.h5sdk.utils.ProgressRequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_IMAGE_HANDLE;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_NO_BANNER;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_NO_BANNER_URL;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_NO_CAMERA_PERM;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_NO_NETWORK;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_PAY_APP_NOT_INSTALL;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_REQUEST_PERM_FAIL;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_TIMEOUT;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.ERROR_VIDEO_RECORD_UN_SUPPORT;
import static com.pycredit.h5sdk.js.JsCallAppErrorCode.SUCCESS;

/**
 * Created by huangx on 2017/10/13.
 */

public class PYCreditJsCallAppProcessor implements PYCreditJsCallAppProcess {

    protected WeakReference<Context> contextRef;
    protected WeakReference<Activity> activityRef;
    protected WeakReference<Fragment> fragmentRef;

    protected String bannerUrl;
    protected BannerCallback bannerCallback;

    /**
     * 拍照参数暂存
     */
    protected PYCreditJs2AppInfo captureInfo;
    protected PYCreditJsParser captureParser;
    protected JsCallAppCallback captureCallback;
    /**
     * 支付参数暂存
     */
    protected PYCreditJs2AppInfo payInfo;
    protected PYCreditJsParser payParser;
    protected JsCallAppCallback payCallback;
    /**
     * 检查是否支持视频录制暂存
     */
    protected PYCreditJs2AppInfo checkRecordInfo;
    protected PYCreditJsParser checkRecordParser;
    protected JsCallAppCallback checkRecordCallback;

    protected String cameraFilePath;//拍照图片存放地址

    protected Capture capture;

    protected Handler mainHandler = new Handler(Looper.getMainLooper());

    public PYCreditJsCallAppProcessor(Activity activity) {
        activityRef = new WeakReference<>(activity);
        contextRef = new WeakReference<Context>(activity);
        init();
    }

    public PYCreditJsCallAppProcessor(Fragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
        contextRef = new WeakReference<>(fragment.getContext());
        init();
    }

    protected void init() {
        capture = new CustomCaptureImpl();
    }

    /**
     * 获取广告图片地址
     *
     * @param bannerImageUrl
     * @param callback
     */
    public void setBanner(String bannerImageUrl, BannerCallback callback) {
        bannerUrl = bannerImageUrl;
        this.bannerCallback = callback;
    }

    /**
     * 设置自定义拍照实现
     *
     * @param capture
     */
    public void setCapture(Capture capture) {
        this.capture = capture;
    }

    /**
     * 拍照
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void cameraGetImage(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        captureInfo = js2AppInfo;
        captureParser = parser;
        captureCallback = callback;
        String[] perms = new String[]{Manifest.permission.CAMERA};
        if (PermChecker.hasPermissions(contextRef.get(), perms)) {
            if (!startCapturePic()) {
                if (captureCallback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                    errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                    captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                }
            }
        } else {
            //直接请求
            if (contextRef != null && contextRef.get() != null) {
                PermRequestActivity.requestPermissions(contextRef.get(), perms, new PermChecker.RequestPermCallback() {
                    @Override
                    public void onRequestSuccess() {
                        if (!startCapturePic()) {
                            if (captureCallback != null) {
                                Map<String, Object> errorData = new HashMap<>();
                                errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                                errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                                captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                            }
                        }
                    }

                    @Override
                    public void onRequestFail() {
                        if (captureCallback != null) {
                            Map<String, Object> errorData = new HashMap<>();
                            errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                            errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                            captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                        }
                    }
                });
            }
        }
    }

    /**
     * 原图预览
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void previewImage(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            String path = dataObj.optString("localId");
            if (!TextUtils.isEmpty(path)) {
                if (contextRef != null && contextRef.get() != null) {
                    Intent intent = new Intent(contextRef.get(), PhotoPreviewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("path", path);
                    contextRef.get().startActivity(intent);
                    if (callback != null) {
                        Map<String, Object> successData = new HashMap<>();
                        successData.put("code", SUCCESS.getCode());
                        successData.put("message", SUCCESS.getMsg());
                        PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                        callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                    }
                    return;
                }
                if (callback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", "-1");
                    errorData.put("message", "APP页面不存在");
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                }
                return;
            }
        }
        if (callback != null) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("code", "-1");
            errorData.put("message", "H5参数错误");
            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
            callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 拉起支付 App
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void openPayApp(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        payInfo = js2AppInfo;
        payParser = parser;
        payCallback = callback;
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            String url = dataObj.optString("url");
            String referer = dataObj.optString("redirectUrl");
            String scheme = dataObj.optString("scheme");
            if (!TextUtils.isEmpty(scheme)) {
                Intent schemeIntent = new Intent(Intent.ACTION_VIEW);
                schemeIntent.setData(Uri.parse(scheme));
                PackageManager packageManager = contextRef.get().getPackageManager();
                if (schemeIntent.resolveActivity(packageManager) == null) {
                    if (payCallback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", ERROR_PAY_APP_NOT_INSTALL.getCode());
                        errorData.put("message", ERROR_PAY_APP_NOT_INSTALL.getMsg());
                        PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                        payCallback.jsCallAppFail(payInfo, app2JsInfo, payParser);
                    }
                    return;
                }
            }
            if (!TextUtils.isEmpty(url)) {
                if (contextRef != null && contextRef.get() != null) {
                    WebActivity.startPay(contextRef.get(), url, referer, new WebActivity.Callback() {
                        @Override
                        public void onSuccess() {
                            if (payCallback != null) {
                                Map<String, Object> successData = new HashMap<>();
                                successData.put("code", SUCCESS.getCode());
                                successData.put("message", SUCCESS.getMsg());
                                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                                payCallback.jsCallAppSuccess(payInfo, app2JsInfo, payParser);
                            }
                        }

                        @Override
                        public void onFail(String errCode, String errMsg) {
                            if (payCallback != null) {
                                Map<String, Object> errorData = new HashMap<>();
                                errorData.put("code", errCode);
                                errorData.put("message", errMsg);
                                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                                payCallback.jsCallAppFail(payInfo, app2JsInfo, payParser);
                            }
                        }
                    });
                    return;
                }
                if (callback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", "-1");
                    errorData.put("message", "APP页面不存在");
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                }
                return;
            }
        }
        if (callback != null) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("code", "-1");
            errorData.put("message", "H5参数错误");
            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
            callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 设置广告图片
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void getAdBannerURL(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        if (!TextUtils.isEmpty(bannerUrl)) {
            if (callback != null) {
                Map<String, Object> successData = new HashMap<>();
                successData.put("url", bannerUrl);
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
            }
        } else {
            if (callback != null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", ERROR_NO_BANNER_URL.getCode());
                errorData.put("message", ERROR_NO_BANNER_URL.getMsg());
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
            }
        }

    }

    /**
     * 广告图片点击事件
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void adClick(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        if (callback != null) {
            Map<String, Object> successData = new HashMap<>();
            successData.put("code", SUCCESS.getCode());
            successData.put("message", SUCCESS.getMsg());
            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
        }
        if (bannerCallback != null) {
            bannerCallback.onBannerClick();
        } else {
            if (callback != null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", ERROR_NO_BANNER.getCode());
                errorData.put("message", ERROR_NO_BANNER.getMsg());
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
            }
        }
    }

    /**
     * 获取SDK版本信息
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void getAppInfo(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        if (callback != null) {
            Map<String, Object> successData = new HashMap<>();
            successData.put("version", H5SDKHelper.getSdkVersion());
            successData.put("manufacturer", Build.MANUFACTURER);
            successData.put("model", Build.MODEL);
            successData.put("product", Build.PRODUCT);
            successData.put("OSVersion", Build.VERSION.RELEASE);
            successData.put("deviceInfo", Build.MANUFACTURER + "_" + Build.MODEL);
            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 申请权根（拍照、拍视频）
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void authorization(final PYCreditJs2AppInfo js2AppInfo, final PYCreditJsParser parser, final JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            List<String> permList = new ArrayList<>();
            boolean image = dataObj.optBoolean("image");
            boolean video = dataObj.optBoolean("video");
            if (image) {
                if (!permList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (!permList.contains(Manifest.permission.CAMERA)) {
                    permList.add(Manifest.permission.CAMERA);
                }
            }
            if (video) {
                if (!permList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (!permList.contains(Manifest.permission.CAMERA)) {
                    permList.add(Manifest.permission.CAMERA);
                }
                if (!permList.contains(Manifest.permission.RECORD_AUDIO)) {
                    permList.add(Manifest.permission.RECORD_AUDIO);
                }
            }
            if (!permList.isEmpty()) {
                if (contextRef != null && contextRef.get() != null) {
                    String[] perms = permList.toArray(new String[permList.size()]);
                    if (PermChecker.hasPermissions(contextRef.get(), perms)) {
                        if (callback != null) {
                            Map<String, Object> successData = new HashMap<>();
                            successData.put("code", SUCCESS.getCode());
                            successData.put("message", SUCCESS.getMsg());
                            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                        }
                    } else {
                        PermRequestActivity.requestPermissions(contextRef.get(), perms, new PermChecker.RequestPermCallback() {
                            @Override
                            public void onRequestSuccess() {
                                if (callback != null) {
                                    Map<String, Object> successData = new HashMap<>();
                                    successData.put("code", SUCCESS.getCode());
                                    successData.put("message", SUCCESS.getMsg());
                                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                                }
                            }

                            @Override
                            public void onRequestFail() {
                                if (callback != null) {
                                    Map<String, Object> errorData = new HashMap<>();
                                    errorData.put("code", ERROR_REQUEST_PERM_FAIL.getCode());
                                    errorData.put("message", ERROR_REQUEST_PERM_FAIL.getMsg());
                                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                                }
                            }
                        });
                    }
                } else {
                    if (callback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", ERROR_REQUEST_PERM_FAIL.getCode());
                        errorData.put("message", ERROR_REQUEST_PERM_FAIL.getMsg());
                        PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                        callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                    }
                }
            } else {
                if (callback != null) {
                    Map<String, Object> successData = new HashMap<>();
                    successData.put("code", SUCCESS.getCode());
                    successData.put("message", SUCCESS.getMsg());
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                }
            }
        }
    }

    /**
     * 检查是否支持视频录制
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void checkVideoRecording(PYCreditJs2AppInfo js2AppInfo, PYCreditJsParser parser, JsCallAppCallback callback) {
        checkRecordInfo = js2AppInfo;
        checkRecordParser = parser;
        checkRecordCallback = callback;
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (H5JsHelper.fileChooserEnable.get()) {
                    checkRecordSupportResult(true);
                } else {
                    checkRecordSupportResult(false);
                }
            }
        }, 1000);
    }

    /**
     * 代理网络请求
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void request(final PYCreditJs2AppInfo js2AppInfo, final PYCreditJsParser parser, final JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            String url = dataObj.optString("url");
            String method = dataObj.optString("method");
            JSONObject headers = dataObj.optJSONObject("headers");
            int timeout = dataObj.optInt("timeout");
            JSONObject postParms = dataObj.optJSONObject("data");
            String postString = null;
            if (postParms == null) {//请求体没有参数取字符串直接发送
                postString = dataObj.optString("data");
            }
            JSONArray files = dataObj.optJSONArray("files");
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(method)) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", "-1");
                errorData.put("message", "H5参数错误");
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                return;
            }
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .build();
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url);
            if (headers != null) {//请求头
                Iterator<String> keys = headers.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = headers.optString(key);
                    requestBuilder.addHeader(key, value);
                }
            }
            if ("GET".equalsIgnoreCase(method)) {
                requestBuilder.get();
            } else if ("HEAD".equals(method)) {
                requestBuilder.head();
            } else if ("POST".equals(method)) {
                RequestBody requestBody;
                if (!TextUtils.isEmpty(postString)) {//请求体为字符串
                    String mimeType = "text/plain";
                    if (headers != null) {
                        String contentType = headers.optString("Content-Type");
                        if (!TextUtils.isEmpty(contentType)) {
                            mimeType = contentType;
                        }
                    }
                    requestBody = RequestBody.create(MediaType.parse(mimeType), postString);
                } else {
                    MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
                    bodyBuilder.setType(MultipartBody.FORM);
                    if (postParms != null) {//表单参数
                        Iterator<String> keys = postParms.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = postParms.optString(key);
                            bodyBuilder.addFormDataPart(key, value);
                        }
                    }
                    if (files != null && files.length() > 0) {//文件
                        for (int i = 0; i < files.length(); i++) {
                            JSONObject fileObj = files.optJSONObject(i);
                            if (fileObj != null) {
                                String keyName = fileObj.optString("dataKey");
                                String filePath = fileObj.optString("localId");
                                File file = new File(filePath);
                                if (file.exists()) {
                                    String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
                                    String mimeType = "image/jpeg";
                                    if (!TextUtils.isEmpty(ext)) {
                                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                                        if (TextUtils.isEmpty(mimeType)) {
                                            mimeType = "image/jpeg";
                                        }
                                    }
                                    bodyBuilder.addFormDataPart(keyName, file.getName(), RequestBody.create(MediaType.parse(mimeType), file));
                                }
                            }
                        }
                    }
                    requestBody = bodyBuilder.build();
                }
                ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestBody) {
                    private int last = 0;

                    @Override
                    protected void onProgress(long current, long total, boolean done) {
                        int progress = (int) (current * 100 / total);
                        if (progress - last >= 5) {
                            if (callback != null) {
                                Map<String, Object> progressData = new HashMap<>();
                                progressData.put("value", progress);
                                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(progressData);
                                callback.jsCallAppProgress(js2AppInfo, app2JsInfo, parser);
                            }
                            last = progress;
                        }
                    }
                };
                requestBuilder.post(progressRequestBody);
            }
            Request request = requestBuilder.build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Map<String, Object> errorData = new HashMap<>();
                    if (e.getCause().equals(SocketTimeoutException.class)) {//请求超时
                        errorData.put("code", ERROR_TIMEOUT.getCode());
                        errorData.put("message", ERROR_TIMEOUT.getMsg());
                    } else if (e.getCause().equals(UnknownHostException.class)) {//一般没有网络报这个错
                        errorData.put("code", ERROR_NO_NETWORK.getCode());
                        errorData.put("message", ERROR_NO_NETWORK.getMsg());
                    } else {
                        errorData.put("code", "-1");
                        errorData.put("message", e.getMessage());
                    }
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Map<String, Object> successData = new HashMap<>();
                    successData.put("responseStatus", response.code());
                    Map<String, String> headers = new HashMap<>();
                    Set<String> names = response.headers().names();
                    if (names != null) {
                        for (String name : names) {
                            headers.put(name, response.header(name));
                        }
                    }
                    successData.put("responseHeaders", headers);
                    successData.put("responseBody", response.body() != null ? response.body().string() : response.message());
                    PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                }
            });
        }
    }

    /**
     * 调用相机拍照
     */
    public boolean startCapturePic() {
        if (contextRef.get() != null) {
            File cacheDir = DeviceUtils.getCacheDir(contextRef.get());
            cameraFilePath = cacheDir.getAbsolutePath() + File.separator + "image_" + System.currentTimeMillis() + ".jpg";
            CaptureConfig captureConfig = new CaptureConfig(false, false, 0);
            JSONObject dataObj = captureInfo.getDataObj();
            int thumbWidth = dataObj.optInt("thumbWidth", Integer.MAX_VALUE);
            int defaultDirection = dataObj.optInt("defaultDirection", Integer.MAX_VALUE);
            if (!dataObj.has("imageType")) {//没有类型以缩略图宽度来区分
                if (thumbWidth > 480) {
                    captureConfig.landscape = false;
                    captureConfig.imageType = CaptureConfig.IMAGE_TYPE_IN_HAND;
                } else {
                    captureConfig.landscape = true;
                    captureConfig.imageType = CaptureConfig.IMAGE_TYPE_FRONT;//这里没法判断是身份证正面还是反面，默认正面
                }
            } else {
                int imageType = dataObj.optInt("imageType", CaptureConfig.IMAGE_TYPE_IN_HAND);
                captureConfig.imageType = imageType;
                if (imageType == CaptureConfig.IMAGE_TYPE_IN_HAND) {
                    captureConfig.landscape = false;
                } else {
                    captureConfig.landscape = true;
                }
            }
            if (defaultDirection == 0) {
                captureConfig.defaultFrontCamera = true;
            } else {
                captureConfig.defaultFrontCamera = false;
            }

            CaptureCallback callback = new CaptureCallback() {
                @Override
                public void onSuccess(String savePath) {
                    //拍照成功
                    JSONObject dataObj = captureInfo.getDataObj();
                    int thumbWidth = dataObj.optInt("thumbWidth", Integer.MAX_VALUE);
                    int thumbHeight = dataObj.optInt("thumbHeight", Integer.MAX_VALUE);
                    Bitmap bitmap = ImageUtils.getBitmap(cameraFilePath, thumbWidth, thumbHeight);
                    if (bitmap != null) {
                        byte[] bytes = ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG);
                        bitmap.recycle();
                        String base64Encode2String = EncodeUtils.base64Encode2String(bytes);
                        if (captureCallback != null) {
                            File imageFile = new File(cameraFilePath);
                            Map<String, Object> successData = new HashMap<>();
                            successData.put("base64", base64Encode2String);
                            successData.put("localId", cameraFilePath);
                            successData.put("filename", imageFile.getName());
                            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                            captureCallback.jsCallAppSuccess(captureInfo, app2JsInfo, captureParser);
                        }
                    } else {
                        if (captureCallback != null) {
                            Map<String, Object> errorData = new HashMap<>();
                            errorData.put("code", ERROR_IMAGE_HANDLE.getCode());
                            errorData.put("message", ERROR_IMAGE_HANDLE.getMsg());
                            PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                            captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                        }
                    }
                }

                @Override
                public void onFail(String errCode, String errMsg) {
                    if (captureCallback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", errCode);
                        errorData.put("message", errMsg);
                        PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                        captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                    }
                }
            };
            if (capture != null) {
                capture.startCapture(contextRef.get(), captureConfig, cameraFilePath, callback);
            }
        }
        return true;
    }

    /**
     * 检查是否支持录制视频结果
     *
     * @param support
     */
    private void checkRecordSupportResult(boolean support) {
        if (checkRecordCallback != null) {
            if (support) {
                Map<String, Object> successData = new HashMap<>();
                successData.put("code", SUCCESS.getCode());
                successData.put("message", SUCCESS.getMsg());
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(successData);
                checkRecordCallback.jsCallAppSuccess(checkRecordInfo, app2JsInfo, checkRecordParser);
            } else {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", ERROR_VIDEO_RECORD_UN_SUPPORT.getCode());
                errorData.put("message", ERROR_VIDEO_RECORD_UN_SUPPORT.getMsg());
                PYCreditApp2JsInfo app2JsInfo = new PYCreditApp2JsInfo(errorData);
                checkRecordCallback.jsCallAppFail(checkRecordInfo, app2JsInfo, checkRecordParser);
            }
        }

    }
}
