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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
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
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.response.DownloadImageResponse;

public class PickMemberActivity extends Activity
{
    private EditText memberEditText;
	private MemberListViewAdapter adapter;

	private DBManager dbManager;
	private List<User> userList;
    private List<User> showList = new ArrayList<User>();
    private List<User> chosenList;
	
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
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressWarnings("unchecked")
	private void initData()
	{
		dbManager = DBManager.getDBManager();
		
		int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
		userList = dbManager.getGroupUsers(currentGroupID);
		chosenList = (List<User>) getIntent().getSerializableExtra("users");
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

				Intent intent = new Intent();
				intent.putExtra("users", (Serializable) adapter.getChosenList());
				setResult(RESULT_OK, intent);
				finish();
			}
		});

        if (userList.size() == 1)
        {
            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    startActivity(new Intent(PickMemberActivity.this, InviteActivity.class));
                }
            });
            inviteLayout.setVisibility(View.VISIBLE);

            LinearLayout searchContainer = (LinearLayout) findViewById(R.id.searchContainer);
            searchContainer.setVisibility(View.GONE);
        }
        else
        {
            memberEditText = (EditText) findViewById(R.id.memberEditText);
            memberEditText.addTextChangedListener(new TextWatcher()
            {
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count)
                {

                }

                public void afterTextChanged(Editable s)
                {
                    filterList();
                }
            });
        }

        adapter = new MemberListViewAdapter(this, userList, chosenList);

        ListView userListView = (ListView) findViewById(R.id.userListView);
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

        for (User user : userList)
        {
            if (user.hasUndownloadedAvatar())
            {
                sendDownloadAvatarRequest(user);
            }
        }
	}

    private void filterList()
    {
        String keyWord = memberEditText.getText().toString();

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
					user.setAvatarPath(avatarPath);
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

    private void hideSoftKeyboard()
    {
        if (memberEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(memberEditText.getWindowToken(), 0);
        }
    }
}
