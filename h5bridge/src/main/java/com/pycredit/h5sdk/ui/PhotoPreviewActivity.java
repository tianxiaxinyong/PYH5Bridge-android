package com.pycredit.h5sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pycredit.h5sdk.R;


/**
 * 图片预览
 * Created by huangx on 2017/10/14.
 */

public class PhotoPreviewActivity extends Activity {

    protected ImageView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        }
        FrameLayout container = new FrameLayout(this);
        PhotoView photoView = new PhotoView(this);
        photoView.setBackgroundColor(Color.BLACK);
        container.addView(photoView);
        back = new ImageView(this);
        back.setImageResource(R.drawable.h5_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        back.setPadding((int) dp2px(this, 16), (int) dp2px(this, 16), (int) dp2px(this, 16), (int) dp2px(this, 16));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(back, layoutParams);
        setContentView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        String path = getIntent().getStringExtra("path");
        ImageLoader.getInstance().displayImage("file://" + path, photoView);

    }

    public static float dp2px(Context context, int dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dip, context.getResources().getDisplayMetrics());
    }
}
