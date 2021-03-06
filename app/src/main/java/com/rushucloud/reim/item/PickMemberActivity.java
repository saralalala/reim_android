package com.rushucloud.reim.item;

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
import classes.model.Image;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.PinnedSectionListView;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class PickMemberActivity extends Activity
{
    // Widgets
    private ClearEditText memberEditText;
    private MemberListViewAdapter adapter;
    private LinearLayout indexLayout;

    // Local Data
    private DBManager dbManager;
    private List<User> userList;
    private List<User> showList = new ArrayList<>();
    private List<User> chosenList;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reim_member);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickMemberActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickMemberActivity");
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

                List<User> chosenList = adapter == null ? new ArrayList<User>() : adapter.getChosenList();
                Intent intent = new Intent();
                intent.putExtra("users", (Serializable) chosenList);
                ViewUtils.goBackWithResult(PickMemberActivity.this, intent);
            }
        });

        if (userList.size() == 1)
        {
            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    startActivity(new Intent(PickMemberActivity.this, InputContactActivity.class));
                }
            });
            inviteLayout.setVisibility(View.VISIBLE);

            LinearLayout searchContainer = (LinearLayout) findViewById(R.id.searchContainer);
            searchContainer.setVisibility(View.GONE);
        }

        memberEditText = (ClearEditText) findViewById(R.id.memberEditText);
        memberEditText.addTextChangedListener(new TextWatcher()
        {
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (memberEditText.hasFocus())
                {
                    memberEditText.setClearIconVisible(s.length() > 0);
                }
            }

            public void afterTextChanged(Editable s)
            {
                int visibility = s.toString().isEmpty() ? View.VISIBLE : View.GONE;
                indexLayout.setVisibility(visibility);
                filterList();
            }
        });

        adapter = new MemberListViewAdapter(this, userList, chosenList);

        PinnedSectionListView userListView = (PinnedSectionListView) findViewById(R.id.userListView);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideSoftKeyboard();
                adapter.setCheck(position);
                adapter.notifyDataSetChanged();
            }
        });
        userListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView absListView, int i)
            {
                hideSoftKeyboard();
            }

            public void onScroll(AbsListView absListView, int i, int i2, int i3)
            {

            }
        });

        int topMargin = userList.size() == 1 ? 140 : 123;
        indexLayout = (LinearLayout) findViewById(R.id.indexLayout);
        TextView centralTextView = (TextView) findViewById(R.id.centralTextView);

        ViewUtils.initIndexLayout(this, topMargin, adapter.getSelector(), userListView, indexLayout, centralTextView);

        for (User user : userList)
        {
            if (user.hasUndownloadedAvatar())
            {
                sendDownloadAvatarRequest(user);
            }
        }
    }

    private void hideSoftKeyboard()
    {
        if (memberEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(memberEditText.getWindowToken(), 0);
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
        dbManager = DBManager.getDBManager();

        int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
        userList = dbManager.getGroupUsers(currentGroupID);
        chosenList = (List<User>) getIntent().getSerializableExtra("users");
    }

    private void filterList()
    {
        showList.clear();
        showList.addAll(User.filterList(userList, memberEditText.getText().toString()));
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
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), Image.TYPE_AVATAR, user.getAvatarID());
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
                            userList = dbManager.getGroupUsers(currentGroupID);
                            filterList();
                        }
                    });
                }
            }
        });
    }
}