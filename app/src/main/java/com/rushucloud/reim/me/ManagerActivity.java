package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.User;
import classes.adapter.MemberListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.DownloadImageRequest;
import netUtils.request.group.GetGroupRequest;
import netUtils.request.user.DefaultManagerRequest;
import netUtils.response.DownloadImageResponse;
import netUtils.response.group.GetGroupResponse;
import netUtils.response.user.DefaultManagerResponse;

public class ManagerActivity extends Activity
{
    private EditText managerEditText;
    private ImageView avatarImageView;
    private TextView nicknameTextView;
	private ListView managerListView;
	private MemberListViewAdapter adapter;

	private AppPreference appPreference;
	private DBManager dbManager;

	private int currentGroupID;
	private User currentUser;
    private User manager;
	private List<User> userList;
    private List<User> showList = new ArrayList<User>();
    private List<User> chosenList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_manager);
		initView();
        ReimProgressDialog.setContext(this);
        if (PhoneUtils.isNetworkConnected())
        {
            sendGetGroupRequest();
        }
        else
        {
            initData();
            refreshManagerView();
            initListView();
        }
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ManagerActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ManagerActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();

        currentGroupID = appPreference.getCurrentGroupID();
		currentUser = appPreference.getCurrentUser();
        manager = currentUser != null? currentUser.getDefaultManager() : null;

		userList = User.removeUserFromList(dbManager.getGroupUsers(currentGroupID), currentUser.getServerID());
        User.sortByNickname(userList);
        chosenList = currentUser.buildBaseManagerList();
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ManagerActivity.this, "UMENG_MINE_BOSS_SETTING_SAVE");
                hideSoftKeyboard();
				
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(ManagerActivity.this, R.string.error_save_network_unavailable);
				}
				else if (manager == null)
				{
					sendDefaultManagerRequest(-1);			
				}
				else if (manager.getServerID() == currentUser.getServerID())
				{
					ViewUtils.showToast(ManagerActivity.this, R.string.error_self_as_manager);				
				}
				else if (manager.getServerID() == currentUser.getDefaultManagerID())
				{
					finish();
				}
				else
				{
					sendDefaultManagerRequest(manager.getServerID());
				}
			}
		});

        managerEditText = (EditText) findViewById(R.id.managerEditText);
        managerEditText.addTextChangedListener(new TextWatcher()
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

        avatarImageView = (ImageView) findViewById(R.id.avatarImageView);
        nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);

		managerListView = (ListView) findViewById(R.id.userListView);
		managerListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				MobclickAgent.onEvent(ManagerActivity.this, "UMENG_MINE_BOSS_SETTING_CHOOSE");
                hideSoftKeyboard();

                User user = adapter.getItem(position);
                if (!chosenList.contains(user))
                {
                    chosenList.clear();
                    chosenList.add(adapter.getItem(position));
                    manager = user;
                }
                else
                {
                    chosenList.clear();
                    manager = null;
                }
				adapter.setChosenList(chosenList);
				adapter.notifyDataSetChanged();

                refreshManagerView();
			}
		});	
	}

    private void refreshManagerView()
    {
        if (manager != null)
        {
            nicknameTextView.setText(manager.getNickname());
            ViewUtils.setImageViewBitmap(manager, avatarImageView);
        }
        else
        {
            nicknameTextView.setText(R.string.manager_not_set);
            avatarImageView.setImageResource(R.drawable.default_avatar);
        }
    }

	private void initListView()
	{
		adapter = new MemberListViewAdapter(this, userList, chosenList);
		managerListView.setAdapter(adapter);
		
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

	private void sendGetGroupRequest()
	{
		ReimProgressDialog.show();
		GetGroupRequest request = new GetGroupRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetGroupResponse response = new GetGroupResponse(httpResponse);
				if (response.getStatus())
				{
					appPreference = AppPreference.getAppPreference();
					dbManager = DBManager.getDBManager();
					
					currentGroupID = appPreference.getCurrentGroupID();
					currentUser = appPreference.getCurrentUser();
					
					dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);
					dbManager.syncUser(currentUser);

					dbManager.syncGroup(response.getGroup());
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							initData();
                            refreshManagerView();
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
							ViewUtils.showToast(ManagerActivity.this, R.string.failed_to_get_data);
						}
					});
				}
			}
		});
	}
	
	private void sendDefaultManagerRequest(final int newManagerID)
	{
		ReimProgressDialog.show();
		DefaultManagerRequest request = new DefaultManagerRequest(newManagerID);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DefaultManagerResponse response = new DefaultManagerResponse(httpResponse);
				if (response.getStatus())
				{
					currentUser.setDefaultManagerID(newManagerID);
					currentUser.setLocalUpdatedDate(Utils.getCurrentTime());
					currentUser.setServerUpdatedDate(currentUser.getLocalUpdatedDate());
					dbManager.updateUser(currentUser);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ManagerActivity.this, R.string.succeed_in_changing_default_manager);
                            finish();
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
							ViewUtils.showToast(ManagerActivity.this, R.string.failed_to_change_manager, response.getErrorMessage());				
						}
					});
				}
			}
		});
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
							userList = User.removeUserFromList(dbManager.getGroupUsers(currentGroupID), currentUser.getServerID());
                            User.sortByNickname(userList);
                            filterList();
                            if (manager != null && manager.equals(user))
                            {
                                manager = user;
                                refreshManagerView();
                            }
						}
					});	
				}
			}
		});
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(managerEditText.getWindowToken(), 0);
    }
}