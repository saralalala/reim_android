package com.rushucloud.reim.guide;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.main.MainActivity;
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
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        TextView companyTextView = (TextView) findViewById(R.id.companyTextView);
        companyTextView.setText(getIntent().getStringExtra("companyName"));

        TextView promptTextView = (TextView) findViewById(R.id.promptTextView);

        int count = getIntent().getIntExtra("count", 0);
        if (count > 0)
        {
            String content = String.format(ViewUtils.getString(R.string.invitation_sent), count);
            int length = Integer.toString(count).length();

            SpannableString text = new SpannableString(content);
            ViewUtils.setTextBold(text, 6, 6 + length);
            ViewUtils.setTextBoldAndUnderlined(text, 44 + length, 47 + length);
            promptTextView.setText(text);
        }
        else
        {
            SpannableString text = new SpannableString(ViewUtils.getString(R.string.create_prompt));
            ViewUtils.setTextBoldAndUnderlined(text, 38, 41);
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
