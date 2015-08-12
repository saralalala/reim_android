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
import com.umeng.analytics.MobclickAgent;

import classes.model.Proxy;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.proxy.CreateProxyRequest;
import netUtils.request.proxy.ModifyProxyRequest;
import netUtils.response.proxy.CreateProxyResponse;
import netUtils.response.proxy.ModifyProxyResponse;

public class ProxyScopeActivity extends Activity
{
    // Widgets
    private ImageView allCheckImageView;
    private ImageView approveCheckImageView;
    private ImageView submitCheckImageView;

    // Local Data
    private Proxy proxy = new Proxy();
    private boolean newProxy = true;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_proxy_scope);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ProxyScopeActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ProxyScopeActivity");
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

        TextView completeTextView = (TextView) findViewById(R.id.completeTextView);
        completeTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!PhoneUtils.isNetworkConnected() && newProxy)
                {
                    ViewUtils.showToast(ProxyScopeActivity.this, R.string.error_create_network_unavailable);
                }
                else if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ProxyScopeActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newProxy)
                {
                    sendCreateProxyRequest();
                }
                else
                {
                    sendModifyProxyRequest();
                }
            }
        });

        allCheckImageView = (ImageView) findViewById(R.id.allCheckImageView);
        approveCheckImageView = (ImageView) findViewById(R.id.approveCheckImageView);
        submitCheckImageView = (ImageView) findViewById(R.id.submitCheckImageView);

        LinearLayout allLayout = (LinearLayout) findViewById(R.id.allLayout);
        allLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                setProxyPermission(Proxy.PERMISSION_ALL);
            }
        });

        LinearLayout approveLayout = (LinearLayout) findViewById(R.id.approveLayout);
        approveLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                setProxyPermission(Proxy.PERMISSION_APPROVE);
            }
        });

        LinearLayout submitLayout = (LinearLayout) findViewById(R.id.submitLayout);
        submitLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                setProxyPermission(Proxy.PERMISSION_SUBMIT);
            }
        });

        setProxyPermission(proxy.getPermission());
    }

    private void setProxyPermission(int proxyPermission)
    {
        switch (proxyPermission)
        {
            case Proxy.PERMISSION_ALL:
            {
                proxy.setPermission(Proxy.PERMISSION_ALL);
                allCheckImageView.setVisibility(View.VISIBLE);
                approveCheckImageView.setVisibility(View.INVISIBLE);
                submitCheckImageView.setVisibility(View.INVISIBLE);
                break;
            }
            case Proxy.PERMISSION_APPROVE:
            {
                proxy.setPermission(Proxy.PERMISSION_APPROVE);
                allCheckImageView.setVisibility(View.INVISIBLE);
                approveCheckImageView.setVisibility(View.VISIBLE);
                submitCheckImageView.setVisibility(View.INVISIBLE);
                break;
            }
            case Proxy.PERMISSION_SUBMIT:
            {
                proxy.setPermission(Proxy.PERMISSION_SUBMIT);
                allCheckImageView.setVisibility(View.INVISIBLE);
                approveCheckImageView.setVisibility(View.INVISIBLE);
                submitCheckImageView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void goBack()
    {
        if (newProxy)
        {
            ViewUtils.goBackWithIntent(this, PickProxyActivity.class);
        }
        else
        {
            ViewUtils.goBackWithIntent(this, ProxyActivity.class);
        }
    }

    // Data
    private void initData()
    {
        proxy = (Proxy) getIntent().getSerializableExtra("proxy");
        newProxy = getIntent().getBooleanExtra("newProxy", true);
    }

    // Network
    private void sendCreateProxyRequest()
    {
        ReimProgressDialog.show();
        CreateProxyRequest request = new CreateProxyRequest(proxy);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateProxyResponse response = new CreateProxyResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ReimProgressDialog.dismiss();
                        if (response.getStatus())
                        {
                            ViewUtils.showToast(ProxyScopeActivity.this, R.string.succeed_in_creating_proxy);
                            ViewUtils.goBackWithIntent(ProxyScopeActivity.this, ProxyActivity.class);
                        }
                        else if (response.getCode() == NetworkConstant.ERROR_PROXY_EXISTS)
                        {
                            ViewUtils.showToast(ProxyScopeActivity.this, R.string.error_network_proxy_exists);
                        }
                        else
                        {
                            ViewUtils.showToast(ProxyScopeActivity.this, R.string.failed_to_create_proxy, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }

    private void sendModifyProxyRequest()
    {
        ModifyProxyRequest request = new ModifyProxyRequest(proxy);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyProxyResponse response = new ModifyProxyResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (response.getStatus())
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ProxyScopeActivity.this, R.string.succeed_in_modifying_proxy);
                            ViewUtils.goBackWithIntent(ProxyScopeActivity.this, ProxyActivity.class);
                        }
                        else
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ProxyScopeActivity.this, R.string.failed_to_modify_proxy, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }
}
