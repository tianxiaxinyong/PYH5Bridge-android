package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.google.android.cameraview.CameraView;
import com.pycredit.h5sdk.R;
import com.pycredit.h5sdk.capture.CaptureCallback;
import com.pycredit.h5sdk.capture.CaptureConfig;
import com.pycredit.h5sdk.js.JsCallAppErrorCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author huangx
 * @date 2017/11/28
 */

public class CameraActivity extends Activity {

    public static final String EXTRA_CAPTURE_CONFIG = "extra_capture_config";
    public static final String EXTRA_SAVE_PATH = "extra_save_path";

    private CameraView cameraView;
    private ImageView ivBack;
    private ImageView ivCapture;
    private ImageView ivFlash;
    private ImageView ivSwitchCamera;
    private ImageView ivCover;

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

    private static CaptureCallback callback;

    public static void setCallback(CaptureCallback callback) {
        CameraActivity.callback = callback;
    }

    public static void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback callback) {
        CameraActivity.setCallback(callback);
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_CAPTURE_CONFIG, captureConfig);
        intent.putExtra(EXTRA_SAVE_PATH, savePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState != null) {
            captureConfig = (CaptureConfig) savedInstanceState.getSerializable(EXTRA_CAPTURE_CONFIG);
            savePath = savedInstanceState.getString(EXTRA_SAVE_PATH);
        } else {
            captureConfig = (CaptureConfig) getIntent().getSerializableExtra(EXTRA_CAPTURE_CONFIG);
            savePath = getIntent().getStringExtra(EXTRA_SAVE_PATH);
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

    private void initViews() {
        cameraView = (CameraView) findViewById(R.id.cameraView);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivCapture = (ImageView) findViewById(R.id.iv_capture);
        ivFlash = (ImageView) findViewById(R.id.iv_flash);
        ivSwitchCamera = (ImageView) findViewById(R.id.iv_switch_camera);
        ivCover = (ImageView) findViewById(R.id.iv_cover);

        cameraView.addCallback(new CameraView.Callback() {
            @Override
            public void onPictureTaken(CameraView cameraView, final byte[] data) {
                getBackgroundHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(savePath);
                        OutputStream os = null;
                        try {
                            os = new FileOutputStream(file);
                            os.write(data);
                            os.close();
                            if (callback != null) {
                                callback.onSuccess(savePath);
                            }
                        } catch (IOException e) {
                            callback.onFail(JsCallAppErrorCode.ERROR_IMAGE_HANDLE.getCode(), JsCallAppErrorCode.ERROR_IMAGE_HANDLE.getMsg());
                        } finally {
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    // Ignore
                                }
                            }
                            finish();
                        }
                    }
                });
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
                    cameraView.takePicture();
                }
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cameraView.stop();
        setContentView(R.layout.activity_camera);
        initViews();
        cameraView.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_CAPTURE_CONFIG, captureConfig);
        outState.putString(EXTRA_SAVE_PATH, savePath);
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
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (callback != null) {
            callback = null;
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
