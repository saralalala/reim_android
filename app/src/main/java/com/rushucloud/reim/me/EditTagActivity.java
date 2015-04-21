package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Tag;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.tag.CreateTagRequest;
import netUtils.request.tag.ModifyTagRequest;
import netUtils.response.tag.CreateTagResponse;
import netUtils.response.tag.ModifyTagResponse;

public class EditTagActivity extends Activity
{
	private ClearEditText nameEditText;

	private DBManager dbManager;
    private String originalName;
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
	
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
            goBack();
		}
		return super.onKeyDown(keyCode, event);
	}
		
	private void initData()
	{
		dbManager = DBManager.getDBManager();
		tag = (Tag) getIntent().getSerializableExtra("tag");
        originalName = tag.getName();
	}
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});    		
		
		TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String name = nameEditText.getText().toString();
                tag.setName(name);
                tag.setGroupID(AppPreference.getAppPreference().getCurrentGroupID());

                if (!PhoneUtils.isNetworkConnected() && tag.getServerID() == -1)
                {
                    ViewUtils.showToast(EditTagActivity.this, R.string.error_add_network_unavailable);
                }
                else if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(EditTagActivity.this, R.string.error_modify_network_unavailable);
                }
				else if (name.isEmpty())
				{
					ViewUtils.showToast(EditTagActivity.this, R.string.error_tag_name_empty);
				}
				else if (tag.getName().equals(originalName))
				{
                    goBack();
				}
				else if (tag.getServerID() == -1)
				{
                    sendCreateTagRequest();
				}
                else
                {
                    sendModifyTagRequest();
                }
			}
		});
		
		nameEditText = (ClearEditText) findViewById(R.id.nameEditText);
		nameEditText.setText(tag.getName());
        ViewUtils.requestFocus(this, nameEditText);
	}

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
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
                            goBack();
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
                            goBack();
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

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}