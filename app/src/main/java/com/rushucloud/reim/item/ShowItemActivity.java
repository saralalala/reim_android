package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.rushucloud.reim.MultipleImageActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import classes.Category;
import classes.Image;
import classes.Item;
import classes.utils.ReimApplication;
import classes.User;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;

public class ShowItemActivity extends Activity
{
	private LinearLayout invoiceLayout;
	private ImageView categoryImageView;
	private LinearLayout tagLayout;
	private LinearLayout memberLayout;
	
	private DBManager dbManager;
	private Item item;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reim_show);
		MobclickAgent.onEvent(ShowItemActivity.this, "UMENG_VIEW_ITEM");
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ShowItemActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
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
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});

		// init status part
		TextView actualCostTextView = (TextView) findViewById(R.id.actualCostTextView);
		TextView budgetTextView = (TextView) findViewById(R.id.budgetTextView);
		TextView approvedTextView = (TextView) findViewById(R.id.approvedTextView);
		
		TextView amountTextView = (TextView) findViewById(R.id.amountTextView);
		amountTextView.setText(Utils.formatDouble(item.getAmount()));
		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		if (item.isPaApproved())
		{
			budgetTextView.setText(getString(R.string.budget) + " " + Utils.formatDouble(item.getPaAmount()));
		}
		else
		{
			actualCostTextView.setVisibility(View.GONE);
			budgetTextView.setVisibility(View.GONE);
			approvedTextView.setVisibility(View.GONE);
		}
		
		// init type
		String temp = item.isProveAhead() ? getString(R.string.prove_ahead) : getString(R.string.consumed);
		if (item.needReimbursed())
		{
			temp += "/" + getString(R.string.need_reimburse);
		}
		TextView typeTextView = (TextView) findViewById(R.id.typeTextView);
		typeTextView.setText(temp);
		
		// init invoice photo
		invoiceLayout = (LinearLayout) findViewById(R.id.invoiceLayout);
		refreshInvoiceView();
		
		if (!PhoneUtils.isNetworkConnected())
		{
			ViewUtils.showToast(ShowItemActivity.this, R.string.error_download_invoice_network_unavailable);				
		}
		else
		{
			for (Image image : item.getInvoices())
			{
				if (image.isNotDownloaded() && PhoneUtils.isNetworkConnected())
				{
					sendDownloadInvoiceRequest(image);
				}
			}			
		}
		
		// init time
		TextView timeTextView = (TextView) findViewById(R.id.timeTextView);
		if (item.getConsumedDate() > 0)
		{
			timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));			
		}
		else
		{
			timeTextView.setText(R.string.not_available);
		}
		
		// init vendor		
		TextView vendorTextView = (TextView) findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getVendor());

		// init location
		String cityName = item.getLocation().isEmpty() ? getString(R.string.not_available) : item.getLocation();
		TextView locationTextView = (TextView) findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);

		// init category
		categoryImageView = (ImageView) findViewById(R.id.categoryImageView);
		TextView categoryTextView = (TextView) findViewById(R.id.categoryTextView);
		if (item.getCategory() != null)
		{
			if (!item.getCategory().getIconPath().isEmpty())
			{
				Bitmap categoryIcon = BitmapFactory.decodeFile(item.getCategory().getIconPath());
				if (categoryIcon != null)
				{
					categoryImageView.setImageBitmap(categoryIcon);
				}
			}
			categoryTextView.setText(item.getCategory().getName());
			
			if (item.getCategory().hasUndownloadedIcon() && PhoneUtils.isNetworkConnected())
			{
				sendDownloadCategoryIconRequest(item.getCategory());
			}
		}

		// init tag
		tagLayout = (LinearLayout) findViewById(R.id.tagLayout);
		refreshTagView();
		
		// init member
		memberLayout = (LinearLayout) findViewById(R.id.memberLayout);
		refreshMemberView();
		
		if (item.getRelevantUsers() != null && PhoneUtils.isNetworkConnected())
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
		TextView noteTextView = (TextView) findViewById(R.id.noteTextView);
		noteTextView.setText(item.getNote());		
	}
	
	private void refreshInvoiceView()
	{
		invoiceLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 96);
		int width = PhoneUtils.dpToPixel(getResources(), 30);
		int verticalPadding = PhoneUtils.dpToPixel(getResources(), 10);
		int horizontalPadding = PhoneUtils.dpToPixel(getResources(), 10);
		int maxCount = (layoutMaxLength + horizontalPadding) / (width + horizontalPadding);
		horizontalPadding = (layoutMaxLength - width * maxCount) / (maxCount - 1);

		LinearLayout layout = new LinearLayout(this);
		int invoiceCount = item.getInvoices() != null ? item.getInvoices().size() : 0;
		for (int i = 0; i < invoiceCount; i++)
		{
			if (i % maxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = verticalPadding;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				invoiceLayout.addView(layout);
			}
			
			final Bitmap bitmap = item.getInvoices().get(i).getBitmap();

			final int index = i;
			ImageView invoiceImageView = new ImageView(this);
			invoiceImageView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if (bitmap != null)
					{
						ArrayList<String> pathList = new ArrayList<String>();
						for (Image image : item.getInvoices())
						{
							if (!image.getPath().isEmpty())
							{
								pathList.add(image.getPath());
							}
						}
						
						int pageIndex = pathList.indexOf(item.getInvoices().get(index).getPath());
						
						Bundle bundle = new Bundle();
						bundle.putStringArrayList("imagePath", pathList);
						bundle.putInt("index", pageIndex);
						
						Intent intent = new Intent(ShowItemActivity.this, MultipleImageActivity.class);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				}
			});
			
			if (bitmap == null)
			{
				invoiceImageView.setImageResource(R.drawable.default_invoice);				
			}
			else
			{
				invoiceImageView.setImageBitmap(bitmap);
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
			if ((i + 1) % maxCount != 0)
			{
				params.rightMargin = horizontalPadding;				
			}
			
			layout.addView(invoiceImageView, params);
		}
	}
	
	private void refreshTagView()
	{
		tagLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 96);
		int verticalPadding = PhoneUtils.dpToPixel(getResources(), 17);
		int horizontalPadding = PhoneUtils.dpToPixel(getResources(), 10);
		int padding = PhoneUtils.dpToPixel(getResources(), 24);
		int textSize = PhoneUtils.dpToPixel(getResources(), 16);

		int space = 0;
		LinearLayout layout = new LinearLayout(this);
		int tagCount = item.getTags() != null ? item.getTags().size() : 0;
		for (int i = 0; i < tagCount; i++)
		{
			String name = item.getTags().get(i).getName();
			
			View view = View.inflate(this, R.layout.grid_item_tag, null);

			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(name);
			
			Paint textPaint = new Paint();
			textPaint.setTextSize(textSize);
			Rect textRect = new Rect();
			textPaint.getTextBounds(name, 0, name.length(), textRect);
			int width = textRect.width() + padding;
			
			if (space - width - horizontalPadding <= 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = verticalPadding;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				tagLayout.addView(layout);
				
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				layout.addView(view, params);
				space = layoutMaxLength - width;
			}
			else
			{
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.leftMargin = horizontalPadding;
				layout.addView(view, params);
				space -= width + horizontalPadding;
			}
		}
	}
	
	private void refreshMemberView()
	{
		memberLayout.removeAllViews();
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 96);
		int width = PhoneUtils.dpToPixel(getResources(), 50);
		int verticalPadding = PhoneUtils.dpToPixel(getResources(), 18);
		int horizontalPadding = PhoneUtils.dpToPixel(getResources(), 18);
		int maxCount = (layoutMaxLength + horizontalPadding) / (width + horizontalPadding);
		horizontalPadding = (layoutMaxLength - width * maxCount) / (maxCount - 1);

		LinearLayout layout = new LinearLayout(this);
		int memberCount = item.getRelevantUsers() != null ? item.getRelevantUsers().size() : 0;
		for (int i = 0; i < memberCount; i++)
		{
			if (i % maxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = verticalPadding;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				memberLayout.addView(layout);
			}
			
			User user = item.getRelevantUsers().get(i);
			
			View memberView = View.inflate(this, R.layout.grid_member, null);
			
			ImageView avatarImageView = (ImageView) memberView.findViewById(R.id.avatarImageView);			
			if (!user.getAvatarPath().isEmpty())
			{
				Bitmap avatar = BitmapFactory.decodeFile(user.getAvatarPath());
				if (avatar != null)
				{
					avatarImageView.setImageBitmap(avatar);		
				}				
			}
			
			TextView nameTextView = (TextView) memberView.findViewById(R.id.nameTextView);
			nameTextView.setText(user.getNickname());
			
			LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
			if ((i + 1) % maxCount != 0)
			{
				params.rightMargin = horizontalPadding;				
			}
			
			layout.addView(memberView, params);
		}
	}
	
	private void sendDownloadInvoiceRequest(final Image image)
	{
		DownloadImageRequest request = new DownloadImageRequest(image.getServerID(), DownloadImageRequest.INVOICE_QUALITY_ORIGINAL);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					final String invoicePath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_INVOICE);
					if (!invoicePath.isEmpty())
					{
						image.setPath(invoicePath);
						dbManager.updateImageByServerID(image);
						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								initData();
								refreshInvoiceView();
							}
						});
					}
					else
					{						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ViewUtils.showToast(ShowItemActivity.this, R.string.failed_to_save_invoice);
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
							ViewUtils.showToast(ShowItemActivity.this, R.string.failed_to_download_invoice);
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
					PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
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
					String avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							initData();
							refreshMemberView();
						}
					});	
				}
			}
		});
    }
}
