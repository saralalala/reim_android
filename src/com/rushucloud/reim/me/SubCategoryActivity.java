package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Category.CreateCategoryRequest;
import netUtils.Request.Category.DeleteCategoryRequest;
import netUtils.Request.Category.ModifyCategoryRequest;
import netUtils.Response.Category.CreateCategoryResponse;
import netUtils.Response.Category.DeleteCategoryResponse;
import netUtils.Response.Category.ModifyCategoryResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.Category;
import classes.ReimApplication;
import classes.Utils;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SubCategoryActivity extends Activity
{
	private ListView categoryListView;
	private ArrayAdapter<String> adapter;
	private List<Category> categoryList;
	private int parentID;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_category);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SubCategoryActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
		refreshListView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SubCategoryActivity");
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

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.add, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_add_item)
		{
			if (!Utils.isNetworkConnected())
			{
				Toast.makeText(this, "网络未连接，无法添加", Toast.LENGTH_SHORT).show();
			}
			else
			{
				showCategoryDialog(new Category());
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "修改");
		menu.add(0, 1, 0, "删除");
	}

	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		final int index = (int) categoryListView.getAdapter().getItemId(menuInfo.position);
		final Category category = categoryList.get(index);
		switch (item.getItemId())
		{
			case 0:
			{
				if (!Utils.isNetworkConnected())
				{
					Toast.makeText(this, "网络未连接，无法修改", Toast.LENGTH_SHORT).show();
				}
				else
				{
					showCategoryDialog(category);
				}
				break;
			}
			case 1:
				{
					if (!Utils.isNetworkConnected())
					{
						Toast.makeText(this, "网络未连接，无法删除", Toast.LENGTH_SHORT).show();
					}
					else 
					{
						AlertDialog mDialog = new AlertDialog.Builder(SubCategoryActivity.this)
												.setTitle("警告")
												.setMessage(R.string.deleteItemWarning)
												.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
												{
													public void onClick(DialogInterface dialog, int which)
													{
														sendDeleteCategoryRequest(category);
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
		
		parentID = this.getIntent().getIntExtra("parentID", -1);
	}
	
	private void initView()
	{		
		categoryListView = (ListView)findViewById(R.id.categoryListView);
		registerForContextMenu(categoryListView);
	}
	
	private void refreshListView()
	{
		categoryList = dbManager.getSubCategories(parentID, appPreference.getCurrentGroupID());
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Category.getCategoryNames(categoryList));
		categoryListView.setAdapter(adapter);
	}

	private void showCategoryDialog(final Category category)
	{
		final boolean isNewCategory = category.getServerID() == -1 ? true : false; 
		View view = View.inflate(this, R.layout.profile_category_dialog, null);
		final EditText nameEditText = (EditText)view.findViewById(R.id.nameEditText);
		final EditText limitEditText = (EditText)view.findViewById(R.id.limitEditText);
		final CheckBox proveAheadCheckBox = (CheckBox)view.findViewById(R.id.proveAheadCheckBox);
		
		if (!isNewCategory)
		{
			nameEditText.setText(category.getName());
			limitEditText.setText(Double.toString(category.getLimit()));
			proveAheadCheckBox.setChecked(category.isProveAhead());
		}
		
		AlertDialog mDialog = new AlertDialog.Builder(this)
											.setTitle("请输入分类信息")
											.setView(view)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													String name = nameEditText.getText().toString();
													String limit = limitEditText.getText().toString();
													if (name.equals(""))
													{
														Toast.makeText(SubCategoryActivity.this, "分类名称不能为空",
																	Toast.LENGTH_SHORT).show();
													}
													else if (limit.equals(""))
													{
														Toast.makeText(SubCategoryActivity.this, "分类限额不能为空",
																	Toast.LENGTH_SHORT).show();
													}
													else
													{
														category.setName(name);
														category.setLimit(Double.valueOf(limit));
														category.setParentID(parentID);
														category.setGroupID(appPreference.getCurrentGroupID());
														category.setIsProveAhead(proveAheadCheckBox.isChecked());
														if (isNewCategory)
														{
															sendCreateCategoryRequest(category);															
														}
														else
														{
															sendUpdateCategoryRequest(category);
														}
													}
												}
											})
											.setNegativeButton(R.string.cancel, null)
											.create();
		mDialog.show();
	}
	
	private void sendCreateCategoryRequest(final Category category)
	{
		ReimApplication.showProgressDialog();
		CreateCategoryRequest request = new CreateCategoryRequest(category);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateCategoryResponse response = new CreateCategoryResponse(httpResponse);
				if (response.getStatus())
				{
					category.setServerID(response.getCategoryID());
					category.setLocalUpdatedDate(Utils.getCurrentTime());
					category.setServerUpdatedDate(category.getLocalUpdatedDate());
					dbManager.insertCategory(category);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshListView();
							ReimApplication.dismissProgressDialog();
							Toast.makeText(SubCategoryActivity.this, "分类创建成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(SubCategoryActivity.this, "分类创建失败", Toast.LENGTH_SHORT).show();							
						}
					});
				}
			}
		});
	}
	
	private void sendUpdateCategoryRequest(final Category category)
	{
		ReimApplication.showProgressDialog();
		ModifyCategoryRequest request = new ModifyCategoryRequest(category);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyCategoryResponse response = new ModifyCategoryResponse(httpResponse);
				if (response.getStatus())
				{
					category.setLocalUpdatedDate(Utils.getCurrentTime());
					category.setServerUpdatedDate(category.getLocalUpdatedDate());
					dbManager.updateCategory(category);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshListView();
							ReimApplication.dismissProgressDialog();
							Toast.makeText(SubCategoryActivity.this, "分类修改成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(SubCategoryActivity.this, "分类修改失败", Toast.LENGTH_SHORT).show();							
						}
					});
				}
			}
		});
	}
	
	private void sendDeleteCategoryRequest(final Category category)
	{
		ReimApplication.showProgressDialog();
		DeleteCategoryRequest request = new DeleteCategoryRequest(category.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DeleteCategoryResponse response = new DeleteCategoryResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.deleteCategory(category.getServerID());
					dbManager.deleteSubCategories(category.getServerID(), appPreference.getCurrentGroupID());
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshListView();
							ReimApplication.dismissProgressDialog();
							Toast.makeText(SubCategoryActivity.this, "分类删除成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(SubCategoryActivity.this, "分类删除失败", Toast.LENGTH_SHORT).show();					
						}
					});
				}
			}
		});
	}
}
