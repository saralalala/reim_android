package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.group.CreateGroupRequest;
import netUtils.response.group.CreateGroupResponse;

public class WeChatShareActivity extends Activity
{
    private AppPreference appPreference;
    private DBManager dbManager;
    private String companyName;
    private ArrayList<String> inputList = new ArrayList<>();
    private ArrayList<String> inputChosenList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_wechat);
        initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("WeChatShareActivity");
		MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("WeChatShareActivity");
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

        Bundle bundle = getIntent().getExtras();
        companyName = bundle.getString("companyName", "");
        inputList = bundle.getStringArrayList("inputList");
        inputChosenList = bundle.getStringArrayList("inputChosenList");
        contactChosenList = (List<User>) bundle.getSerializable("contactChosenList");
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

        TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String inviteList = "";
                for (String contact : inputChosenList)
                {
                    inviteList += contact + ",";
                }
                for (User user : contactChosenList)
                {
                    inviteList += user.getContact() + ",";
                }
                if (!inviteList.isEmpty())
                {
                    inviteList = inviteList.substring(0, inviteList.length() - 1);
                }

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(WeChatShareActivity.this, R.string.error_create_network_unavailable);
                }
                else
                {
                    sendCreateGroupRequest(inviteList, inputChosenList.size() + contactChosenList.size());
                }
            }
        });

        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                WeChatUtils.shareToWX("https://www.cloudbaoxiao.com", "测试内容Title", "发工资发工资发工资发工资发工资", R.drawable.ic_launcher);
            }
        });

        TextView copyTextView = (TextView) findViewById(R.id.copyTextView);
        copyTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String url = "https://www.cloudbaoxiao.com";
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.app_name), url);
                clipboardManager.setPrimaryClip(clip);
                ViewUtils.showToast(WeChatShareActivity.this, R.string.prompt_copied_to_clipboard);
            }
        });
	}

    private void sendCreateGroupRequest(String inviteList, final int count)
    {
        ReimProgressDialog.show();
        CreateGroupRequest request = new CreateGroupRequest(companyName, inviteList, 1);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateGroupResponse response = new CreateGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Group group = new Group();
                    group.setName(companyName);
                    group.setServerID(response.getGroupID());
                    group.setLocalUpdatedDate(response.getDate());
                    group.setServerUpdatedDate(response.getDate());

                    User currentUser = appPreference.getCurrentUser();
                    currentUser.setGroupID(group.getServerID());
                    currentUser.setIsAdmin(true);

                    dbManager.insertGroup(group);
                    dbManager.updateUser(currentUser);
                    appPreference.setCurrentGroupID(group.getServerID());
                    appPreference.saveAppPreference();

                    int currentGroupID = response.getGroup().getServerID();

                    // update AppPreference
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setCurrentGroupID(currentGroupID);
                    appPreference.saveAppPreference();

                    // update members
                    DBManager dbManager = DBManager.getDBManager();
                    currentUser = response.getCurrentUser();
                    User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                    if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                    {
                        currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                    }

                    dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                    dbManager.syncUser(currentUser);

                    // update categories
                    dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                    // update tags
                    dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(WeChatShareActivity.this, CreateCompleteActivity.class);
                            intent.putExtra("count", count);
                            intent.putExtra("companyName", companyName);
                            ViewUtils.goForwardAndFinish(WeChatShareActivity.this, intent);
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
                            ViewUtils.showToast(WeChatShareActivity.this, R.string.failed_to_create_company, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void goBack()
    {
        Bundle bundle = new Bundle();
        bundle.putString("companyName", companyName);
        bundle.putStringArrayList("inputList", inputList);
        bundle.putStringArrayList("inputChosenList", inputChosenList);
        bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtras(bundle);
        ViewUtils.goBackWithIntent(this, intent);
    }
}