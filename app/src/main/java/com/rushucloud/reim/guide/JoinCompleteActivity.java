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

public class JoinCompleteActivity extends Activity
{
    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_join_complete);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("JoinCompleteActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("JoinCompleteActivity");
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

        SpannableString text = new SpannableString(ViewUtils.getString(R.string.prompt_join));
        ViewUtils.setTextBoldAndUnderlined(text, 36, 38);

        TextView contentTextView = (TextView) findViewById(R.id.contentTextView);
        contentTextView.setText(text);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(JoinCompleteActivity.this, MainActivity.class);
            }
        });
    }
}