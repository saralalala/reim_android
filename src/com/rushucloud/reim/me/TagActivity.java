package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Tag.CreateTagRequest;
import netUtils.Request.Tag.DeleteTagRequest;
import netUtils.Request.Tag.ModifyTagRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Tag.CreateTagResponse;
import netUtils.Response.Tag.DeleteTagResponse;
import netUtils.Response.Tag.ModifyTagResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.Tag;
import classes.ReimApplication;
import classes.Utils;
import classes.Adapter.TagListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	private PopupWindow deletePopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Tag> tagList;
	
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
		ReimApplication.setProgressDialog(this);
		
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
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(TagActivity.this, "网络未连接，无法添加");
				}
				else
				{
					showTagDialog(new Tag());
				}
			}
		});
		
		tagTextView = (TextView)findViewById(R.id.tagTextView);
		
		tagListView = (ListView)findViewById(R.id.tagListView);
		tagListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Tag tag = tagList.get(position);
				showTagDialog(tag);
			}
		});
		tagListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				showDeleteWindow(position);
				return false;
			}
		});		
	}
	
	private void refreshListView()
	{
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		adapter = new TagListViewAdapter(this, tagList, null);
		tagListView.setAdapter(adapter);
		
		if (tagList.size() == 0)
		{
			tagListView.setVisibility(View.INVISIBLE);
			tagTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			tagListView.setVisibility(View.VISIBLE);
			tagTextView.setVisibility(View.INVISIBLE);			
		}
		
		if (Utils.isNetworkConnected())
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

    private void showDeleteWindow(final int index)
    {    
    	if (deletePopupWindow == null)
		{
    		View deleteView = View.inflate(this, R.layout.window_delete, null);
    		
    		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
    		deleteButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				deletePopupWindow.dismiss();
    				
    				final Tag tag = tagList.get(index);
    				if (!Utils.isNetworkConnected())
    				{
    					Utils.showToast(TagActivity.this, "网络未连接，无法删除");
    				}
    				else 
    				{
    					Builder builder = new Builder(TagActivity.this);
    					builder.setTitle(R.string.warning);
    					builder.setMessage("是否要删除此标签");
    					builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
    											{
    												public void onClick(DialogInterface dialog, int which)
    												{
    													sendDeleteTagRequest(tag);
    												}
    											});
    					builder.setNegativeButton(R.string.cancel, null);
    					builder.create().show();
    				}
    			}
    		});
    		deleteButton = Utils.resizeWindowButton(deleteButton);
    		
    		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
    		cancelButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				deletePopupWindow.dismiss();
    			}
    		});
    		cancelButton = Utils.resizeWindowButton(cancelButton);
    		
    		deletePopupWindow = Utils.constructPopupWindow(this, deleteView);    	
		}
    	
		deletePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		Utils.dimBackground(this);
    }

	private void showTagDialog(final Tag tag)
	{
		final boolean isNewTag = tag.getServerID() == -1 ? true : false; 
		View view = View.inflate(this, R.layout.dialog_me_tag, null);
		final EditText nameEditText = (EditText)view.findViewById(R.id.nameEditText);
		nameEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		if (!isNewTag)
		{
			nameEditText.setText(tag.getName());
		}
		
		Builder builder = new Builder(this);
		builder.setTitle("请输入标签信息");
		builder.setView(view);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													String name = nameEditText.getText().toString();
													if (name.equals(tag.getName()))
													{
														return;
													}
													else if (Utils.isNetworkConnected())
													{						
														Utils.showToast(TagActivity.this, "网络未连接，无法修改");										
													}
													else if (name.equals(""))
													{
														Utils.showToast(TagActivity.this, "标签名称不能为空");
													}
													else
													{
														tag.setName(name);
														tag.setGroupID(appPreference.getCurrentGroupID());
														if (isNewTag)
														{
															sendCreateTagRequest(tag);															
														}
														else
														{
															sendUpdateTagRequest(tag);
														}
													}
												}
											});
		builder.setNegativeButton(R.string.cancel, null);
		builder.create().show();
	}
	
	private void sendCreateTagRequest(final Tag tag)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(TagActivity.this, "标签创建成功");
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
							Utils.showToast(TagActivity.this, "标签创建失败");							
						}
					});
				}
			}
		});
	}
	
	private void sendUpdateTagRequest(final Tag tag)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(TagActivity.this, "标签修改成功");
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
							Utils.showToast(TagActivity.this, "标签修改失败");							
						}
					});
				}
			}
		});
	}
	
	private void sendDeleteTagRequest(final Tag tag)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(TagActivity.this, "标签删除成功");
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
							Utils.showToast(TagActivity.this, "标签删除失败");					
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
					String iconPath = Utils.saveIconToFile(response.getBitmap(), tag.getIconID());
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