package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.category.CreateCategoryRequest;
import netUtils.request.category.ModifyCategoryRequest;
import netUtils.response.category.CreateCategoryResponse;
import netUtils.response.category.ModifyCategoryResponse;

public class EditCategoryActivity extends Activity
{
	private ImageView iconImageView;
	private ClearEditText nameEditText;
//	private EditText limitEditText;
//	private ToggleButton proveAheadToggleButton;
	private LinearLayout iconLayout;

	private DBManager dbManager;
	
	private List<Integer> iconList;
	private List<Boolean> checkList;
    private Category originalCategory;
	private Category category;
	
	private int iconSideLength;
	private int iconHorizontalInterval;
	private int iconVerticalInterval;
	private int iconMaxCount;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_edit_category);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditCategoryActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EditCategoryActivity");
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

        originalCategory = (Category) getIntent().getSerializableExtra("category");
        category = new Category(originalCategory);
		
		iconList = new ArrayList<>();
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
		
		checkList = new ArrayList<>();
		for (int i = 0; i < iconList.size(); i++)
		{
			checkList.add(false);
		}

		if (category.getIconID() > 0)
		{
			checkList.set(category.getIconID() - 1, true);
		}
		else if (category.getParentID() != 0)
		{
			Category parent = dbManager.getCategory(category.getParentID());
			if (parent.getIconID() > 0)
			{
				category.setIconID(parent.getIconID());
				checkList.set(parent.getIconID() - 1, true);				
			}
		}
	}
	
	private void initView()
	{
        int widthPixels = ViewUtils.getPhoneWindowWidth(this);
		int padding = ViewUtils.dpToPixel(32);
		iconSideLength = ViewUtils.dpToPixel(40);
		iconHorizontalInterval = ViewUtils.dpToPixel(20);
		iconMaxCount = (widthPixels - padding + iconHorizontalInterval) / (iconSideLength + iconHorizontalInterval);
		iconHorizontalInterval = (widthPixels - padding - iconSideLength * iconMaxCount) / (iconMaxCount - 1);
		iconVerticalInterval = ViewUtils.dpToPixel(20);
		
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
//				String limitString = limitEditText.getText().toString();
//                double limit = limitString.isEmpty()? 0 : Utils.stringToDouble(limitString);
//                boolean isProveAhead = proveAheadToggleButton.isChecked();
                int iconIndex = checkList.indexOf(true);
                int iconID = iconIndex == -1? -1 : iconIndex + 1;

                category.setName(name);
                category.setGroupID(AppPreference.getAppPreference().getCurrentGroupID());
//					category.setIsProveAhead(isProveAhead);
//                    category.setLimit(limit);
                category.setIconID(iconID);

                if (!PhoneUtils.isNetworkConnected() && category.getServerID() == -1)
                {
                    ViewUtils.showToast(EditCategoryActivity.this, R.string.error_add_network_unavailable);
                }
                else if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(EditCategoryActivity.this, R.string.error_modify_network_unavailable);
                }
				else if (name.isEmpty())
                {
                    ViewUtils.showToast(EditCategoryActivity.this, R.string.error_category_name_empty);
                }
				else if (category.getName().equals(originalCategory.getName()) && category.getIconID() == originalCategory.getIconID())
//                        && category.getLimit() == limit && category.isProveAhead() == isProveAhead)
                {
                    goBack();
                }
                else if (category.getServerID() == -1)
			    {
					sendCreateCategoryRequest();
				}
				else
				{
					sendModifyCategoryRequest();
				}
			}
		});
		
		iconImageView = (ImageView) findViewById(R.id.iconImageView);
		
		nameEditText = (ClearEditText) findViewById(R.id.nameEditText);
		
//		limitEditText = (EditText) findViewById(R.id.limitEditText);
//		limitEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
//
//		proveAheadToggleButton = (ToggleButton) findViewById(R.id.proveAheadToggleButton);

		iconLayout = (LinearLayout) findViewById(R.id.iconLayout);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		}); 
		
		if (category.getServerID() != -1)
		{
			nameEditText.setText(category.getName());
//			limitEditText.setText(Utils.formatDouble(category.getLimit()));
//			proveAheadToggleButton.setChecked(category.isProveAhead());
		}

		if (category.getIconID() > 0)
		{
			iconImageView.setImageResource(iconList.get(category.getIconID() - 1));
		}
		
		refreshIconLayout();       
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
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSideLength, iconSideLength);
			if (i % iconMaxCount != 0)
			{
				params.leftMargin = iconHorizontalInterval;
			}
			layout.addView(imageView, params);
		}
	}

	private void resetCheck()
	{
		for (int j = 0; j < checkList.size(); j++)
		{
			checkList.set(j, false);
		}
	}
	
	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
//		imm.hideSoftInputFromWindow(limitEditText.getWindowToken(), 0);
	}

	private void sendCreateCategoryRequest()
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditCategoryActivity.this, R.string.succeed_in_creating_category);
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
							ViewUtils.showToast(EditCategoryActivity.this, R.string.failed_to_create_category, response.getErrorMessage());
						}
					});
				}
			}
		});
	}

	private void sendModifyCategoryRequest()
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditCategoryActivity.this, R.string.succeed_in_modifying_category);
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
							ViewUtils.showToast(EditCategoryActivity.this, R.string.failed_to_modify_category, response.getErrorMessage());
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
