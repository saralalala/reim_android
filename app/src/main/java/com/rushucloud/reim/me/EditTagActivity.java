package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Tag;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.Tag.CreateTagRequest;
import netUtils.Request.Tag.ModifyTagRequest;
import netUtils.Response.Tag.CreateTagResponse;
import netUtils.Response.Tag.ModifyTagResponse;

public class EditTagActivity extends Activity
{
	private EditText nameEditText;

	private DBManager dbManager;
	private Tag tag;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_edit_tag);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditTagActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EditTagActivity");
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
		dbManager = DBManager.getDBManager();
		
		tag = (Tag) getIntent().getSerializableExtra("tag");
	}
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});    		
		
		TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String name = nameEditText.getText().toString();
				if (name.isEmpty())
				{
					ViewUtils.showToast(EditTagActivity.this, R.string.error_tag_name_empty);
				}
				else if (tag.getName().equals(name))
				{
					finish();
				}
				else
				{
					tag.setName(name);
					tag.setGroupID(AppPreference.getAppPreference().getCurrentGroupID());
					if (tag.getServerID() == -1)
					{
						sendCreateTagRequest();															
					}
					else
					{
						sendModifyTagRequest();
					}
				}
			}
		});
		
		nameEditText = (EditText) findViewById(R.id.nameEditText);
		nameEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		nameEditText.setText(tag.getName());
	}
	
	private void sendCreateTagRequest()
	{
		ReimProgressDialog.show();
		CreateTagRequest request = new CreateTagRequest(tag);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateTagResponse response = new CreateTagResponse(httpResponse);
				if (response.getStatus())
				{
					tag.setServerID(response.getTagID());
					tag.setLocalUpdatedDate(Utils.getCurrentTime());
					tag.setServerUpdatedDate(tag.getLocalUpdatedDate());
					dbManager.insertTag(tag);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditTagActivity.this, R.string.succeed_in_creating_tag);
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
							ViewUtils.showToast(EditTagActivity.this, R.string.failed_to_create_tag);							
						}
					});
				}
			}
		});
	}
	
	private void sendModifyTagRequest()
	{
		ReimProgressDialog.show();
		ModifyTagRequest request = new ModifyTagRequest(tag);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyTagResponse response = new ModifyTagResponse(httpResponse);
				if (response.getStatus())
				{
					tag.setLocalUpdatedDate(Utils.getCurrentTime());
					tag.setServerUpdatedDate(tag.getLocalUpdatedDate());
					dbManager.updateTag(tag);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditTagActivity.this, R.string.succeed_in_modifying_tag);
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
							ViewUtils.showToast(EditTagActivity.this, R.string.failed_to_modify_tag);							
						}
					});
				}
			}
		});
	}
}