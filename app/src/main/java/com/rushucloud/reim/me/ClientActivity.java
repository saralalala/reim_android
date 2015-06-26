package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.ProxyListViewAdapter;
import classes.model.Proxy;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.CommonRequest;
import netUtils.request.proxy.GetClientsRequest;
import netUtils.response.common.CommonResponse;
import netUtils.response.proxy.GetClientsResponse;

public class ClientActivity extends Activity
{
    // Widgets
    private TextView clientTextView;
    private ProxyListViewAdapter adapter;

    // Local Data
    private AppPreference appPreference;
    private List<Proxy> clientList = new ArrayList<>();
    private List<Proxy> chosenList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_client);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ClientActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (PhoneUtils.isNetworkConnected())
        {
            sendGetClientsRequest();
        }
        else
        {
            ViewUtils.showToast(this, R.string.failed_to_get_data);
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ClientActivity");
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (chosenList.isEmpty())
                {
                    ViewUtils.showToast(ClientActivity.this, R.string.prompt_choose_client);
                }
                else
                {
                    Proxy proxy = chosenList.get(0);
                    appPreference.setProxyUserID(appPreference.getCurrentUserID()); // proxyUserID is id of actual current user
                    appPreference.setProxyPermission(proxy.getPermission()); // set permission
                    appPreference.setCurrentUserID(proxy.getUser().getServerID()); // currentUserID changes to client user id
                    sendCommonRequest();
                }
            }
        });

        clientTextView = (TextView) findViewById(R.id.clientTextView);

        adapter = new ProxyListViewAdapter(this, clientList, chosenList);
        ListView clientListView = (ListView) findViewById(R.id.clientListView);
        clientListView.setAdapter(adapter);
        clientListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                chosenList.clear();
                chosenList.add(clientList.get(i));
                adapter.setChosenList(chosenList);
                adapter.notifyDataSetChanged();
            }
        });

        refreshView();
    }

    private void refreshView()
    {
        if (clientList.isEmpty())
        {
            clientTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            clientTextView.setVisibility(View.GONE);
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
    }

    // Network
    private void sendGetClientsRequest()
    {
        ReimProgressDialog.show();
        GetClientsRequest request = new GetClientsRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetClientsResponse response = new GetClientsResponse(httpResponse);
                if (response.getStatus())
                {
                    clientList.clear();
                    clientList.addAll(response.getProxyList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            adapter.setProxyList(clientList);
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
                            ViewUtils.showToast(ClientActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendCommonRequest()
    {
        ReimProgressDialog.show();
        CommonRequest request = new CommonRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CommonResponse response = new CommonResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    appPreference.setServerToken(response.getServerToken());

                    if (response.getGroup() != null)
                    {
                        currentGroupID = response.getGroup().getServerID();

                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update members
                        User currentUser = response.getCurrentUser();
                        User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                        if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                        {
                            currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                        }

                        dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                        dbManager.updateUser(currentUser);

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                        // update tags
                        dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                        // update group info
                        dbManager.syncGroup(response.getGroup());
                    }
                    else
                    {
                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update current user
                        dbManager.syncUser(response.getCurrentUser());

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ClientActivity.this, R.string.succeed_in_switch_identity);
                            ViewUtils.goBack(ClientActivity.this);
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
                            appPreference.setCurrentUserID(appPreference.getProxyUserID());
                            appPreference.setProxyUserID(-1);
                            appPreference.setProxyPermission(-1);
                            appPreference.saveAppPreference();
                            ViewUtils.showToast(ClientActivity.this, R.string.failed_to_switch_identity, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}