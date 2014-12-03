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
import android.widget.ImageView;
import android.widget.TextView;

public class ShowItemActivity extends Activity
{
	private DBManager dbManager;
	private Item item;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_show_item);
		MobclickAgent.onEvent(ShowItemActivity.this, "UMENG_VIEW_ITEM");
		initData();
		initView();
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
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
		dbManager = DBManager.getDBManager();
		Intent intent = this.getIntent();
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
		getActionBar().hide();
		ReimApplication.setProgressDialog(this);
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});

		TextView actualCostTextView = (TextView)findViewById(R.id.actualCostTextView);
		TextView budgetTextView = (TextView)findViewById(R.id.budgetTextView);
		ImageView approvedImageView = (ImageView)findViewById(R.id.approvedImageView);
		
		TextView amountTextView = (TextView)findViewById(R.id.amountTextView);
		amountTextView.setText(Utils.formatDouble(item.getAmount()));
		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		if (item.getStatus() == Item.STATUS_PROVE_AHEAD_APPROVED)
		{
			budgetTextView.setText(getString(R.string.budget) + " " + Utils.formatDouble(item.getPaAmount()));
		}
		else
		{
			actualCostTextView.setVisibility(View.GONE);
			budgetTextView.setVisibility(View.GONE);
			approvedImageView.setVisibility(View.GONE);
		}
		
		String temp = item.isProveAhead() ? getString(R.string.proveAhead) : getString(R.string.consumed);
		if (item.needReimbursed())
		{
			temp += "/" + getString(R.string.needReimburse);
		}
		TextView typeTextView = (TextView)findViewById(R.id.typeTextView);
		typeTextView.setText(temp);

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
		else if (item.getImageID() == -1)
		{
			invoiceImageView.setVisibility(View.GONE);
		}
		else
		{			
			invoiceImageView.setImageResource(R.drawable.default_invoice);
			if (item.getImageID() != -1 && item.getImageID() != 0 && Utils.isNetworkConnected())
			{
				sendDownloadImageRequest(invoiceImageView);
			}
			else if (item.getImageID() != -1 && item.getImageID() != 0 && !Utils.isNetworkConnected())
			{
				Utils.showToast(ShowItemActivity.this, "网络未连接，无法下载图片");				
			}
		}
	}
	
	private void sendDownloadImageRequest(final ImageView invoiceImageView)
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
						dbManager.updateItem(item);
						
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
								Utils.showToast(ShowItemActivity.this, "图片保存失败");
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
							Utils.showToast(ShowItemActivity.this, "图片下载失败");
						}
					});								
				}
			}
		});		
	}
}
