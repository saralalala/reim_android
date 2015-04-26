package com.rushucloud.reim.guide;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;

public class JoinedActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_joined);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("JoinedActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("JoinedActivity");
		MobclickAgent.onPause(this);
	}

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }

	private void initView()
	{
        TextView companyTextView = (TextView) findViewById(R.id.companyTextView);
        companyTextView.setText(getIntent().getStringExtra("companyName"));

        SpannableString text = new SpannableString(ViewUtils.getString(R.string.prompt_joined));
        text.setSpan(new StyleSpan(Typeface.BOLD), 18, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_light)), 18, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new StyleSpan(Typeface.BOLD), 24, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_light)), 24, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new StyleSpan(Typeface.BOLD), 33, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_light)), 33, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView contentTextView = (TextView) findViewById(R.id.contentTextView);
        contentTextView.setText(text);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(JoinedActivity.this, MainActivity.class);
            }
        });
	}
}