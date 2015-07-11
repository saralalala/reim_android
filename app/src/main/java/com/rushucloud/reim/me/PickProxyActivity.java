package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.MemberListViewAdapter;
import classes.model.Image;
import classes.model.Proxy;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.PinnedSectionListView;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.DownloadImageRequest;
import netUtils.request.group.GetGroupRequest;
import netUtils.response.common.DownloadImageResponse;
import netUtils.response.group.GetGroupResponse;

public class PickProxyActivity extends Activity
{
    // Widgets
    private ClearEditText proxyEditText;
    private TextView noMemberTextView;
    private PinnedSectionListView proxyListView;
    private MemberListViewAdapter adapter;
    private LinearLayout indexLayout;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
    private List<User> userList = new ArrayList<>();
    private List<User> showList = new ArrayList<>();
    private List<User> chosenList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_pick_proxy);
        ReimProgressDialog.setContext(this);
        initView();
        if (PhoneUtils.isNetworkConnected())
        {
            sendGetGroupRequest();
        }
        else
        {
            initData();
            initListView();
        }
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickProxyActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickProxyActivity");
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
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                if (chosenList.isEmpty())
                {
                    ViewUtils.showToast(PickProxyActivity.this, R.string.prompt_choose_proxy);
                }
                else
                {
                    Proxy proxy = new Proxy();
                    proxy.setUser(chosenList.get(0));
                    Intent intent = new Intent(PickProxyActivity.this, ProxyScopeActivity.class);
                    intent.putExtra("proxy", proxy);
                    ViewUtils.goForwardAndFinish(PickProxyActivity.this, intent);
                }
            }
        });

        proxyEditText = (ClearEditText) findViewById(R.id.proxyEditText);
        proxyEditText.addTextChangedListener(new TextWatcher()
        {
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (proxyEditText.hasFocus())
                {
                    proxyEditText.setClearIconVisible(s.length() > 0);
                }
            }

            public void afterTextChanged(Editable s)
            {
                int visibility = s.toString().isEmpty() ? View.VISIBLE : View.GONE;
                indexLayout.setVisibility(visibility);
                filterList();
            }
        });

        noMemberTextView = (TextView) findViewById(R.id.noMemberTextView);

        proxyListView = (PinnedSectionListView) findViewById(R.id.proxyListView);
    }

    private void initListView()
    {
        if (userList.isEmpty())
        {
            noMemberTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            noMemberTextView.setVisibility(View.GONE);

            adapter = new MemberListViewAdapter(this, userList, chosenList);
            proxyListView.setAdapter(adapter);
            proxyListView.setOnItemClickListener(new OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    hideSoftKeyboard();
                    chosenList.clear();
                    chosenList.add(adapter.getItem(position));
                    adapter.setChosenList(chosenList);
                    adapter.notifyDataSetChanged();
                }
            });
            proxyListView.setOnScrollListener(new AbsListView.OnScrollListener()
            {
                public void onScrollStateChanged(AbsListView absListView, int i)
                {
                    hideSoftKeyboard();
                }

                public void onScroll(AbsListView absListView, int i, int i2, int i3)
                {

                }
            });

            indexLayout = (LinearLayout) findViewById(R.id.indexLayout);
            TextView centralTextView = (TextView) findViewById(R.id.centralTextView);

            ViewUtils.initIndexLayout(this, 123, adapter.getSelector(), proxyListView, indexLayout, centralTextView);

            if (PhoneUtils.isNetworkConnected())
            {
                for (User user : userList)
                {
                    if (user.hasUndownloadedAvatar())
                    {
                        sendDownloadAvatarRequest(user);
                    }
                }
            }
        }
    }

    private void hideSoftKeyboard()
    {
        if (proxyEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(proxyEditText.getWindowToken(), 0);
        }
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBackWithIntent(this, ProxyActivity.class);
    }

    // Data
    @SuppressWarnings("unchecked")
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        userList.clear();
        userList.addAll(User.removeUserFromList(dbManager.getGroupUsers(appPreference.getCurrentGroupID()), appPreference.getCurrentUserID()));
    }

    private void filterList()
    {
        showList.clear();
        showList.addAll(User.filterList(userList, proxyEditText.getText().toString()));
        adapter.setMemberList(showList);
        adapter.notifyDataSetChanged();
    }

    // Network
    private void sendGetGroupRequest()
    {
        ReimProgressDialog.show();
        GetGroupRequest request = new GetGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetGroupResponse response = new GetGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    appPreference = AppPreference.getAppPreference();
                    dbManager = DBManager.getDBManager();

                    int currentGroupID = appPreference.getCurrentGroupID();
                    User currentUser = appPreference.getCurrentUser();

                    dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);
                    dbManager.updateUser(currentUser);

                    dbManager.syncGroup(response.getGroup());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            initData();
                            initListView();
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
                            ViewUtils.showToast(PickProxyActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendDownloadAvatarRequest(final User user)
    {
        DownloadImageRequest request = new DownloadImageRequest(user.getAvatarServerPath());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), Image.TYPE_AVATAR, user.getAvatarID());
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            userList = User.removeUserFromList(dbManager.getGroupUsers(appPreference.getCurrentGroupID()), appPreference.getCurrentUserID());
                            filterList();
                        }
                    });
                }
            }
        });
    }
}