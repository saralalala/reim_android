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

public class PhoneActivity extends Activity
{
    // View
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

        TextView unbindTextView = (TextView) findViewById(R.id.unbindTextView);
        unbindTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                final User currentUser = AppPreference.getAppPreference().getCurrentUser();
                if (currentUser == null)
                {
                    ViewUtils.showToast(PhoneActivity.this, R.string.failed_to_read_data);
                }
                else if (currentUser.getEmail().isEmpty() && currentUser.getWeChat().isEmpty())
                {
                    ViewUtils.showToast(PhoneActivity.this, R.string.prompt_last_certification);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PhoneActivity.this);
                    builder.setTitle(R.string.tip);
                    builder.setMessage(R.string.prompt_unbind);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (PhoneUtils.isNetworkConnected())
                            {
                                sendUnbindRequest(currentUser);
                            }
                            else
                            {
                                ViewUtils.showToast(PhoneActivity.this, R.string.error_unbind_network_unavailable);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
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

    // Network
    private void sendUnbindRequest(final User user)
    {
        ReimProgressDialog.show();
        UnbindRequest request = new UnbindRequest(NetworkConstant.CONTACT_TYPE_PHONE);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UnbindResponse response = new UnbindResponse(httpResponse);
                if (response.getStatus())
                {
                    user.setPhone("");
                    DBManager.getDBManager().updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(PhoneActivity.this, R.string.succeed_in_unbinding_phone);
                            goBack();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(PhoneActivity.this, R.string.failed_to_unbind_phone, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}