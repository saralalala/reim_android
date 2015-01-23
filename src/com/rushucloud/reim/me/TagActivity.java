package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Tag.CreateTagResponse;
import netUtils.Response.Tag.DeleteTagResponse;
import netUtils.Response.Tag.ModifyTagResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Tag.CreateTagRequest;
import netUtils.Request.Tag.DeleteTagRequest;
import netUtils.Request.Tag.ModifyTagRequest;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Tag;
import classes.adapter.TagListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TagActivity extends Activity
{
	private ListView tagListView;
	private TextView tagTextView;
	private TagListViewAdapter adapter;
	private PopupWindow operationPopupWindow;
	private PopupWindow tagPopupWindow;
	private EditText nameEditText;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Tag> tagList;
	private Tag currentTag;
	private boolean isNewTag;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_tag_management);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("TagActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshListView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("TagActivity");
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
		
		TextView addTextView = (TextView)findViewById(R.id.addTextView);
		addTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(TagActivity.this, R.string.error_add_network_unavailable);
				}
				else
				{
					isNewTag = true;
					currentTag = new Tag();
					showTagWindow();
				}
			}
		});
		
		tagTextView = (TextView)findViewById(R.id.tagTextView);
		
		tagListView = (ListView)findViewById(R.id.tagListView);
		tagListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				currentTag = tagList.get(position);
				showOperationWindow();
				return false;
			}
		});
		
		initTagWindow();
	}

	private void initTagWindow()
	{ 		
		View tagView = View.inflate(this, R.layout.window_me_tag, null);
		
		nameEditText = (EditText) tagView.findViewById(R.id.nameEditText);
		nameEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		ImageView backImageView = (ImageView) tagView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				tagPopupWindow.dismiss();
			}
		});    		
		
		TextView saveTextView = (TextView) tagView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String name = nameEditText.getText().toString();
				if (name.equals(""))
				{
					ViewUtils.showToast(TagActivity.this, R.string.error_tag_name_empty);
				}
				else
				{
					currentTag.setName(name);
					currentTag.setGroupID(appPreference.getCurrentGroupID());
					if (isNewTag)
					{
						sendCreateTagRequest(currentTag);															
					}
					else
					{
						sendModifyTagRequest(currentTag);
					}
				}
			}
		});
		
		tagPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, tagView);
	}
	
	private void refreshListView()
	{
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		adapter = new TagListViewAdapter(this, tagList, null);
		tagListView.setAdapter(adapter);
		
		if (tagList.isEmpty())
		{
			tagListView.setVisibility(View.INVISIBLE);
			tagTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			tagListView.setVisibility(View.VISIBLE);
			tagTextView.setVisibility(View.INVISIBLE);			
		}
		
		if (PhoneUtils.isNetworkConnected())
		{
			for (Tag tag : tagList)
			{
				if (tag.hasUndownloadedIcon())
				{
					sendDownloadIconRequest(tag);
				}
			}
		}
	}

    private void showOperationWindow()
    {    
    	if (operationPopupWindow == null)
		{
    		View operationView = View.inflate(this, R.layout.window_operation, null);
    		
    		Button modifyButton = (Button) operationView.findViewById(R.id.modifyButton);
    		modifyButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();
    				
    				if (!PhoneUtils.isNetworkConnected())
    				{
    					ViewUtils.showToast(TagActivity.this, R.string.error_modify_network_unavailable);
    				}
    				else
    				{
    					isNewTag = false;
    					showTagWindow();
    				}
    			}
    		});
    		modifyButton = ViewUtils.resizeWindowButton(modifyButton);
    		
    		Button deleteButton = (Button) operationView.findViewById(R.id.deleteButton);
    		deleteButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();
    				
    				if (!PhoneUtils.isNetworkConnected())
    				{
    					ViewUtils.showToast(TagActivity.this, R.string.error_delete_network_unavailable);
    				}
    				else 
    				{
    					Builder builder = new Builder(TagActivity.this);
    					builder.setTitle(R.string.warning);
    					builder.setMessage(R.string.prompt_delete_tag);
    					builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
    											{
    												public void onClick(DialogInterface dialog, int which)
    												{
    													sendDeleteTagRequest(currentTag);
    												}
    											});
    					builder.setNegativeButton(R.string.cancel, null);
    					builder.create().show();
    				}
    			}
    		});
    		deleteButton = ViewUtils.resizeWindowButton(deleteButton);
    		
    		Button cancelButton = (Button) operationView.findViewById(R.id.cancelButton);
    		cancelButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();
    			}
    		});
    		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
    		
    		operationPopupWindow = ViewUtils.constructBottomPopupWindow(this, operationView);    	
		}
    	
		operationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		operationPopupWindow.update();
		
		ViewUtils.dimBackground(this);
    }
    
    private void showTagWindow()
    {
		nameEditText.setText(currentTag.getName());
		
		tagPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		tagPopupWindow.update();
    }
	
	private void sendCreateTagRequest(final Tag tag)
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
							refreshListView();
							ReimProgressDialog.dismiss();
							tagPopupWindow.dismiss();
							ViewUtils.showToast(TagActivity.this, R.string.succeed_in_creating_tag);
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
							ViewUtils.showToast(TagActivity.this, R.string.failed_to_create_tag);							
						}
					});
				}
			}
		});
	}
	
	private void sendModifyTagRequest(final Tag tag)
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
							refreshListView();
							ReimProgressDialog.dismiss();
							tagPopupWindow.dismiss();
							ViewUtils.showToast(TagActivity.this, R.string.succeed_in_modifying_tag);
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
							ViewUtils.showToast(TagActivity.this, R.string.failed_to_modify_tag);							
						}
					});
				}
			}
		});
	}
	
	private void sendDeleteTagRequest(final Tag tag)
	{
		ReimProgressDialog.show();
		DeleteTagRequest request = new DeleteTagRequest(tag.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DeleteTagResponse response = new DeleteTagResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.deleteTag(tag.getServerID());
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshListView();
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(TagActivity.this, R.string.succeed_in_deleting_tag);
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
							ViewUtils.showToast(TagActivity.this, R.string.failed_to_delete_tag);					
						}
					});
				}
			}
		});
	}

    private void sendDownloadIconRequest(final Tag tag)
    {
    	DownloadImageRequest request = new DownloadImageRequest(tag.getIconID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String iconPath = PhoneUtils.saveIconToFile(response.getBitmap(), tag.getIconID());
					tag.setIconPath(iconPath);
					tag.setLocalUpdatedDate(Utils.getCurrentTime());
					tag.setServerUpdatedDate(tag.getLocalUpdatedDate());
					dbManager.updateTag(tag);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
							adapter.setTag(tagList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}