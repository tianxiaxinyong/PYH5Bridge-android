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

import com.pycredit.h5sdk.perm.PermChecker;

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

    private Uri imageUri;

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
                    if (PermChecker.hasPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE})) {
                        startUploadFile(target);
                    } else {
                        final Intent finalTarget = target;
                        PermRequestActivity.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, new PermChecker.RequestPermCallback() {
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
                } else if (acceptType.contains("audio")) {
                    if (PermChecker.hasPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE})) {
                        startUploadFile(target);
                    } else {
                        final Intent finalTarget = target;
                        PermRequestActivity.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, new PermChecker.RequestPermCallback() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_UPLOAD) {
            if (resultCode == RESULT_OK) {
                Uri uri = data != null ? data.getData() : null;
                if (uri != null) {
                    uploadSuccess(uri);
                } else {
                    if (imageUri != null) {
                        uploadSuccess(imageUri);
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
        imageUri = null;
    }


    private void uploadFail() {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onFail();
            callbackMap.remove(currentCallbackKey);
        }
        imageUri = null;
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
            if (acceptType.startsWith("image/*")) {
                return createCaptureIntent();
            } else if (acceptType.startsWith("video/*")) {
                return createCamcorderIntent();
            } else if (acceptType.startsWith("audio/*")) {
                return createSoundRecorderIntent();
            }
        }
        return null;
    }

    /**
     * 拍照片
     *
     * @return
     */
    private Intent createCaptureIntent() {
        File cacheDir = getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = getCacheDir();
        }
        String cameraFilePath = cacheDir.getAbsolutePath() + File.separator + "image_" + System.currentTimeMillis();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            File file = new File(cameraFilePath);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//如果api大于Android api23
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".h5sdk.fileprovider", file);//替换获取uri的获取方式
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//加入flag
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } else {
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                imageUri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }
            return intent;
        }
        return null;
    }

    /**
     * 录视频
     *
     * @return
     */
    private Intent createCamcorderIntent() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        }
        return null;
    }

    /**
     * 录声音
     *
     * @return
     */
    private Intent createSoundRecorderIntent() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        }
        return null;
    }
}
