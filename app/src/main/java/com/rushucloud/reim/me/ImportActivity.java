package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import classes.model.DidiExpense;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.user.UnbindRequest;
import netUtils.response.user.UnbindResponse;

public class ImportActivity extends Activity
{
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

        final TextView didiTextView = (TextView) findViewById(R.id.didiTextView);
        didiTextView.setText("13811891565");

        LinearLayout didiLayout = (LinearLayout) findViewById(R.id.didiLayout);
        didiLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (didiTextView.getText().equals("13811891565"))
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

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}
