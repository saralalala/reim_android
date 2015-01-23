package com.rushucloud.reim.me;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Category.CreateCategoryResponse;
import netUtils.Response.Category.DeleteCategoryResponse;
import netUtils.Response.Category.ModifyCategoryResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Category.CreateCategoryRequest;
import netUtils.Request.Category.DeleteCategoryRequest;
import netUtils.Request.Category.ModifyCategoryRequest;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Category;
import classes.ReimApplication;
import classes.adapter.CategoryListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CategoryActivity extends Activity
{
	private ListView categoryListView;
	private TextView categoryTextView;
	private CategoryListViewAdapter adapter;
	private PopupWindow operationPopupWindow;
	private PopupWindow categoryPopupWindow;
	private ImageView iconImageView;
	private EditText nameEditText;
	private EditText limitEditText;
	private ToggleButton proveAheadToggleButton;
	private LinearLayout iconLayout;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<Category> categoryList;
	private List<Integer> iconList;
	private List<Boolean> checkList;
	private Category currentCategory;
	private boolean isNewCategory;
	
	private int iconWidth;
	private int iconHorizontalInterval;
	private int iconVerticalInterval;
	private int iconMaxCount;	

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
		ReimProgressDialog.setProgressDialog(this);
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
		
		iconList = new ArrayList<Integer>();
		iconList.add(R.drawable.icon_food);
		iconList.add(R.drawable.icon_transport);
		iconList.add(R.drawable.icon_office_supplies);
		iconList.add(R.drawable.icon_business_development);
		iconList.add(R.drawable.icon_marketing);
		iconList.add(R.drawable.icon_recruiting);
		iconList.add(R.drawable.icon_travel);
		iconList.add(R.drawable.icon_operating);
		iconList.add(R.drawable.icon_entertainment);
		iconList.add(R.drawable.icon_others);
		
		checkList = new ArrayList<Boolean>();
		for (int i = 0; i < iconList.size(); i++)
		{
			checkList.add(false);
		}
	}
	
	private void initView()
	{
		getActionBar().hide();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int padding = PhoneUtils.dpToPixel(getResources(), 32);
		iconWidth = PhoneUtils.dpToPixel(getResources(), 40);
		iconHorizontalInterval = PhoneUtils.dpToPixel(getResources(), 20);
		iconMaxCount = (metrics.widthPixels - padding + iconHorizontalInterval) / (iconWidth + iconHorizontalInterval);
		iconHorizontalInterval = (metrics.widthPixels - padding - iconWidth * iconMaxCount) / (iconMaxCount - 1);
		iconVerticalInterval = PhoneUtils.dpToPixel(getResources(), 20);
		
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
					ViewUtils.showToast(CategoryActivity.this, R.string.error_add_network_unavailable);
				}
				else
				{
					isNewCategory = true;
					currentCategory = new Category();
					showCategoryWindow();
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
				currentCategory = categoryList.get(position);
				showOperationWindow();
				return false;
			}
		});

		initCategoryWindow();
	}

	private void initCategoryWindow()
	{ 		
		View categoryView = View.inflate(this, R.layout.window_me_category, null);    		

		iconImageView = (ImageView) categoryView.findViewById(R.id.iconImageView);
		
		nameEditText = (EditText) categoryView.findViewById(R.id.nameEditText);
		nameEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		limitEditText = (EditText) categoryView.findViewById(R.id.limitEditText);
		limitEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		proveAheadToggleButton = (ToggleButton) categoryView.findViewById(R.id.proveAheadToggleButton);

		iconLayout = (LinearLayout) categoryView.findViewById(R.id.iconLayout);
		
		ImageView backImageView = (ImageView) categoryView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				categoryPopupWindow.dismiss();
			}
		});    		
		
		TextView saveTextView = (TextView) categoryView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String name = nameEditText.getText().toString();
				String limit = limitEditText.getText().toString();
				if (name.equals(""))
				{
					ViewUtils.showToast(CategoryActivity.this, R.string.error_category_name_empty);
				}
				else
				{
					currentCategory.setName(name);
					currentCategory.setParentID(0);
					currentCategory.setGroupID(appPreference.getCurrentGroupID());
					currentCategory.setIsProveAhead(proveAheadToggleButton.isChecked());

   					if (!limit.equals(""))
					{
						currentCategory.setLimit(Double.valueOf(limit));
					}
					
					int iconIndex = checkList.indexOf(true);
					if (iconIndex != -1)
					{
    					currentCategory.setIconID(iconIndex + 1);
						Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconList.get(iconIndex));
						String iconPath = PhoneUtils.saveIconToFile(bitmap, iconIndex + 1);
						currentCategory.setIconPath(iconPath);
					}
					else
					{
    					currentCategory.setIconID(-1);
					}
					
					if (isNewCategory)
					{
						sendCreateCategoryRequest(currentCategory);
					}
					else
					{
						sendModifyCategoryRequest(currentCategory);
					}
				}
			}
		});
		
		categoryPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, categoryView);
	}

	private void resetCheck()
	{
		for (int j = 0; j < checkList.size(); j++)
		{
			checkList.set(j, false);
		}
	}
	
	private void refreshListView()
	{
		categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
		adapter = new CategoryListViewAdapter(this, categoryList, null);
		categoryListView.setAdapter(adapter);
		
		if (categoryList.isEmpty())
		{
			categoryListView.setVisibility(View.INVISIBLE);
			categoryTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			categoryListView.setVisibility(View.VISIBLE);
			categoryTextView.setVisibility(View.INVISIBLE);			
		}	
		
		if (PhoneUtils.isNetworkConnected())
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

	private void refreshIconLayout()
	{
		iconLayout.removeAllViews();

		LinearLayout layout = new LinearLayout(this);
		for (int i = 0; i < iconList.size(); i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				if (i != 0)
				{
					params.topMargin = iconVerticalInterval;					
				}
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				iconLayout.addView(layout);
			}

			final int index = i;
			ImageView imageView = new ImageView(this);
			imageView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					resetCheck();
					checkList.set(index, true);
	    			iconImageView.setImageResource(iconList.get(index));
					refreshIconLayout();
				}
			});
			if (checkList.get(i))
			{
				imageView.setImageResource(R.drawable.icon_chosen);
			}
			else
			{
				imageView.setImageResource(iconList.get(i));
			}
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconWidth, iconWidth);
			if (i % iconMaxCount != 0)
			{
				params.leftMargin = iconHorizontalInterval;
			}
			layout.addView(imageView, params);
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
    					ViewUtils.showToast(CategoryActivity.this, R.string.error_modify_network_unavailable);
    				}
    				else
    				{
    					isNewCategory = false;
    					showCategoryWindow();
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
    					ViewUtils.showToast(CategoryActivity.this, R.string.error_delete_network_unavailable);
    				}
    				else
    				{
    					Builder builder = new Builder(CategoryActivity.this);
    					builder.setTitle(R.string.warning);
    					builder.setMessage(R.string.prompt_delete_category);
    					builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
    												{
    													public void onClick(DialogInterface dialog, int which)
    													{
    														sendDeleteCategoryRequest(currentCategory);
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
    
	private void showCategoryWindow()
	{
		resetCheck();
		
		if (!isNewCategory)
		{
			nameEditText.setText(currentCategory.getName());
			limitEditText.setText(Utils.formatDouble(currentCategory.getLimit()));
			proveAheadToggleButton.setChecked(currentCategory.isProveAhead());

			if (currentCategory.getIconID() > 0)
			{
				int index = currentCategory.getIconID() - 1;
    			checkList.set(index, true);
    			iconImageView.setImageResource(iconList.get(index));
			}
		}
		else
		{
			nameEditText.setText("");
			limitEditText.setText("");
			proveAheadToggleButton.setChecked(false);
			iconImageView.setImageResource(R.drawable.default_icon);
		}
		
		refreshIconLayout();
		
		categoryPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		categoryPopupWindow.update();
	}

	private void sendCreateCategoryRequest(final Category category)
	{
		ReimProgressDialog.show();
		CreateCategoryRequest request = new CreateCategoryRequest(category);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateCategoryResponse response = new CreateCategoryResponse(httpResponse);
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
							ReimProgressDialog.dismiss();
							categoryPopupWindow.dismiss();
							ViewUtils.showToast(CategoryActivity.this, R.string.succeed_in_creating_category);
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
							ViewUtils.showToast(CategoryActivity.this, R.string.failed_to_create_category, response.getErrorMessage());
						}
					});
				}
			}
		});
	}

	private void sendModifyCategoryRequest(final Category category)
	{
		ReimProgressDialog.show();
		ModifyCategoryRequest request = new ModifyCategoryRequest(category);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyCategoryResponse response = new ModifyCategoryResponse(httpResponse);
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
							ReimProgressDialog.dismiss();
							categoryPopupWindow.dismiss();
							ViewUtils.showToast(CategoryActivity.this, R.string.succeed_in_modifying_category);
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
							ViewUtils.showToast(CategoryActivity.this, R.string.failed_to_modify_category, response.getErrorMessage());
						}
					});
				}
			}
		});
	}

	private void sendDeleteCategoryRequest(final Category category)
	{
		ReimProgressDialog.show();
		DeleteCategoryRequest request = new DeleteCategoryRequest(category.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DeleteCategoryResponse response = new DeleteCategoryResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.deleteCategory(category.getServerID());
					dbManager.deleteSubCategories(category.getServerID(), appPreference.getCurrentGroupID());
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshListView();
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(CategoryActivity.this, R.string.failed_to_delete_category);
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
							ViewUtils.showToast(CategoryActivity.this, R.string.failed_to_delete_category, response.getErrorMessage());
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
					String iconPath = PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
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