package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Tag.CreateTagRequest;
import netUtils.Request.Tag.DeleteTagRequest;
import netUtils.Request.Tag.ModifyTagRequest;
import netUtils.Response.Tag.CreateTagResponse;
import netUtils.Response.Tag.DeleteTagResponse;
import netUtils.Response.Tag.ModifyTagResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.Tag;
import classes.ReimApplication;
import classes.Utils;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TagActivity extends Activity
{
	private ListView tagListView;
	private TextView tagTextView;
	private ArrayAdapter<String> adapter;
	private List<Tag> tagList;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_tag);
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
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "删除");
	}

	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		final int index = (int) tagListView.getAdapter().getItemId(menuInfo.position);
		final Tag tag = tagList.get(index);
		switch (item.getItemId())
		{
			case 0:
			{
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(this, "网络未连接，无法删除");
				}
				else 
				{
					AlertDialog mDialog = new AlertDialog.Builder(TagActivity.this)
											.setTitle("警告")
											.setMessage(R.string.deleteItemWarning)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													sendDeleteTagRequest(tag);
												}
											})
											.setNegativeButton(R.string.cancel, null)
											.create();
					mDialog.show();
				}
				break;
			}
			default:
				break;
		}

		return super.onContextItemSelected(item);
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
		registerForContextMenu(tagListView);
		
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
	}
	
	private void refreshListView()
	{
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Tag.getTagsName(tagList));
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
	}

	private void showTagDialog(final Tag tag)
	{
		final boolean isNewTag = tag.getServerID() == -1 ? true : false; 
		View view = View.inflate(this, R.layout.me_tag_dialog, null);
		final EditText nameEditText = (EditText)view.findViewById(R.id.nameEditText);
		
		if (!isNewTag)
		{
			nameEditText.setText(tag.getName());
		}
		
		AlertDialog mDialog = new AlertDialog.Builder(this)
											.setTitle("请输入标签信息")
											.setView(view)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
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
											})
											.setNegativeButton(R.string.cancel, null)
											.create();
		mDialog.show();
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
}
