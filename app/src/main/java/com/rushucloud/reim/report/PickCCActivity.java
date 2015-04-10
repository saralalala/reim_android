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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.InviteActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.User;
import classes.adapter.MemberListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.widget.ClearEditText;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.response.DownloadImageResponse;

public class PickCCActivity extends Activity
{
    private ClearEditText ccEditText;
	private MemberListViewAdapter adapter;

	private AppPreference appPreference;
	private DBManager dbManager;
	private List<User> userList;
    private List<User> showList = new ArrayList<User>();
    private List<User> chosenList;
	private int senderID;
	private boolean newReport;
	private boolean fromFollowing;
	
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
			finish();
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
        chosenList = ccList == null? new ArrayList<User>() : ccList;
	}
	
	private void initView()
	{		
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                hideSoftKeyboard();
				finish();
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
				else if (!fromFollowing && !newReport)
				{
					MobclickAgent.onEvent(PickCCActivity.this, "UMENG_REPORT_EDIT_SEND_SUBMIT");					
				}
				else
				{
					MobclickAgent.onEvent(PickCCActivity.this, "UMENG_REPORT_NEXT_CC_SUBMIT");					
				}

				Intent intent = new Intent();
				intent.putExtra("ccs", (Serializable) chosenList);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

        ListView ccListView = (ListView) findViewById(R.id.ccListView);

        if (userList.isEmpty())
        {
            LinearLayout inviteContainer = (LinearLayout) findViewById(R.id.inviteContainer);
            inviteContainer.setVisibility(View.VISIBLE);

            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    startActivity(new Intent(PickCCActivity.this, InviteActivity.class));
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
        String keyWord = ccEditText.getText().toString();

        showList.clear();
        for (User user : userList)
        {
            if (user.getNickname().contains(keyWord) || user.getEmail().contains(keyWord) || user.getPhone().contains(keyWord))
            {
                showList.add(user);
            }
        }

        adapter.setMemberList(showList);
        adapter.notifyDataSetChanged();
    }
	
    private void sendDownloadAvatarRequest(final User user)
    {
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
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
}
