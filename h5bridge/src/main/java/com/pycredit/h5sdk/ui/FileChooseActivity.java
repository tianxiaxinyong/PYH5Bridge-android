package com.pycredit.h5sdk.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.pycredit.h5sdk.capture.CaptureCallback;
import com.pycredit.h5sdk.capture.CaptureConfig;
import com.pycredit.h5sdk.perm.PermChecker;
import com.pycredit.h5sdk.utils.DeviceUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangx
 * @date 2018/1/15
 */

public class FileChooseActivity extends Activity {

    public static final String EXTRA_ACCEPT_TYPE = "extra_accept_type";
    public static final String EXTRA_CAPTURE = "extra_capture";
    public static final String EXTRA_CALLBACK_KEY = "extra_callback_key";

    private static final int REQUEST_CODE_FILE_UPLOAD = 100;

    public interface FileChooseCallback {
        /**
         * 选择成功
         *
         * @param uri
         */
        void onSuccess(Uri uri);

        /**
         * 选择失败
         */
        void onFail();
    }

    private static Map<Long, FileChooseCallback> callbackMap = new HashMap<>();

    private long currentCallbackKey;

    private String acceptType;
    private boolean capture;

    private Uri mediaUri;

    private static void setChooseCallback(long callbackKey, FileChooseCallback chooseCallback) {
        FileChooseActivity.callbackMap.put(callbackKey, chooseCallback);
    }

    public static void startFileChoose(Context context, String acceptType, boolean capture, FileChooseCallback chooseCallback) {
        long callbackKey = System.currentTimeMillis();
        setChooseCallback(callbackKey, chooseCallback);
        Intent intent = new Intent(context, FileChooseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_ACCEPT_TYPE, acceptType);
        intent.putExtra(EXTRA_CAPTURE, capture);
        intent.putExtra(EXTRA_CALLBACK_KEY, callbackKey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acceptType = getIntent().getStringExtra(EXTRA_ACCEPT_TYPE);
        capture = getIntent().getBooleanExtra(EXTRA_CAPTURE, false);
        currentCallbackKey = getIntent().getLongExtra(EXTRA_CALLBACK_KEY, 0);
        Intent target = null;
        if (capture) {
            target = createCaptureEnableIntent(acceptType);
        } else {
            target = createCaptureDisableIntent(acceptType);
        }
        if (!capture) {
            if (PermChecker.hasPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
                startUploadFile(target);
            } else {
                final Intent finalTarget = target;
                PermRequestActivity.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new PermChecker.RequestPermCallback() {
                    @Override
                    public void onRequestSuccess() {
                        startUploadFile(finalTarget);
                    }

                    @Override
                    public void onRequestFail() {
                        uploadFail();
                    }
                });
            }
        } else {
            if (acceptType != null) {
                if (acceptType.contains("image") || acceptType.contains("video")) {
                    String[] perms = null;
                    if (acceptType.contains("image")) {
                        perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    }
                    if (acceptType.contains("video")) {
                        perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                    }
                    if (PermChecker.hasPermissions(this, perms)) {
                        if (acceptType.contains("video")) {
                            startUploadVideoFile();
                        } else {
                            startUploadFile(target);
                        }
                    } else {
                        final Intent finalTarget = target;
                        PermRequestActivity.requestPermissions(this, perms, new PermChecker.RequestPermCallback() {
                            @Override
                            public void onRequestSuccess() {
                                if (acceptType.contains("video")) {
                                    startUploadVideoFile();
                                } else {
                                    startUploadFile(finalTarget);
                                }
                            }

                            @Override
                            public void onRequestFail() {
                                uploadFail();
                            }
                        });
                    }
                } else if (acceptType.contains("audio")) {
                    String[] perms = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                    if (PermChecker.hasPermissions(this, perms)) {
                        startUploadFile(target);
                    } else {
                        final Intent finalTarget = target;
                        PermRequestActivity.requestPermissions(this, perms, new PermChecker.RequestPermCallback() {
                            @Override
                            public void onRequestSuccess() {
                                startUploadFile(finalTarget);
                            }

                            @Override
                            public void onRequestFail() {
                                uploadFail();
                            }
                        });
                    }
                }
            }

        }
    }

    private FileChooseCallback getCurrentCallback() {
        return callbackMap.get(currentCallbackKey);
    }

    private void startUploadFile(Intent target) {
        if (target != null) {
            try {
                startActivityForResult(target, REQUEST_CODE_FILE_UPLOAD);
            } catch (Exception e) {
                e.printStackTrace();
                uploadFail();
            }
        } else {
            uploadFail();
        }
    }

    /**
     * 开始拍摄视频
     */
    private void startUploadVideoFile() {
        CaptureConfig captureConfig = new CaptureConfig(true, false, 0);
        String savePath = generateMediaPath(MediaSourceType.VIDEO);
        CameraActivity.startCapture(this, captureConfig, savePath, new CaptureCallback() {
            @Override
            public void onSuccess(String savePath) {
                Uri uri = fileToUri(new File(savePath));
                uploadSuccess(uri);
            }

            @Override
            public void onFail(String errCode, String errMsg) {
                uploadFail();
            }
        }, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_UPLOAD) {
            if (resultCode == RESULT_OK) {
                Uri uri = data != null ? data.getData() : null;
                if (uri != null) {
                    uploadSuccess(uri);
                } else {
                    if (mediaUri != null) {
                        uploadSuccess(mediaUri);
                    } else {
                        uploadFail();
                    }
                }
            } else {
                uploadFail();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onFail();
            callbackMap.remove(currentCallbackKey);
        }
        super.onDestroy();
    }

    private void uploadSuccess(Uri uri) {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onSuccess(uri);
            callbackMap.remove(currentCallbackKey);
        }
        finish();
        mediaUri = null;
    }


    private void uploadFail() {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onFail();
            callbackMap.remove(currentCallbackKey);
        }
        mediaUri = null;
        finish();
    }

    /**
     * 拍照或录制关闭(选择文件)
     *
     * @param acceptType
     * @return
     */
    private Intent createCaptureDisableIntent(String acceptType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (acceptType != null) {
            String[] split = acceptType.split(",");
            if (split != null && split.length > 0) {
                if (split.length == 1) {
                    intent.setType(split[0]);
                } else {
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, split);
                }
            }
        }
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        }
        return null;
    }

    /**
     * 拍照或录制打开(拍摄、录制文件)
     *
     * @param acceptType
     * @return
     */
    private Intent createCaptureEnableIntent(String acceptType) {
        if (acceptType != null) {
            MediaSourceType mediaSourceType = null;
            if (acceptType.startsWith("image/*")) {
                mediaSourceType = MediaSourceType.IMAGE;
            } else if (acceptType.startsWith("video/*")) {
                mediaSourceType = MediaSourceType.VIDEO;
            } else if (acceptType.startsWith("audio/*")) {
                mediaSourceType = MediaSourceType.AUDIO;
            }
            if (mediaSourceType != null) {
                return createMediaIntent(mediaSourceType);
            }
        }
        return null;
    }

    /**
     * 拍照片
     *
     * @return
     */
    private Intent createMediaIntent(MediaSourceType type) {
        Intent intent = new Intent(type.action);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            String mediaFilePath = generateMediaPath(type);
            File file = new File(mediaFilePath);
            mediaUri = fileToUri(file);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//如果api大于Android api23
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//加入flag
            } else {
                intent.addCategory(Intent.CATEGORY_DEFAULT);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
            return intent;
        }
        return null;
    }

    /**
     * 生成文件路径
     *
     * @param mediaSourceType
     * @return
     */
    private String generateMediaPath(MediaSourceType mediaSourceType) {
        File cacheDir = DeviceUtils.getCacheDir(this);
        return cacheDir.getAbsolutePath() + File.separator + mediaSourceType.getFileName();
    }

    /**
     * File转uri
     *
     * @param file
     * @return
     */
    private Uri fileToUri(File file) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//如果api大于Android api23
            return FileProvider.getUriForFile(this, getPackageName() + ".h5sdk.fileprovider", file);//替换获取uri的获取方式
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * 媒体源类型
     */
    enum MediaSourceType {
        /**
         * 照片
         */
        IMAGE(MediaStore.ACTION_IMAGE_CAPTURE, ".jpg"),
        /**
         * 视频
         */
        VIDEO(MediaStore.ACTION_VIDEO_CAPTURE, ".mp4"),
        /**
         * 录音
         */
        AUDIO(MediaStore.Audio.Media.RECORD_SOUND_ACTION, ".mp3");

        private String action;
        private String extension;

        MediaSourceType(String action, String extension) {
            this.action = action;
            this.extension = extension;
        }

        private String getFileName() {
            return String.format(name() + "_%s" + extension, System.currentTimeMillis());
        }
    }

}
