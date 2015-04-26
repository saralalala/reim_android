package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class PhoneActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_phone);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PhoneActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PhoneActivity");
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
            goBack();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initView()
	{
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

		TextView phoneTextView = (TextView) findViewById(R.id.phoneTextView);
        phoneTextView.setText(getIntent().getStringExtra("phone"));

        LinearLayout bindPhoneLayout = (LinearLayout) findViewById(R.id.bindPhoneLayout);
        bindPhoneLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
                ViewUtils.goForward(PhoneActivity.this, BindPhoneActivity.class);
			}
		});        
	}

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}