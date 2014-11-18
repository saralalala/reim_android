package com.rushucloud.reim;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;
import classes.Item;
import classes.ReimApplication;
import classes.Tag;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowItemActivity extends Activity
{
	private Item item;
	private boolean fromReim;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_show_item);
		MobclickAgent.onEvent(ShowItemActivity.this, "UMENG_VIEW_ITEM");
		initData();
		initView();
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ShowItemActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ShowItemActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBack();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
		DBManager dbManager = DBManager.getDBManager();
		Intent intent = this.getIntent();
		fromReim = intent.getBooleanExtra("fromReim", false);
		int itemID = intent.getIntExtra("itemLocalID", -1);
		if (itemID == -1)
		{
			itemID = intent.getIntExtra("othersItemServerID", -1);
			item = dbManager.getOthersItem(itemID);
			if (item == null)
			{
				item = new Item();
			}
		}
		else
		{
			item = dbManager.getItemByLocalID(itemID);
			if (item == null)
			{
				item = new Item();
			}			
		}
	}
	
	private void initView()
	{		
		CheckBox proveAheadCheckBox = (CheckBox)findViewById(R.id.proveAheadCheckBox);
		proveAheadCheckBox.setChecked(item.isProveAhead());
		
		CheckBox needReimCheckBox = (CheckBox)findViewById(R.id.needReimCheckBox);
		needReimCheckBox.setChecked(item.needReimbursed());
		
		TextView amountTextView = (TextView)findViewById(R.id.amountTextView);
		amountTextView.setText(Double.toString(item.getAmount()));
		
		TextView paAmountTextView = (TextView)findViewById(R.id.paAmountTextView);
		if (item.getPaAmount() != 0)
		{
			paAmountTextView.setText(getResources().getString(R.string.paAmount) + Double.toString(item.getPaAmount()));
		}
		else
		{
			paAmountTextView.setVisibility(View.GONE);
		}

		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		TextView categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		categoryTextView.setText(categoryName);
		
		TextView vendorTextView = (TextView)findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getMerchant());

		String cityName = item.getLocation().equals("") ? "N/A" : item.getLocation();
		TextView locationTextView = (TextView)findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);
		
		TextView tagTextView = (TextView)findViewById(R.id.tagTextView);
		tagTextView.setText(Tag.getTagsNameString(item.getTags()));
		
		TextView timeTextView = (TextView)findViewById(R.id.timeTextView);
		if (item.getConsumedDate() != -1 && item.getConsumedDate() != 0)
		{
			timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));			
		}
		else
		{
			timeTextView.setText(R.string.notAvailable);
		}
		
		TextView memberTextView = (TextView)findViewById(R.id.memberTextView);
		memberTextView.setText(User.getUsersNameString(item.getRelevantUsers()));
		
		TextView noteTextView = (TextView)findViewById(R.id.noteTextView);
		noteTextView.setText(item.getNote());
		
		final ImageView invoiceImageView = (ImageView)findViewById(R.id.invoiceImageView);
		invoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!item.getInvoicePath().equals(""))
				{
					Intent intent = new Intent(ShowItemActivity.this, ImageActivity.class);
					intent.putExtra("imagePath", item.getInvoicePath());
					startActivity(intent);
				}
			}
		});
		

		Bitmap bitmap = BitmapFactory.decodeFile(item.getInvoicePath());
		if (bitmap != null)
		{
			invoiceImageView.setImageBitmap(bitmap);
		}
		else
		{			
			invoiceImageView.setImageResource(R.drawable.default_invoice);
			if (item.getImageID() != -1 && item.getImageID() != 0)
			{
				DownloadImageRequest request = new DownloadImageRequest(item.getImageID(), DownloadImageRequest.INVOICE_QUALITY_ORIGINAL);
				request.sendRequest(new HttpConnectionCallback()
				{
					public void execute(Object httpResponse)
					{
						DownloadImageResponse response = new DownloadImageResponse(httpResponse);
						if (response.getBitmap() != null)
						{
							final String invoicePath = Utils.saveBitmapToFile(response.getBitmap(), 
																			HttpConstant.IMAGE_TYPE_INVOICE);
							if (!invoicePath.equals(""))
							{
								item.setInvoicePath(invoicePath);
								DBManager.getDBManager().updateItem(item);
								
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										Bitmap bitmap = BitmapFactory.decodeFile(invoicePath);
										invoiceImageView.setImageBitmap(bitmap);
									}
								});
							}
							else
							{						
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										Toast.makeText(ShowItemActivity.this, "图片保存失败", Toast.LENGTH_SHORT).show();
									}
								});						
							}
						}
						else
						{				
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									Toast.makeText(ShowItemActivity.this, "图片下载失败", Toast.LENGTH_SHORT).show();
								}
							});								
						}
					}
				});
			}	
		}
	}
	
	private void initButton()
	{		
		Button backButton = (Button)findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				goBack();
			}
		});
	}
	
	private void goBack()
	{
		if (fromReim)
		{
	    	ReimApplication.setTabIndex(0);
	    	Intent intent = new Intent(ShowItemActivity.this, MainActivity.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
	    	finish();
		}
		else
		{
			finish();
		}
	}
}
