package com.rushucloud.reim.start;

import android.app.Activity;
import android.os.Bundle;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

public class NewFeaturesActivity extends Activity
{
    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new_features);
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("NewFeaturesActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("NewFeaturesActivity");
        MobclickAgent.onPause(this);
    }
}
