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
import netUtils.request.user.ResendRequest;
import netUtils.request.user.UnbindRequest;
import netUtils.response.user.ResendResponse;
import netUtils.response.user.UnbindResponse;

public class EmailActivity extends Activity
{
    // Widgets
    private LinearLayout activeLayout;
    private TextView emailTextView;

    // Local Data
    private int waitingTime = 60;
    private Thread thread;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_email);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("EmailActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("EmailActivity");
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
                    ViewUtils.showToast(EmailActivity.this, R.string.failed_to_read_data);
                }
                else if (currentUser.getPhone().isEmpty() && currentUser.getWeChat().isEmpty())
                {
                    ViewUtils.showToast(EmailActivity.this, R.string.prompt_last_certification);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EmailActivity.this);
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
                                ViewUtils.showToast(EmailActivity.this, R.string.error_unbind_network_unavailable);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            }
        });

        emailTextView = (TextView) findViewById(R.id.emailTextView);
        emailTextView.setText(getIntent().getStringExtra("email"));

        activeLayout = (LinearLayout) findViewById(R.id.activeLayout);
        activeLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (waitingTime != 60)
                {
                    ViewUtils.showToast(EmailActivity.this, String.format(getString(R.string.prompt_resend_active_email_wait), waitingTime));
                }
                else
                {
                    thread = new Thread(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                while (waitingTime > 0)
                                {
                                    java.lang.Thread.sleep(1000);
                                    waitingTime -= 1;
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                waitingTime = 60;
                            }
                        }
                    });
                    thread.start();

                    sendActiveRequest();
                }
            }
        });

        LinearLayout bindEmailLayout = (LinearLayout) findViewById(R.id.bindEmailLayout);
        bindEmailLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(EmailActivity.this, BindEmailActivity.class);
            }
        });
    }

    private void refreshView()
    {
        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        int visibility = currentUser.isActive() ? View.GONE : View.VISIBLE;
        activeLayout.setVisibility(visibility);
        emailTextView.setText(currentUser.getEmail());
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Network
    private void sendUnbindRequest(final User user)
    {
        ReimProgressDialog.show();
        UnbindRequest request = new UnbindRequest(NetworkConstant.CONTACT_TYPE_EMAIL);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UnbindResponse response = new UnbindResponse(httpResponse);
                if (response.getStatus())
                {
                    user.setEmail("");
                    user.setIsActive(false);
                    DBManager.getDBManager().updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EmailActivity.this, R.string.succeed_in_unbinding_email);
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
                            ViewUtils.showToast(EmailActivity.this, R.string.failed_to_unbind_email, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendActiveRequest()
    {
        ResendRequest request = new ResendRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ResendResponse response = new ResendResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (response.getStatus())
                        {
                            ViewUtils.showToast(EmailActivity.this, R.string.succeed_in_resending_active_email);
                        }
                        else
                        {
                            ViewUtils.showToast(EmailActivity.this, R.string.failed_to_resend_active_email);
                            thread.interrupt();
                        }
                    }
                });
            }
        });
    }
}
