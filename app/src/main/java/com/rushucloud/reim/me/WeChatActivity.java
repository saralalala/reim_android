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

public class WeChatActivity extends Activity
{
    // Local Data
    private User currentUser;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_wechat);
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("WeChatActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("WeChatActivity");
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
                if (currentUser == null)
                {
                    ViewUtils.showToast(WeChatActivity.this, R.string.failed_to_read_data);
                }
                else if (currentUser.getEmail().isEmpty() && currentUser.getPhone().isEmpty())
                {
                    ViewUtils.showToast(WeChatActivity.this, R.string.prompt_last_certification);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WeChatActivity.this);
                    builder.setTitle(R.string.tip);
                    builder.setMessage(R.string.prompt_unbind);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (PhoneUtils.isNetworkConnected())
                            {
                                sendUnbindRequest();
                            }
                            else
                            {
                                ViewUtils.showToast(WeChatActivity.this, R.string.error_unbind_network_unavailable);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            }
        });

        TextView wechatTextView = (TextView) findViewById(R.id.wechatTextView);
        wechatTextView.setText(getIntent().getStringExtra("wechat"));

        LinearLayout bindWeChatLayout = (LinearLayout) findViewById(R.id.bindWeChatLayout);
        bindWeChatLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (currentUser == null)
                {
                    ViewUtils.showToast(WeChatActivity.this, R.string.failed_to_read_data);
                }
                else if (currentUser.getEmail().isEmpty() && currentUser.getPhone().isEmpty())
                {
                    ViewUtils.showToast(WeChatActivity.this, R.string.prompt_wechat_only);
                }
                else
                {
                    ViewUtils.goForward(WeChatActivity.this, BindWeChatActivity.class);
                }
            }
        });
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Network
    private void sendUnbindRequest()
    {
        ReimProgressDialog.show();
        UnbindRequest request = new UnbindRequest(NetworkConstant.CONTACT_TYPE_WECHAT);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UnbindResponse response = new UnbindResponse(httpResponse);
                if (response.getStatus())
                {
                    currentUser.setWeChat("");
                    DBManager.getDBManager().updateUser(currentUser);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(WeChatActivity.this, R.string.succeed_in_unbinding_wechat);
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
                            ViewUtils.showToast(WeChatActivity.this, R.string.failed_to_unbind_wechat, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}