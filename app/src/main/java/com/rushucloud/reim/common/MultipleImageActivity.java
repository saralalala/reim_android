package com.rushucloud.reim.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.utils.ViewUtils;
import classes.widget.galleryWidget.FilePagerAdapter;
import classes.widget.galleryWidget.GalleryViewPager;

public class MultipleImageActivity extends Activity
{
    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_multiple);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("MultipleImageActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("MultipleImageActivity");
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
        Bundle bundle = getIntent().getExtras();
        List<String> pathList = bundle.getStringArrayList("imagePath");
        int index = bundle.getInt("index", 0);

        FilePagerAdapter adapter = new FilePagerAdapter(this, pathList);
        GalleryViewPager galleryViewPager = (GalleryViewPager) findViewById(R.id.galleryViewPager);
        galleryViewPager.setOffscreenPageLimit(3);
        galleryViewPager.setAdapter(adapter);
        galleryViewPager.setCurrentItem(index);
    }
}