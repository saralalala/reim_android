package com.rushucloud.reim.guide;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;

public class GuideStartActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_start);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("GuideStartActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("GuideStartActivity");
		MobclickAgent.onPause(this);
	}

	private void initView()
	{
		getActionBar().hide();

        Button joinButton = (Button) findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(GuideStartActivity.this, CreateCompanyActivity.class);
            }
        });
		
		TextView createTextView = (TextView) findViewById(R.id.createTextView);
        createTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG|Paint.FAKE_BOLD_TEXT_FLAG);
        createTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {

            }
        });
	}
}
