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
import classes.widget.ClearEditText;
import classes.widget.PinnedSectionListView;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class PickCCActivity extends Activity
{
    // Widgets
    private ClearEditText ccEditText;
    private MemberListViewAdapter adapter;
    private LinearLayout indexLayout;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
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
        setContentView(R.layout.activity_report_cc);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickCCActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickCCActivity");
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

    @SuppressWarnings("unchecked")
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        senderID = getIntent().getIntExtra("sender", -1);
        newReport = getIntent().getBooleanExtra("newReport", false);
        fromFollowing = getIntent().getBooleanExtra("fromFollowing", false);
        userList = User.removeUserFromList(dbManager.getGroupUsers(appPreference.getCurrentGroupID()), appPreference.getCurrentUserID());
        if (senderID != -1)
        {
            userList = User.removeUserFromList(userList, senderID);
        }

        List<User> ccList = (List<User>) getIntent().getSerializableExtra("ccs");
        chosenList = ccList == null ? new ArrayList<User>() : ccList;
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
                    MobclickAgent.onEvent(PickCCActivity.this, "UMENG_REPORT_NEW_SEND_SUBMIT");
                }
                else if (!fromFollowing)
                {
                    MobclickAgent.onEvent(PickCCActivity.this, "UMENG_REPORT_EDIT_SEND_SUBMIT");
                }
                else
                {
                    MobclickAgent.onEvent(PickCCActivity.this, "UMENG_REPORT_NEXT_CC_SUBMIT");
                }

                Intent intent = new Intent();
                intent.putExtra("ccs", (Serializable) chosenList);
                ViewUtils.goBackWithResult(PickCCActivity.this, intent);
            }
        });

        PinnedSectionListView ccListView = (PinnedSectionListView) findViewById(R.id.ccListView);

        if (userList.isEmpty())
        {
            LinearLayout inviteContainer = (LinearLayout) findViewById(R.id.inviteContainer);
            inviteContainer.setVisibility(View.VISIBLE);

            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    ViewUtils.goForward(PickCCActivity.this, InputContactActivity.class);
                }
            });

            LinearLayout searchContainer = (LinearLayout) findViewById(R.id.searchContainer);
            searchContainer.setVisibility(View.GONE);

            ccListView.setVisibility(View.GONE);
        }
        else
        {
            ccEditText = (ClearEditText) findViewById(R.id.ccEditText);
            ccEditText.addTextChangedListener(new TextWatcher()
            {
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    if (ccEditText.hasFocus())
                    {
                        ccEditText.setClearIconVisible(s.length() > 0);
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
            ccListView.setAdapter(adapter);
            ccListView.setOnItemClickListener(new OnItemClickListener()
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
            ccListView.setOnScrollListener(new AbsListView.OnScrollListener()
            {
                public void onScrollStateChanged(AbsListView absListView, int i)
                {
                    hideSoftKeyboard();
                }

                public void onScroll(AbsListView absListView, int i, int i2, int i3)
                {

                }
            });

            indexLayout = (LinearLayout) this.findViewById(R.id.indexLayout);
            TextView centralTextView = (TextView) findViewById(R.id.centralTextView);

            ViewUtils.initIndexLayout(this, 123, adapter.getSelector(), ccListView, indexLayout, centralTextView);

            for (User user : userList)
            {
                if (user.hasUndownloadedAvatar())
                {
                    sendDownloadAvatarRequest(user);
                }
            }
        }
    }

    private void filterList()
    {
        showList.clear();
        showList.addAll(User.filterList(userList, ccEditText.getText().toString()));
        adapter.setMemberList(showList);
        adapter.notifyDataSetChanged();
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
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            userList = User.removeUserFromList(dbManager.getGroupUsers(appPreference.getCurrentGroupID()), appPreference.getCurrentUserID());
                            if (senderID != -1)
                            {
                                userList = User.removeUserFromList(userList, senderID);
                            }
                            filterList();
                        }
                    });
                }
            }
        });
    }

    private void hideSoftKeyboard()
    {
        if (ccEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ccEditText.getWindowToken(), 0);
        }
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}
