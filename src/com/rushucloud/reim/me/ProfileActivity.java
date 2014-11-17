package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ModifyUserRequest;
import netUtils.Response.User.ModifyUserResponse;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import classes.Adapter.ProfileListViewAdapater;
import database.DBManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ProfileActivity extends Activity
{
	private ListView profileListView;
	private ProfileListViewAdapater adapter;

	private User currentUser;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ProfileActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ProfileActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_save_item)
		{
			if (Utils.isNetworkConnected())
			{
				saveUserInfo();				
			}
			else
			{
				Toast.makeText(ProfileActivity.this, "网络未连接，无法保存用户信息", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initData()
	{
		currentUser = AppPreference.getAppPreference().getCurrentUser();
	}
	
	private void initView()
	{		
		ReimApplication.setProgressDialog(this);

		adapter = new ProfileListViewAdapater(ProfileActivity.this, currentUser);
		profileListView = (ListView)findViewById(R.id.profileListView);
		profileListView.setAdapter(adapter);
		profileListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 4:
						startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
						break;
					case 5:
					{
						if (currentUser.getGroupID() == -1)
						{
							Toast.makeText(ProfileActivity.this, "你还没加入任何组", Toast.LENGTH_SHORT).show();			
						}
						else
						{
							startActivity(new Intent(ProfileActivity.this, ManagerActivity.class));
						}
						break;
					}
					case 6:
						MobclickAgent.onEvent(ProfileActivity.this, "UMENG_MINE_CATEGORT_SETTING");
						startActivity(new Intent(ProfileActivity.this, CategoryActivity.class));
						break;
					case 7:
						MobclickAgent.onEvent(ProfileActivity.this, "UMENG_MINE_TAG_SETTING");
						startActivity(new Intent(ProfileActivity.this, TagActivity.class));
						break;
					default:
						break;
				}
			}
		});
	}
	
	private void saveUserInfo()
	{
		EditText emailEditText = (EditText)profileListView.getChildAt(0).findViewById(R.id.editText);
		EditText phoneEditText = (EditText)profileListView.getChildAt(1).findViewById(R.id.editText);
		EditText nicknameEditText = (EditText)profileListView.getChildAt(2).findViewById(R.id.editText);
		EditText companyEditText = (EditText)profileListView.getChildAt(3).findViewById(R.id.editText);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);  			
		imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);  			
		imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);  
		
		String email = emailEditText.getText().toString();
		String phone = phoneEditText.getText().toString();;
		String nickname = nicknameEditText.getText().toString();;
		
		if (email.equals("") && phone.equals(""))
		{
			Toast.makeText(this, "邮箱和手机号不能同时为空", Toast.LENGTH_SHORT).show();
		}
		else
		{
			ReimApplication.showProgressDialog();
			final DBManager dbManager = DBManager.getDBManager();
			currentUser.setEmail(email);
			currentUser.setPhone(phone);
			currentUser.setNickname(nickname);
			
			ModifyUserRequest request = new ModifyUserRequest(currentUser);
			request.sendRequest(new HttpConnectionCallback()
			{
				public void execute(Object httpResponse)
				{
					ModifyUserResponse response = new ModifyUserResponse(httpResponse);
					if (response.getStatus())
					{
						dbManager.updateUser(currentUser);
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ReimApplication.dismissProgressDialog();
								Toast.makeText(ProfileActivity.this, "用户信息修改成功", Toast.LENGTH_SHORT).show();
								adapter.setUser(currentUser);
								adapter.notifyDataSetChanged();
							}
						});
					}
					else
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ReimApplication.dismissProgressDialog();
								Toast.makeText(ProfileActivity.this, "用户信息修改失败", Toast.LENGTH_SHORT).show();
								adapter.notifyDataSetChanged();
							}
						});						
					}
				}
			});
		}
	}

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(3);
    	Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}
