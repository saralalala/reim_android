package com.rushucloud.reim;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;
import classes.Category;
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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ShowItemActivity extends Activity
{
	private ImageView invoiceImageView;
	private ImageView categoryImageView;
	private LinearLayout tagLayout;
	private LinearLayout memberLayout;
	
	private DBManager dbManager;
	private Item item;
	private int iconWidth;
	private int iconInterval;
	private int iconMaxCount;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_show);
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

		// init status part
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
		
		// init type
		String temp = item.isProveAhead() ? getString(R.string.prove_ahead) : getString(R.string.consumed);
		if (item.needReimbursed())
		{
			temp += "/" + getString(R.string.need_reimburse);
		}
		TextView typeTextView = (TextView)findViewById(R.id.typeTextView);
		typeTextView.setText(temp);
		
		// init invoice photo
		invoiceImageView = (ImageView)findViewById(R.id.invoiceImageView);
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
		
		Bitmap invoice = BitmapFactory.decodeFile(item.getInvoicePath());
		if (invoice != null)
		{
			invoiceImageView.setImageBitmap(invoice);
		}
		else if (!item.hasInvoice())
		{
			invoiceImageView.setVisibility(View.GONE);
		}
		else
		{			
			invoiceImageView.setImageResource(R.drawable.default_invoice);
			if (item.hasUndownloadedInvoice() && Utils.isNetworkConnected())
			{
				sendDownloadInvoiceRequest();
			}
			else if (item.hasUndownloadedInvoice() && !Utils.isNetworkConnected())
			{
				Utils.showToast(ShowItemActivity.this, "网络未连接，无法下载图片");				
			}
		}
		
		// init time
		TextView timeTextView = (TextView)findViewById(R.id.timeTextView);
		if (item.getConsumedDate() > 0)
		{
			timeTextView.setText(Utils.secondToStringUpToDay(item.getConsumedDate()));			
		}
		else
		{
			timeTextView.setText(R.string.notAvailable);
		}
		
		// init vendor		
		TextView vendorTextView = (TextView)findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getMerchant());

		// init location
		String cityName = item.getLocation().equals("") ? getString(R.string.notAvailable) : item.getLocation();
		TextView locationTextView = (TextView)findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);

		// init category
		categoryImageView = (ImageView) findViewById(R.id.categoryImageView);
		TextView categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		if (item.getCategory() != null)
		{
			Bitmap categoryIcon = BitmapFactory.decodeFile(item.getCategory().getIconPath());
			if (categoryIcon != null)
			{
				categoryImageView.setImageBitmap(categoryIcon);
			}
			categoryTextView.setText(item.getCategory().getName());
			
			if (item.getCategory().hasUndownloadedIcon() && Utils.isNetworkConnected())
			{
				sendDownloadCategoryIconRequest(item.getCategory());
			}
		}
		else
		{
			categoryImageView.setVisibility(View.GONE);
		}
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, metrics);
		iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		iconInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		iconMaxCount = (metrics.widthPixels - padding + iconInterval) / (iconWidth + iconInterval);

		// init tag
		refreshTagView();
		
		if (item.getTags() != null && Utils.isNetworkConnected())
		{
			for (Tag tag : item.getTags())
			{
				if (tag.hasUndownloadedIcon())
				{
					sendDownloadTagIconRequest(tag);
				}
			}
		}
		
		// init member
		refreshMemberView();
		
		if (item.getRelevantUsers() != null && Utils.isNetworkConnected())
		{
			for (User user : item.getRelevantUsers())
			{
				if (user.hasUndownloadedAvatar())
				{
					sendDownloadAvatarRequest(user);
				}
			}
		}
		
		// init note;
		TextView noteTextView = (TextView)findViewById(R.id.noteTextView);
		noteTextView.setText(item.getNote());		
	}
	
	private void refreshTagView()
	{
		initData();

		tagLayout = (LinearLayout) findViewById(R.id.tagLayout);
		tagLayout.removeAllViews();

		LinearLayout layout = new LinearLayout(this);
		int tagCount = item.getTags() != null ? item.getTags().size() : 0;
		for (int i = 0; i < tagCount; i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				tagLayout.addView(layout);
			}
			
			Tag tag = item.getTags().get(i);
			Bitmap tagIcon = BitmapFactory.decodeFile(tag.getIconPath());
			
			View tagView = View.inflate(this, R.layout.grid_tag, null);
			
			ImageView iconImageView = (ImageView) tagView.findViewById(R.id.iconImageView);
			if (tagIcon != null)
			{
				iconImageView.setImageBitmap(tagIcon);		
			}
			
			TextView nameTextView = (TextView) tagView.findViewById(R.id.nameTextView);
			nameTextView.setText(tag.getName());
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconInterval;
			
			layout.addView(tagView, params);
		}
	}
	
	private void refreshMemberView()
	{
		initData();
		
		memberLayout = (LinearLayout) findViewById(R.id.memberLayout);
		memberLayout.removeAllViews();

		LinearLayout layout = new LinearLayout(this);
		int memberCount = item.getRelevantUsers() != null ? item.getRelevantUsers().size() : 0;
		for (int i = 0; i < memberCount; i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				memberLayout.addView(layout);
			}
			
			User user = item.getRelevantUsers().get(i);
			Bitmap memberAvatar = BitmapFactory.decodeFile(user.getAvatarPath());
			
			View memberView = View.inflate(this, R.layout.grid_member, null);
			
			ImageView avatarImageView = (ImageView) memberView.findViewById(R.id.avatarImageView);
			if (memberAvatar != null)
			{
				avatarImageView.setImageBitmap(memberAvatar);		
			}
			
			TextView nameTextView = (TextView) memberView.findViewById(R.id.nameTextView);
			nameTextView.setText(user.getNickname());
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconInterval;
			
			layout.addView(memberView, params);
		}
	}
	
	private void sendDownloadInvoiceRequest()
	{
		DownloadImageRequest request = new DownloadImageRequest(item.getInvoiceID(), DownloadImageRequest.INVOICE_QUALITY_ORIGINAL);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DownloadImageResponse response = new DownloadImageResponse(httpResponse);
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
								invoiceImageView.setImageBitmap(response.getBitmap());
							}
						});
					}
					else
					{						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								Utils.showToast(ShowItemActivity.this, "发票图片下载失败");
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

    private void sendDownloadCategoryIconRequest(final Category category)
    {
    	DownloadImageRequest request = new DownloadImageRequest(category.getIconID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DownloadImageResponse response = new DownloadImageResponse(httpResponse);
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
							categoryImageView.setImageBitmap(response.getBitmap());
						}
					});	
				}
			}
		});
    }
    
    private void sendDownloadTagIconRequest(final Tag tag)
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
							refreshTagView();
						}
					});	
				}
			}
		});
    }

    private void sendDownloadAvatarRequest(final User user)
    {
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshMemberView();
						}
					});	
				}
			}
		});
    }
}
