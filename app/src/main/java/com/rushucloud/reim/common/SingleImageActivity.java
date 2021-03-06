package com.rushucloud.reim.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;
import classes.widget.subscaleview.SubsamplingScaleImageView;

public class SingleImageActivity extends Activity
{
    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_single);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SingleImageActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SingleImageActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            ViewUtils.goBack(this);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        String imagePath = getIntent().getStringExtra("imagePath");

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.avatarImageView);
        imageView.setImageUri(imagePath);
    }
}