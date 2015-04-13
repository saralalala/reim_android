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
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.response.DownloadImageResponse;

public class PickManagerActivity extends Activity
{
    private ClearEditText managerEditText;
    private RelativeLayout managerLayout;
    private ImageView avatarImageView;
    private TextView nicknameTextView;
	private MemberListViewAdapter adapter;

	private AppPreference appPreference;
	private DBManager dbManager;
    private User currentUser;
    private User defaultManager;
	private List<User> userList;
    private List<User> showList = new ArrayList<User>();
    private List<User> chosenList;
	private int senderID;
	private boolean newReport;
	private boolean fromFollowing;
	
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

		List<User> managerList = (List<User>) getIntent().getSerializableExtra("managers");
		currentUser = AppPreference.getAppPreference().getCurrentUser();
        defaultManager = currentUser.getDefaultManager();
        chosenList = managerList == null? currentUser.buildBaseManagerList() : managerList;

        buildUserList();
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
					MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_NEW_SEND_SUBMIT");
				}
				else if (!fromFollowing && !newReport)
				{
					MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_EDIT_SEND_SUBMIT");					
				}
				else
				{
					MobclickAgent.onEvent(PickManagerActivity.this, "UMENG_REPORT_NEXT_SEND_SUBMIT");					
				}
				
				Intent intent = new Intent();
				intent.putExtra("managers", (Serializable) chosenList);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

        if (userList.isEmpty())
        {
            LinearLayout inviteContainer = (LinearLayout) findViewById(R.id.inviteContainer);
            inviteContainer.setVisibility(View.VISIBLE);

            RelativeLayout inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
            inviteLayout.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    startActivity(new Intent(PickManagerActivity.this, InviteActivity.class));
                }
            });

            RelativeLayout searchContainer = (RelativeLayout) findViewById(R.id.searchContainer);
            searchContainer.setVisibility(View.GONE);

            LinearLayout managerContainer = (LinearLayout) findViewById(R.id.managerContainer);
            managerContainer.setVisibility(View.GONE);

            LinearLayout colleagueContainer = (LinearLayout) findViewById(R.id.colleagueContainer);
            colleagueContainer.setVisibility(View.GONE);
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
                    filterList();
                }
            });

            if (defaultManager == null)
            {
                LinearLayout managerContainer = (LinearLayout) findViewById(R.id.managerContainer);
                managerContainer.setVisibility(View.GONE);

                TextView colleagueTextView = (TextView) findViewById(R.id.colleagueTextView);
                colleagueTextView.setVisibility(View.GONE);
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

                avatarImageView = (ImageView) findViewById(R.id.avatarImageView);
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
            ListView managerListView = (ListView) findViewById(R.id.managerListView);
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
            managerLayout.setBackgroundResource(R.color.list_item_selected);
            nicknameTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));
        }
        else if (defaultManager != null)
        {
            managerLayout.setBackgroundResource(R.color.list_item_unselected);
            nicknameTextView.setTextColor(ViewUtils.getColor(R.color.font_major_dark));
        }
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
        String keyWord = managerEditText.getText().toString();

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
					String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
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

    private void hideSoftKeyboard()
    {
        if (managerEditText != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(managerEditText.getWindowToken(), 0);
        }
    }
}
