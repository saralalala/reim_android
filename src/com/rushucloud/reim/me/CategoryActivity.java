package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Category.CreateCategoryRequest;
import netUtils.Request.Category.DeleteCategoryRequest;
import netUtils.Request.Category.ModifyCategoryRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Category.CreateCategoryResponse;
import netUtils.Response.Category.DeleteCategoryResponse;
import netUtils.Response.Category.ModifyCategoryResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.Category;
import classes.ReimApplication;
import classes.Utils;
import classes.Adapter.CategoryListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CategoryActivity extends Activity
{
	private ListView categoryListView;
	private TextView categoryTextView;
	private CategoryListViewAdapter adapter;
	private PopupWindow operationPopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Category> categoryList;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_category_management);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CategoryActivity");
		MobclickAgent.onResume(this);
		refreshListView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CategoryActivity");
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
					Utils.showToast(CategoryActivity.this, "网络未连接，无法添加");
				}
				else
				{
					showCategoryDialog(new Category());
				}
			}
		});

		categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		
		categoryListView = (ListView)findViewById(R.id.categoryListView);
		categoryListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(CategoryActivity.this, SubCategoryActivity.class);
				intent.putExtra("parentID", categoryList.get(position).getServerID());
				startActivity(intent);
			}
		});
		categoryListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				showOperationWindow(position);
				return false;
			}
		});
	}

	private void refreshListView()
	{
		categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
		adapter = new CategoryListViewAdapter(this, categoryList, null);
		categoryListView.setAdapter(adapter);
		
		if (categoryList.size() == 0)
		{
			categoryListView.setVisibility(View.INVISIBLE);
			categoryTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			categoryListView.setVisibility(View.VISIBLE);
			categoryTextView.setVisibility(View.INVISIBLE);			
		}	
		
		if (Utils.isNetworkConnected())
		{
			for (Category category : categoryList)
			{
				if (category.hasUndownloadedIcon())
				{
					sendDownloadIconRequest(category);
				}
			}
		}	
	}

    private void showOperationWindow(final int index)
    {    
    	if (operationPopupWindow == null)
		{
    		View operationView = View.inflate(this, R.layout.window_report_operation, null);
    		
    		Button modifyButton = (Button) operationView.findViewById(R.id.modifyButton);
    		modifyButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();

    				final Category category = categoryList.get(index);
    				if (!Utils.isNetworkConnected())
    				{
    					Utils.showToast(CategoryActivity.this, "网络未连接，无法修改");
    				}
    				else
    				{
    					showCategoryDialog(category);
    				}
    			}
    		});
    		modifyButton = Utils.resizeWindowButton(modifyButton);
    		
    		Button deleteButton = (Button) operationView.findViewById(R.id.deleteButton);
    		deleteButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();
    				
    				final Category category = categoryList.get(index);
    				if (!Utils.isNetworkConnected())
    				{
    					Utils.showToast(CategoryActivity.this, "网络未连接，无法删除");
    				}
    				else
    				{
    					Builder builder = new Builder(CategoryActivity.this);
    					builder.setTitle(R.string.warning);
    					builder.setMessage("是否要删除此分类");
    					builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
    												{
    													public void onClick(DialogInterface dialog, int which)
    													{
    														sendDeleteCategoryRequest(category);
    													}
    												});
    					builder.setNegativeButton(R.string.cancel, null);
    					AlertDialog alertDialog = builder.create();
    					alertDialog.show();
    				}
    			}
    		});
    		deleteButton = Utils.resizeWindowButton(deleteButton);
    		
    		Button cancelButton = (Button) operationView.findViewById(R.id.cancelButton);
    		cancelButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				operationPopupWindow.dismiss();
    			}
    		});
    		cancelButton = Utils.resizeWindowButton(cancelButton);
    		
    		operationPopupWindow = Utils.constructPopupWindow(this, operationView);    	
		}
    	
		operationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		operationPopupWindow.update();
		
		Utils.dimBackground(this);
    }
    
	private void showCategoryDialog(final Category category)
	{
		final boolean isNewCategory = category.getServerID() == -1 ? true : false;
		View view = View.inflate(this, R.layout.dialog_me_category, null);
		final EditText nameEditText = (EditText) view.findViewById(R.id.nameEditText);
		final EditText limitEditText = (EditText) view.findViewById(R.id.limitEditText);
		final CheckBox proveAheadCheckBox = (CheckBox) view.findViewById(R.id.proveAheadCheckBox);

		if (!isNewCategory)
		{
			nameEditText.setText(category.getName());
			limitEditText.setText(Double.toString(category.getLimit()));
			proveAheadCheckBox.setChecked(category.isProveAhead());
		}


		Builder builder = new Builder(this);
		builder.setTitle("请输入分类信息");
		builder.setView(view);
		builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which)
										{
											String name = nameEditText.getText().toString();
											String limit = limitEditText.getText().toString();
											if (name.equals(""))
											{
												Utils.showToast(CategoryActivity.this, "分类名称不能为空");
											}
											else
											{
												category.setName(name);
												category.setParentID(0);
												category.setGroupID(appPreference.getCurrentGroupID());
												category.setIsProveAhead(proveAheadCheckBox.isChecked());
												if (!limit.equals(""))
												{
													category.setLimit(Double.valueOf(limit));
												}
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
									});
		builder.setNegativeButton(R.string.cancel, null);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
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
							Utils.showToast(CategoryActivity.this, "分类创建成功");
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
							Utils.showToast(CategoryActivity.this, "分类创建失败");
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
							Utils.showToast(CategoryActivity.this, "分类修改成功");
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
							Utils.showToast(CategoryActivity.this, "分类修改失败");
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
							Utils.showToast(CategoryActivity.this, "分类删除成功");
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
							Utils.showToast(CategoryActivity.this, "分类删除失败");
						}
					});
				}
			}
		});
	}

    private void sendDownloadIconRequest(final Category category)
    {
    	DownloadImageRequest request = new DownloadImageRequest(category.getIconID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String iconPath = Utils.saveIconToFile(response.getBitmap(), category.getIconID());
					category.setIconPath(iconPath);
					category.setLocalUpdatedDate(Utils.getCurrentTime());
					category.setServerUpdatedDate(category.getLocalUpdatedDate());
					dbManager.updateCategory(category);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
							adapter.setCategory(categoryList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}