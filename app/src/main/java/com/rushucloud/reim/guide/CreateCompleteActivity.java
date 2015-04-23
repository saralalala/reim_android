package com.rushucloud.reim.guide;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.ViewUtils;

public class CreateCompleteActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_create_complete);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("GuideCompleteActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("GuideCompleteActivity");
		MobclickAgent.onPause(this);
	}

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	private void initView()
	{
		getActionBar().hide();

        TextView companyTextView = (TextView) findViewById(R.id.companyTextView);
        companyTextView.setText(getIntent().getStringExtra("companyName"));

        TextView promptTextView = (TextView) findViewById(R.id.promptTextView);

        int count = getIntent().getIntExtra("count", 0);
        if (count > 0)
        {
            String firstPart = ViewUtils.getString(R.string.sending_invitation_1);
            String secondPart = ViewUtils.getString(R.string.sending_invitation_2);
            String thirdPart = ViewUtils.getString(R.string.invite_by_pc);
            String content = firstPart + count + secondPart + thirdPart;
            SpannableString text = new SpannableString(content);
            int index = content.indexOf(secondPart);
            text.setSpan(new StyleSpan(Typeface.BOLD), 5, index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = content.indexOf(thirdPart);
            text.setSpan(new StyleSpan(Typeface.BOLD), index + 5, index + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new UnderlineSpan(), index + 5, index + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            promptTextView.setText(text);
        }
        else
        {
            String content = ViewUtils.getString(R.string.create_prompt);
            SpannableString text = new SpannableString(content);
            text.setSpan(new StyleSpan(Typeface.BOLD), 4, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new UnderlineSpan(), 4, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), 25, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new UnderlineSpan(), 25, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            promptTextView.setText(text);
        }

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(CreateCompleteActivity.this, MainActivity.class);
            }
        });
	}
}
