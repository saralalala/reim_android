package com.rushucloud.reim.report;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.InputContactActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.MemberListViewAdapter;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ClearEditText;
import classes.widget.PinnedSectionListView;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class PickManagerActivity extends Activity
{
    // Widgets
    private ClearEditText managerEditText;
    private RelativeLayout managerLayout;
    private CircleImageView avatarImageView;
    private TextView nicknameTextView;
    private MemberListViewAdapter adapter;
    private LinearLayout indexLayout;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
    private User currentUser;
    private User defaultManager;
    private List<User> userList;
    private List<User> showList = new ArrayList<>();
    private List<User> chosenList;
    private int senderID;
    private boolean newReport;
    private boolean fromFollowing;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_manager);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickManagerActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickManagerActivity");
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                if (!fromFollowing && newReport)
                {
                    MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_NEW_SEND_SUBMIT");
                }
                else if (!fromFollowing)
                {
                    MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_EDIT_SEND_SUBMIT");
                }
                else
                {
                    MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_NEXT_SEND_SUBMIT");
                }

                Intent intent = new Intent();
                intent.putExtra("managers", (Serializable) chosenList);
                ViewUtils.goBackWithResult(PickManagerActivity.this, intent);
            }
        });

        PinnedSectionListView managerListView = (PinnedSectionListView) findViewById(R.id.managerListView);

        if (userList.isEmpty())
        {
            LinearLayout inviteContainer = (LinearLayout) findViewById(R.id.inviteContainer);
            inviteContainer.setVisibility(View.VISIBLE);

            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    ViewUtils.goForward(PickManagerActivity.this, InputContactActivity.class);
                }
            });

            RelativeLayout searchContainer = (RelativeLayout) findViewById(R.id.searchContainer);
            searchContainer.setVisibility(View.GONE);

            LinearLayout managerContainer = (LinearLayout) findViewById(R.id.managerContainer);
            managerContainer.setVisibility(View.GONE);

            managerListView.setVisibility(View.GONE);
        }
        else
        {
            managerEditText = (ClearEditText) findViewById(R.id.managerEditText);
            managerEditText.addTextChangedListener(new TextWatcher()
            {
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    if (managerEditText.hasFocus())
                    {
                        managerEditText.setClearIconVisible(s.length() > 0);
                    }
                }

                public void afterTextChanged(Editable s)
                {
                    int visibility = s.toString().isEmpty() ? View.VISIBLE : View.GONE;
                    indexLayout.setVisibility(visibility);
                    filterList();
                }
            });

            if (defaultManager == null || senderID == currentUser.getDefaultManagerID())
            {
                LinearLayout managerContainer = (LinearLayout) findViewById(R.id.managerContainer);
                managerContainer.setVisibility(View.GONE);
            }
            else
            {
                managerLayout = (RelativeLayout) findViewById(R.id.managerLayout);
                managerLayout.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        if (chosenList.contains(defaultManager))
                        {
                            chosenList.remove(defaultManager);
                        }
                        else
                        {
                            chosenList.add(defaultManager);
                        }
                        adapter.setChosenList(chosenList);
                        refreshManagerView();
                    }
                });

                avatarImageView = (CircleImageView) findViewById(R.id.avatarImageView);
                nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);

                nicknameTextView.setText(defaultManager.getNickname());
                refreshManagerView();

                if (defaultManager.hasUndownloadedAvatar())
                {
                    sendDownloadAvatarRequest(defaultManager);
                }
                else
                {
                    ViewUtils.setImageViewBitmap(defaultManager, avatarImageView);
                }
            }

            adapter = new MemberListViewAdapter(this, userList, chosenList);
            managerListView.setAdapter(adapter);
            managerListView.setOnItemClickListener(new OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    hideSoftKeyboard();
                    adapter.setCheck(position);
                    adapter.notifyDataSetChanged();
                    chosenList.clear();
                    chosenList.addAll(adapter.getChosenList());
                }
            });
            managerListView.setOnScrollListener(new AbsListView.OnScrollListener()
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

            ViewUtils.initIndexLayout(this, 123, adapter.getSelector(), managerListView, indexLayout, centralTextView);

            for (User user : userList)
            {
                if (user.hasUndownloadedAvatar())
                {
                    sendDownloadAvatarRequest(user);
                }
            }
        }
    }

    private void refreshManagerView()
    {
        defaultManager = currentUser.getDefaultManager();
        if (defaultManager != null && chosenList.contains(defaultManager))
        {
            managerLayout.setBackgroundResource(R.color.list_item_pressed);
            nicknameTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));
        }
        else if (defaultManager != null)
        {
            managerLayout.setBackgroundResource(R.color.list_item_unpressed);
            nicknameTextView.setTextColor(ViewUtils.getColor(R.color.font_major_dark));
        }
    }

    private void hideSoftKeyboard()
    {
        if (managerEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(managerEditText.getWindowToken(), 0);
        }
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Data
    @SuppressWarnings("unchecked")
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        senderID = getIntent().getIntExtra("sender", -1);
        newReport = getIntent().getBooleanExtra("newReport", false);
        fromFollowing = getIntent().getBooleanExtra("fromFollowing", false);

        List<User> managerList = (List<User>) getIntent().getSerializableExtra("managers");
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        defaultManager = currentUser.getDefaultManager();
        if (managerList != null)
        {
            chosenList = managerList;
        }
        else if (senderID == currentUser.getDefaultManagerID())
        {
            chosenList = new ArrayList<>();
        }
        else
        {
            chosenList = currentUser.buildBaseManagerList();
        }

        buildUserList();
    }

    private void buildUserList()
    {
        userList = User.removeUserFromList(dbManager.getGroupUsers(appPreference.getCurrentGroupID()), appPreference.getCurrentUserID());
        if (senderID != -1)
        {
            userList = User.removeUserFromList(userList, senderID);
        }
        if (defaultManager != null)
        {
            userList = User.removeUserFromList(userList, defaultManager.getServerID());
        }
        User.sortByNickname(userList);
    }

    private void filterList()
    {
        showList.clear();
        showList.addAll(User.filterList(userList, managerEditText.getText().toString()));
        adapter.setMemberList(showList);
        adapter.notifyDataSetChanged();
    }

    // Network
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
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR, user.getAvatarID());
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (defaultManager != null && defaultManager.equals(user))
                            {
                                ViewUtils.setImageViewBitmap(user, avatarImageView);
                            }
                            else
                            {
                                buildUserList();
                                filterList();
                            }
                        }
                    });
                }
            }
        });
    }
}