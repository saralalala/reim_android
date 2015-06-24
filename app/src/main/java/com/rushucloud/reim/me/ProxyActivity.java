package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.ProxyListViewAdapter;
import classes.model.Proxy;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.proxy.DeleteProxyRequest;
import netUtils.request.proxy.GetProxiesRequest;
import netUtils.response.proxy.DeleteProxyResponse;
import netUtils.response.proxy.GetProxiesResponse;

public class ProxyActivity extends Activity
{
    // Widgets
    private TextView proxyTextView;
    private ProxyListViewAdapter adapter;

    // Local Data
    private List<Proxy> proxyList = new ArrayList<>();
    private int proxyIndex;
    private PopupWindow deletePopupWindow;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_proxy);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ProxyActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (PhoneUtils.isNetworkConnected())
        {
            sendGetProxiesRequest();
        }
        else
        {
            ViewUtils.showToast(this, R.string.failed_to_get_data);
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ProxyActivity");
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

        TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(ProxyActivity.this, PickProxyActivity.class);
            }
        });

        proxyTextView = (TextView) findViewById(R.id.proxyTextView);

        adapter = new ProxyListViewAdapter(this, proxyList, null);
        ListView proxyListView = (ListView) findViewById(R.id.proxyListView);
        proxyListView.setAdapter(adapter);
        proxyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (deletePopupWindow == null || !deletePopupWindow.isShowing())
                {
                    Intent intent = new Intent(ProxyActivity.this, ProxyScopeActivity.class);
                    intent.putExtra("proxy", proxyList.get(i));
                    intent.putExtra("newProxy", false);
                    ViewUtils.goForwardAndFinish(ProxyActivity.this, intent);
                }
            }
        });
        proxyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                proxyIndex = i;
                showDeleteWindow();
                return false;
            }
        });

        initDeleteWindow();

        refreshView();
    }

    private void initDeleteWindow()
    {
        View deleteView = View.inflate(this, R.layout.window_delete, null);

        Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                deletePopupWindow.dismiss();
                sendDeleteProxyRequest();
            }
        });

        Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                deletePopupWindow.dismiss();
            }
        });

        deletePopupWindow = ViewUtils.buildBottomPopupWindow(ProxyActivity.this, deleteView);
    }

    private void showDeleteWindow()
    {
        deletePopupWindow.showAtLocation(ProxyActivity.this.findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        deletePopupWindow.update();

        ViewUtils.dimBackground(ProxyActivity.this);
    }

    private void refreshView()
    {
        if (proxyList.isEmpty())
        {
            proxyTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            proxyTextView.setVisibility(View.GONE);
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Network
    private void sendGetProxiesRequest()
    {
        ReimProgressDialog.show();
        GetProxiesRequest request = new GetProxiesRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetProxiesResponse response = new GetProxiesResponse(httpResponse);
                if (response.getStatus())
                {
                    proxyList.clear();
                    proxyList.addAll(response.getProxyList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            adapter.setProxyList(proxyList);
                            adapter.notifyDataSetChanged();
                            refreshView();
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
                            ViewUtils.showToast(ProxyActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendDeleteProxyRequest()
    {
        ReimProgressDialog.show();
        DeleteProxyRequest request = new DeleteProxyRequest(proxyList.get(proxyIndex).getUser().getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DeleteProxyResponse response = new DeleteProxyResponse(httpResponse);
                if (response.getStatus())
                {
                    proxyList.clear();
                    proxyList.addAll(response.getProxyList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ProxyActivity.this, R.string.succeed_in_deleting_proxy);
                            adapter.setProxyList(proxyList);
                            adapter.notifyDataSetChanged();
                            refreshView();
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
                            ViewUtils.showToast(ProxyActivity.this, R.string.failed_to_delete_proxy, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}