package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.cameraview.CameraHelper;
import com.google.android.cameraview.CameraView;
import com.pycredit.h5sdk.R;
import com.pycredit.h5sdk.capture.CaptureCallback;
import com.pycredit.h5sdk.capture.CaptureConfig;
import com.pycredit.h5sdk.js.JsCallAppErrorCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangx
 * @date 2017/11/28
 */

public class CameraActivity extends Activity {

    public static final String EXTRA_CAPTURE_CONFIG = "extra_capture_config";
    public static final String EXTRA_SAVE_PATH = "extra_save_path";
    public static final String EXTRA_IS_RECORD_VIDEO = "extra_is_record_video";
    public static final String EXTRA_CALLBACK_KEY = "extra_callback_key";

    private CameraView cameraView;
    private ImageView ivBack;
    private ImageView ivCapture;
    private RelativeLayout rlDone;
    private ImageView ivUndo;
    private ImageView ivConfirm;
    private LinearLayout llSettings;
    private ImageView ivFlash;
    private ImageView ivSwitchCamera;
    private ImageView ivCover;
    private Chronometer cmTimer;

    private CaptureConfig captureConfig;

    private String savePath;

    private Handler mBackgroundHandler;

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private int mCurrentFlash;

    private static Map<Long, CaptureCallback> callbackMap = new HashMap<>();

    private long currentCallbackKey;

    private boolean isVideoRecord;

    public static void setCallback(long callbackKey, CaptureCallback callback) {
        callbackMap.put(callbackKey, callback);
    }

    public static void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback callback) {
        startCapture(context, captureConfig, savePath, callback, false);
    }

    public static void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback callback, boolean isVideoRecord) {
        long callbackKey = System.currentTimeMillis();
        setCallback(callbackKey, callback);
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_CAPTURE_CONFIG, captureConfig);
        intent.putExtra(EXTRA_SAVE_PATH, savePath);
        intent.putExtra(EXTRA_IS_RECORD_VIDEO, isVideoRecord);
        intent.putExtra(EXTRA_CALLBACK_KEY, callbackKey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState != null) {
            captureConfig = (CaptureConfig) savedInstanceState.getSerializable(EXTRA_CAPTURE_CONFIG);
            savePath = savedInstanceState.getString(EXTRA_SAVE_PATH);
            isVideoRecord = savedInstanceState.getBoolean(EXTRA_IS_RECORD_VIDEO, false);
            currentCallbackKey = savedInstanceState.getLong(EXTRA_CALLBACK_KEY);
        } else {
            captureConfig = (CaptureConfig) getIntent().getSerializableExtra(EXTRA_CAPTURE_CONFIG);
            savePath = getIntent().getStringExtra(EXTRA_SAVE_PATH);
            isVideoRecord = getIntent().getBooleanExtra(EXTRA_IS_RECORD_VIDEO, false);
            currentCallbackKey = getIntent().getLongExtra(EXTRA_CALLBACK_KEY, 0);
        }
        if (captureConfig == null) {
            captureConfig = new CaptureConfig(true, false, 0);
        }
        if (captureConfig.landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        initViews();
    }

    private CaptureCallback getCurrentCallback() {
        return callbackMap.get(currentCallbackKey);
    }

    private void initViews() {
        cameraView = (CameraView) findViewById(R.id.cameraView);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivCapture = (ImageView) findViewById(R.id.iv_capture);
        rlDone = (RelativeLayout) findViewById(R.id.rl_done);
        ivUndo = (ImageView) findViewById(R.id.iv_undo);
        ivConfirm = (ImageView) findViewById(R.id.iv_confirm);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);
        ivFlash = (ImageView) findViewById(R.id.iv_flash);
        ivSwitchCamera = (ImageView) findViewById(R.id.iv_switch_camera);
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        cmTimer = (Chronometer) findViewById(R.id.cm_timer);
        cameraView.addCallback(new CameraView.Callback() {
            private boolean takingPic;

            @Override
            public void onPictureTaken(CameraView cameraView, final byte[] data) {
                if (!takingPic) {
                    takingPic = true;
                    getBackgroundHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(savePath);
                            OutputStream os = null;
                            try {
                                os = new FileOutputStream(file);
                                os.write(data);
                                os.close();
                                captureSuccess();
                            } catch (IOException e) {
                                e.printStackTrace();
                                captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
                            } finally {
                                if (os != null) {
                                    try {
                                        os.close();
                                    } catch (IOException e) {
                                        // Ignore
                                    }
                                }
                                takingPic = false;
                            }
                        }
                    });
                }
            }

            @Override
            public void onVideoRecorded(String savePath) {
                super.onVideoRecorded(savePath);
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isCameraOpened()) {
                    if (isVideoRecord) {
                        if (!cameraView.isRecordering()) {
                            llSettings.setVisibility(View.GONE);
                            CamcorderProfile camcorderProfile = CameraHelper.chooseOptimalCamcorderProfile(cameraView.getCameraId(), 640, 480);
                            if (camcorderProfile == null) {
                                return;
                            }
                            cameraView.startVideoRecord(savePath, camcorderProfile, new MediaRecorder.OnInfoListener() {
                                @Override
                                public void onInfo(MediaRecorder mr, int what, int extra) {

                                }
                            }, new MediaRecorder.OnErrorListener() {
                                @Override
                                public void onError(MediaRecorder mr, int what, int extra) {
                                    captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
                                }
                            });
                            cmTimer.setVisibility(View.VISIBLE);
                            cmTimer.setBase(SystemClock.elapsedRealtime());
                            cmTimer.start();
                            ivCapture.setImageResource(R.drawable.ic_stop);
                        } else {
                            cameraView.stopVideoRecord();
                            cmTimer.stop();
                            cmTimer.setVisibility(View.GONE);
                            ivCapture.setVisibility(View.GONE);
                            rlDone.setVisibility(View.VISIBLE);
                        }
                    } else {
                        cameraView.takePicture();
                    }
                }
            }
        });
        ivUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llSettings.setVisibility(View.VISIBLE);
                ivCapture.setVisibility(View.VISIBLE);
                ivCapture.setImageResource(R.drawable.ic_record);
                rlDone.setVisibility(View.GONE);
                cameraView.stop();
                cameraView.start();
            }
        });
        ivConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureSuccess();
            }
        });
        ivFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                ivFlash.setImageResource(FLASH_ICONS[mCurrentFlash]);
                cameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
            }
        });
        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int facing = cameraView.getFacing();
                cameraView.setFacing(facing == CameraView.FACING_FRONT ?
                        CameraView.FACING_BACK : CameraView.FACING_FRONT);
            }
        });
        if (captureConfig.defaultFrontCamera) {
            cameraView.setFacing(CameraView.FACING_FRONT);
        } else {
            cameraView.setFacing(CameraView.FACING_BACK);
        }
        switch (captureConfig.imageType) {
            case CaptureConfig.IMAGE_TYPE_IN_HAND:
                ivCover.setImageResource(R.drawable.img_cover_in_hand);
                break;
            case CaptureConfig.IMAGE_TYPE_FRONT:
                ivCover.setImageResource(R.drawable.img_cover_front);
                break;
            case CaptureConfig.IMAGE_TYPE_BACK:
                ivCover.setImageResource(R.drawable.img_cover_back);
                break;
            default:
                ivCover.setImageResource(R.drawable.img_cover_in_hand);
                break;
        }
        if (isVideoRecord) {
            ivCapture.setImageResource(R.drawable.ic_record);
        } else {
            ivCapture.setImageResource(R.drawable.ic_camera);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            cameraView.stop();
        } catch (Exception e) {
            e.printStackTrace();
            captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
        }
        setContentView(R.layout.activity_camera);
        initViews();
        try {
            cameraView.start();
        } catch (Exception e) {
            e.printStackTrace();
            captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_CAPTURE_CONFIG, captureConfig);
        outState.putString(EXTRA_SAVE_PATH, savePath);
        outState.putBoolean(EXTRA_IS_RECORD_VIDEO, isVideoRecord);
        outState.putLong(EXTRA_CALLBACK_KEY, currentCallbackKey);
        super.onSaveInstanceState(outState);
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            cameraView.start();
        } catch (Exception e) {
            e.printStackTrace();
            captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
        }
    }

    @Override
    protected void onPause() {
        try {
            cameraView.stop();
        } catch (Exception e) {
            e.printStackTrace();
            captureFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE);
        }
        super.onPause();
    }

    private void captureSuccess() {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onSuccess(savePath);
            callbackMap.remove(currentCallbackKey);
        }
        finish();
    }

    private void captureFail(JsCallAppErrorCode errorCode) {
        if (getCurrentCallback() != null) {
            getCurrentCallback().onFail(errorCode.getCode(), errorCode.getMsg());
            callbackMap.remove(currentCallbackKey);
        }
        try {
            new File(savePath).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        if (getCurrentCallback() != null) {
            captureFail(JsCallAppErrorCode.ERROR_CAMERA_USER_CANCEL);
        }
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }
}
