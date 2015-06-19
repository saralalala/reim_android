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
import com.rushucloud.reim.item.DidiExpenseActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class ImportActivity extends Activity
{
    // Widgets
    private TextView didiTextView;

    // Local Data
    private User currentUser;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_import);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ImportActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ImportActivity");
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

        didiTextView = (TextView) findViewById(R.id.didiTextView);

        LinearLayout didiLayout = (LinearLayout) findViewById(R.id.didiLayout);
        didiLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!currentUser.getDidi().isEmpty())
                {
                    ViewUtils.goForward(ImportActivity.this, DidiExpenseActivity.class);
                }
                else
                {
                    ViewUtils.goForward(ImportActivity.this, BindDidiActivity.class);
                }
            }
        });
    }

    private void refreshView()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        if (!currentUser.getDidi().isEmpty())
        {
            didiTextView.setText(currentUser.getDidi());
        }
        else
        {
            didiTextView.setText(R.string.not_binding);
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}