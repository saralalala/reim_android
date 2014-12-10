package com.rushucloud.reim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.GetVendorsRequest;
import netUtils.Request.Item.ModifyItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Item.GetVendorsResponse;
import netUtils.Response.Item.ModifyItemResponse;
import netUtils.Response.Report.CreateReportResponse;
import classes.AppPreference;
import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Utils;
import classes.Adapter.CategoryListViewAdapter;
import classes.Adapter.LocationListViewAdapter;
import classes.Adapter.MemberListViewAdapter;
import classes.Adapter.TagListViewAdapter;

import cn.beecloud.BCLocation;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Selection;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import android.widget.TextView;

public class EditItemActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;

	private static AppPreference appPreference;
	private static DBManager dbManager;
	
	private EditText amountEditText;
	private PopupWindow typePopupWindow;
	private TextView typeTextView;
	private LinearLayout invoiceLayout;
	private ImageView invoiceImageView;
	private ImageView addInvoiceImageView;
	private ImageView removeImageView;
	private PopupWindow picturePopupWindow;
	private TextView timeTextView;
	private PopupWindow timePopupWindow;
	private DatePicker datePicker;
	private TextView vendorTextView;
	private TextView locationTextView;
	private AlertDialog locationDialog;
	private ImageView categoryImageView;
	private TextView categoryTextView;
	private LinearLayout tagLayout;
	private View addTagView;
	private LinearLayout memberLayout;
	private View addMemberView;
	private EditText noteEditText;
	
	private Item item;
	private Report report;
	
	private List<String> vendorList = null;
	private List<Category> categoryList = null;
	private List<Tag> tagList = null;
	private List<User> userList = null;

	private LocationListViewAdapter locationAdapter;
	private CategoryListViewAdapter categoryAdapter;
	private TagListViewAdapter tagAdapter;
	private MemberListViewAdapter memberAdapter;

	private boolean fromReim;
	private boolean newItem = false;
	
	private int iconWidth;
	private int iconInterval;
	private int iconMaxCount;
	
	private LocationClient locationClient = null;
	private BDLocationListener listener = new ReimLocationListener();
	private BDLocation currentLocation;
	private String locationInvalid;
	private boolean[] locationCheck;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_edit);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditItemActivity");		
		MobclickAgent.onResume(this);
		locationClient.registerLocationListener(listener);
		if (Utils.isLocalisationEnabled() && Utils.isNetworkConnected())
		{
			getLocation();
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EditItemActivity");
		MobclickAgent.onPause(this);
		locationClient.unRegisterLocationListener(listener);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && removeImageView.getVisibility() == View.VISIBLE)
		{
			removeImageView.setVisibility(View.INVISIBLE);
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(data != null)
		{
			try
			{
				if (requestCode == PICK_IMAGE || requestCode == TAKE_PHOTO)
				{
					Uri uri = data.getData();
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
					invoiceImageView.setImageBitmap(bitmap);
					
					String invoicePath = Utils.saveBitmapToFile(bitmap, HttpConstant.IMAGE_TYPE_INVOICE);
					if (!invoicePath.equals(""))
					{
						item.setInvoicePath(invoicePath);
						item.setInvoiceID(-1);
					}
					else
					{
						Utils.showToast(EditItemActivity.this, "图片保存失败");
					}
					
					refreshInvoiceView();
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				Utils.showToast(EditItemActivity.this, "图片保存失败");
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		locationClient = new LocationClient(getApplicationContext());
		
		vendorList = new ArrayList<String>();
		
		locationInvalid = getString(R.string.location_invalid);

		int currentGroupID = appPreference.getCurrentGroupID();
		categoryList = dbManager.getGroupCategories(currentGroupID);
		if (currentGroupID != -1)
		{
			tagList = dbManager.getGroupTags(currentGroupID);
			userList = dbManager.getGroupUsers(currentGroupID);
		}
		else
		{
			tagList = new ArrayList<Tag>();
			userList = new ArrayList<User>();
		}
		
		Intent intent = this.getIntent();
		fromReim = intent.getBooleanExtra("fromReim", false);
		int itemLocalID = intent.getIntExtra("itemLocalID", -1);
		if (itemLocalID == -1)
		{
			newItem = true;
			MobclickAgent.onEvent(this, "UMENG_NEW_ITEM");
			item = new Item();
			if (categoryList.size() > 0)
			{
				item.setCategory(categoryList.get(0));				
			}
			if (vendorList.size() > 0)
			{
				item.setMerchant(vendorList.get(0));
			}
			item.setConsumedDate(Utils.getCurrentTime());
		}
		else
		{
			newItem = false;
			MobclickAgent.onEvent(this, "UMENG_EDIT_ITEM");
			item = dbManager.getItemByLocalID(itemLocalID);			
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
		
		TextView saveTextView = (TextView)findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{			
			    	hideSoftKeyboard();
			    	double amount = Double.valueOf(amountEditText.getText().toString());
					DecimalFormat format = new DecimalFormat("#0.0");
					item.setAmount(Double.valueOf(format.format(amount)));
					item.setConsumer(appPreference.getCurrentUser());
					item.setNote(noteEditText.getText().toString());
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					
					if (fromReim && item.isProveAhead() && item.getPaAmount() == 0)
					{
						AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
											.setTitle("请选择操作")
											.setMessage("这是一条预审批的条目，您是想仅保存此条目还是要直接发送给上级审批？")
											.setPositiveButton(R.string.only_save, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													saveItem();												
												}
											})
											.setNeutralButton(R.string.send_to_approve, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													if (Utils.isNetworkConnected())
													{
														showManagerDialog();
													}
													else
													{
														Utils.showToast(EditItemActivity.this, "网络未连接，无法发送审批");
													}
												}
											})
											.setNegativeButton(R.string.cancel, null)
											.create();
						mDialog.show();
					}
					else
					{
						saveItem();
					}
				}
				catch (NumberFormatException e)
				{
					AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
														.setTitle("保存失败")
														.setMessage("数字输入格式不正确")
														.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																amountEditText.requestFocus();
															}
														})
														.create();
					mDialog.show();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		// init status part
		TextView actualCostTextView = (TextView)findViewById(R.id.actualCostTextView);
		TextView budgetTextView = (TextView)findViewById(R.id.budgetTextView);
		ImageView approvedImageView = (ImageView)findViewById(R.id.approvedImageView);

		amountEditText = (EditText)findViewById(R.id.amountEditText);
		amountEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountEditText.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					Spannable spanText = amountEditText.getText();
					Selection.setSelection(spanText, spanText.length());
				}
			}
		});
		if (item.getAmount() == 0)
		{
			amountEditText.requestFocus();
		}
		else
		{
			amountEditText.setText(Utils.formatDouble(item.getAmount()));
		}
		
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
		
		typeTextView = (TextView)findViewById(R.id.typeTextView);
		typeTextView.setText(temp);
		typeTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (fromReim && item.getStatus() != Item.STATUS_PROVE_AHEAD_APPROVED)
				{
					showTypeDialog();
				}
			}
		});
		
		// init type window
		View typeView = View.inflate(this, R.layout.reim_type_window, null);
		RadioButton consumedRadio = (RadioButton)typeView.findViewById(R.id.consumedRadio);
		final RadioButton proveAheadRadio = (RadioButton)typeView.findViewById(R.id.proveAheadRadio);
		proveAheadRadio.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked && newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_PROVEAHEAD");
				}
				if (isChecked && !newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_PROVEAHEAD");
				}
			}
		});
		
		consumedRadio.setChecked(!item.isProveAhead());
		proveAheadRadio.setChecked(item.isProveAhead());		
		
		final ToggleButton needReimToggleButton = (ToggleButton)typeView.findViewById(R.id.needReimToggleButton);
		needReimToggleButton.setChecked(item.needReimbursed());
		needReimToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked && newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_REIMBURSE");
				}
				if (isChecked && !newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_REIMBURSE");
				}
			}
		});

		TextView confirmTextView = (TextView) typeView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				item.setIsProveAhead(proveAheadRadio.isChecked());
				item.setNeedReimbursed(needReimToggleButton.isChecked());
				typePopupWindow.dismiss();
				
				String temp = item.isProveAhead() ? getString(R.string.prove_ahead) : getString(R.string.consumed);
				if (item.needReimbursed())
				{
					temp += "/" + getString(R.string.need_reimburse);
				}
				typeTextView.setText(temp);
			}
		});

		typePopupWindow = Utils.constructPopupWindow(this, typeView);
		
		// init invoice		
		invoiceLayout = (LinearLayout)findViewById(R.id.invoiceLayout);
		
		invoiceImageView = new ImageView(this);
		invoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				Intent intent = new Intent(EditItemActivity.this, ImageActivity.class);
				intent.putExtra("imagePath", item.getInvoicePath());
				startActivity(intent);
			}
		});		
		invoiceImageView.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				removeImageView.setVisibility(View.VISIBLE);
				return false;
			}
		});
		
		addInvoiceImageView = new ImageView(this);
		addInvoiceImageView.setImageResource(R.drawable.add_photo_button);
		addInvoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				showPictureDialog();
			}
		});

		removeImageView = (ImageView) findViewById(R.id.removeImageView);
		removeImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				removeImageView.setVisibility(View.INVISIBLE);
				item.setInvoiceID(-1);
				item.setInvoicePath("");
				refreshInvoiceView();
			}
		});
		
		refreshInvoiceView();
		
		// init picture window
		final View pictureView = View.inflate(this, R.layout.window_picture, null); 

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.window_button_unselected);
		final double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		final Button cameraButton = (Button) pictureView.findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				startActivityForResult(intent, TAKE_PHOTO);
			}
		});
		cameraButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				ViewGroup.LayoutParams params = cameraButton.getLayoutParams();
				params.height = (int)(cameraButton.getWidth() * ratio);;
				cameraButton.setLayoutParams(params);
			}
		});
		
		final Button galleryButton = (Button) pictureView.findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
				
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setType("image/*");
				startActivityForResult(intent, PICK_IMAGE);
			}
		});
		galleryButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				ViewGroup.LayoutParams params = galleryButton.getLayoutParams();
				params.height = (int)(galleryButton.getWidth() * ratio);;
				galleryButton.setLayoutParams(params);
			}
		});
		
		final Button cancelButton = (Button) pictureView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
			}
		});
		cancelButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				ViewGroup.LayoutParams params = cancelButton.getLayoutParams();
				params.height = (int)(cancelButton.getWidth() * ratio);;
				cancelButton.setLayoutParams(params);
			}
		});
		
		picturePopupWindow = Utils.constructPopupWindow(this, pictureView);
		
		// init time
		int time = item.getConsumedDate() > 0 ? item.getConsumedDate() : Utils.getCurrentTime();
		timeTextView = (TextView)findViewById(R.id.timeTextView);
		timeTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showTimeDialog();
			}
		});
		timeTextView.setText(Utils.secondToStringUpToDay(time));
		
		// init time window
		final View timeView = View.inflate(this, R.layout.window_date, null);
		
		final Button confirmButton = (Button) timeView.findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				timePopupWindow.dismiss();
				GregorianCalendar greCal = new GregorianCalendar(datePicker.getYear(), 
						datePicker.getMonth(), datePicker.getDayOfMonth());
				item.setConsumedDate((int)(greCal.getTimeInMillis() / 1000));
				timeTextView.setText(Utils.secondToStringUpToDay(item.getConsumedDate()));
			}
		});
		confirmButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_short_solid_dark);
				double ratio = ((double)bitmap.getWidth()) / bitmap.getHeight();
				ViewGroup.LayoutParams params = confirmButton.getLayoutParams();
				params.width = (int)(confirmButton.getHeight() * ratio);;
				confirmButton.setLayoutParams(params);
			}
		});
		
		datePicker = (DatePicker) timeView.findViewById(R.id.datePicker);
		
		timePopupWindow = Utils.constructPopupWindow(this, timeView);
		
		// init vendor		
		vendorTextView = (TextView)findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getMerchant());
		vendorTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MERCHANT");
				}
				else
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MERCHANT");
				}
						
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(EditItemActivity.this, "网络未连接，无法联网获取商家，请手动输入商家名称");
				}
				else if (currentLocation != null)
				{
	    			double latitude = currentLocation.getLatitude();
	    			double longitude = currentLocation.getLongitude();
	    			String category = item.getCategory() == null ? "" : item.getCategory().getName();
	    			sendVendorsRequest(category, latitude, longitude);
				}
				else if (!Utils.isLocalisationEnabled())
				{
					Utils.showToast(EditItemActivity.this, "定位服务不可用，请打开定位服务或手动输入商家名称");
				}
	    		else
	    		{
	    			Utils.showToast(EditItemActivity.this, "未获取到定位信息，请手动输入商家名或稍后再试");    	
	    		}
			}
		});

		// init location
		String cityName = item.getLocation().equals("") ? getString(R.string.notAvailable) : item.getLocation();
		locationTextView = (TextView)findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);
		locationTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				locationDialog.show();
			}
		});
		
		// init location dialog
		locationAdapter = new LocationListViewAdapter(this, item.getLocation());
		locationCheck = locationAdapter.getCheck();
		
    	View locationView = View.inflate(this, R.layout.reim_location, null);
    	final EditText locationEditText = (EditText) locationView.findViewById(R.id.locationEditText);
    	if (!item.getLocation().equals(""))
		{
        	locationEditText.setText(item.getLocation());			
		}

    	ListView locationListView = (ListView) locationView.findViewById(R.id.locationListView);
    	locationListView.setAdapter(locationAdapter);
    	locationListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (position == 0 && !locationAdapter.getCurrentCity().equals(locationInvalid))
				{
					locationEditText.setText(locationAdapter.getCurrentCity());
				}
				else if (position > 1)
				{
					locationEditText.setText(locationAdapter.getCityList().get(position - 2));
					for (int i = 0; i < locationCheck.length; i++)
					{
						locationCheck[i] = false;
					}
					locationCheck[position - 2] = true;
					locationAdapter.setCheck(locationCheck);
					locationAdapter.notifyDataSetChanged();
				}
			}
		});
    	
		Builder builder = new Builder(EditItemActivity.this);
		builder.setTitle(R.string.choose_location);
		builder.setView(locationView);
		builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which)
										{
											item.setLocation(locationEditText.getText().toString());
											locationTextView.setText(item.getLocation());
										}
									});
		builder.setNegativeButton(R.string.cancel, null);
		locationDialog = builder.create();
		
		// init category
		categoryImageView = (ImageView) findViewById(R.id.categoryImageView);
		categoryImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (item.getStatus() != Item.STATUS_PROVE_AHEAD_APPROVED)
				{
					if (newItem)
					{
						MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_CATEGORY");
					}
					else
					{
						MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_CATEGORY");
					}
					
					hideSoftKeyboard();
					showCategoryDialog();
				}				
			}
		});
		
		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		categoryTextView.setText(categoryName);
		categoryTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (item.getStatus() != Item.STATUS_PROVE_AHEAD_APPROVED)
				{
					if (newItem)
					{
						MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_CATEGORY");
					}
					else
					{
						MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_CATEGORY");
					}
					
					hideSoftKeyboard();
					showCategoryDialog();
				}				
			}
		});
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
		
		// init tag		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, metrics);
		iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		iconInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		iconMaxCount = (metrics.widthPixels - padding + iconInterval) / (iconWidth + iconInterval);
		
		tagLayout = (LinearLayout) findViewById(R.id.tagLayout);
		
		addTagView = View.inflate(this, R.layout.grid_tag, null);
		addTagView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TAG");
				}
				else
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TAG");
				}
				
				hideSoftKeyboard();
				if (tagList.size() > 0)
				{
					showTagDialog();
				}
				else
				{
					Utils.showToast(EditItemActivity.this, "当前组无任何标签");
				}														
			}
		});
		
		ImageView iconImageView = (ImageView) addTagView.findViewById(R.id.iconImageView);
		iconImageView.setImageResource(R.drawable.add_tag_button);
		
		refreshTagView();
		
		// init member		
		memberLayout = (LinearLayout) findViewById(R.id.memberLayout);
		
		addMemberView = View.inflate(this, R.layout.grid_member, null);
		addMemberView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MEMBER");
				}
				else
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MEMBER");
				}
				
				hideSoftKeyboard();
				if (userList.size() > 0)
				{
					showMemberDialog();
				}
				else
				{
					Utils.showToast(EditItemActivity.this, "当前组无任何其他成员");
				}											
			}
		});
		
		ImageView avatarImageView = (ImageView) addMemberView.findViewById(R.id.avatarImageView);
		avatarImageView.setImageResource(R.drawable.add_tag_button);
		
		refreshMemberView();
		
		// init note
		noteEditText = (EditText)findViewById(R.id.noteEditText);
		noteEditText.setText(item.getNote());
		noteEditText.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus && newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_NOTE");
				}
				if (hasFocus && !newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_NOTE");
				}
			}
		});
		
		LinearLayout baseLayout = (LinearLayout)findViewById(R.id.baseLayout);
		baseLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
	private void refreshInvoiceView()
	{
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		int sideLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, metrics);
		
		invoiceLayout.removeAllViews();
		
		if (!item.hasInvoice())
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
			invoiceLayout.addView(addInvoiceImageView, params);
		}
		else
		{
			Bitmap bitmap = BitmapFactory.decodeFile(item.getInvoicePath());
			if (bitmap != null)
			{
				invoiceImageView.setImageBitmap(bitmap);
			}			
			else // item has invoice path but the file was deleted
			{
				invoiceImageView.setImageResource(R.drawable.default_invoice);
				if (item.hasUndownloadedInvoice() && Utils.isNetworkConnected())
				{
					sendDownloadInvoiceRequest();
				}
				else if (item.hasUndownloadedInvoice() && !Utils.isNetworkConnected())
				{
					Utils.showToast(EditItemActivity.this, "网络未连接，无法下载图片");				
				}
			}
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
			params.rightMargin = padding;
			invoiceLayout.addView(invoiceImageView, params);
			
			params = new LinearLayout.LayoutParams(sideLength, sideLength);
			invoiceLayout.addView(addInvoiceImageView, params);			
		}
	}

	private void refreshTagView()
	{
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
		
		// add addTagView
		if (tagCount % iconMaxCount == 0)
		{
			layout = new LinearLayout(this);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.topMargin = iconInterval;
			layout.setLayoutParams(params);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			
			tagLayout.addView(layout);			
		}

		ViewGroup viewGroup = (ViewGroup) addTagView.getParent();
		if (viewGroup != null)
		{
			viewGroup.removeView(addTagView);
		}
		LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);		
		layout.addView(addTagView, params);		
	}

	private void refreshMemberView()
	{
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
			Bitmap avatar = BitmapFactory.decodeFile(user.getAvatarPath());
			
			View memberView = View.inflate(this, R.layout.grid_member, null);
			
			ImageView iconImageView = (ImageView) memberView.findViewById(R.id.iconImageView);
			if (avatar != null)
			{
				iconImageView.setImageBitmap(avatar);		
			}
			
			TextView nameTextView = (TextView) memberView.findViewById(R.id.nameTextView);
			nameTextView.setText(user.getNickname());
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconInterval;
			
			layout.addView(memberView, params);
		}
		
		// add addMemberView
		if (memberCount % iconMaxCount == 0)
		{
			layout = new LinearLayout(this);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.topMargin = iconInterval;
			layout.setLayoutParams(params);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			
			memberLayout.addView(layout);			
		}

		ViewGroup viewGroup = (ViewGroup) addMemberView.getParent();
		if (viewGroup != null)
		{
			viewGroup.removeView(addMemberView);
		}
		LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);		
		layout.addView(addMemberView, params);		
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(noteEditText.getWindowToken(), 0);  	
    }
 
    private void getLocation()
    {
    	LocationClientOption option = new LocationClientOption();
    	option.setLocationMode(LocationMode.Hight_Accuracy);
    	option.setScanSpan(5000);
    	option.setIsNeedAddress(false);
    	option.setNeedDeviceDirect(false);
    	locationClient.setLocOption(option);
    	locationClient.start();
    }
    
    private void saveItem()
    {
    	if (dbManager.syncItem(item))
		{
			Utils.showToast(EditItemActivity.this, "条目保存成功");
			finish();
		}
		else
		{
			Utils.showToast(EditItemActivity.this, "条目保存失败");
		}
    }
    
    private void showTypeDialog()
    {	
		typePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		typePopupWindow.update();
		
		Utils.dimBackground(this);
    }
    
    private void showPictureDialog()
    {
		picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		Utils.dimBackground(this);
    }
    
    private void showTimeDialog()
    {
		if (newItem)
		{
			MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TIME");
		}
		else
		{
			MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TIME");
		}
		
		Calendar calendar = Calendar.getInstance();
		if (item.getConsumedDate() <= 0)
		{
			calendar.setTimeInMillis(System.currentTimeMillis());
		}
		else
		{
			calendar.setTimeInMillis((long)item.getConsumedDate() * 1000);
		}

		datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
		
		timePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		timePopupWindow.update();

		Utils.dimBackground(this);
    }

    private void showVendorDialog()
    {
    	final String vendor = item.getMerchant();
		int index = vendorList.indexOf(vendor);
		if (index == -1)
		{
			index = 0;
			item.setMerchant(vendorList.get(0));
		}
		String[] vendors = vendorList.toArray(new String[vendorList.size()]);
		AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
											.setTitle(R.string.choose_vendor)
											.setSingleChoiceItems(vendors, index, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													item.setMerchant(vendorList.get(which));
												}
											})
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													vendorTextView.setText(item.getMerchant());
												}
											})
											.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													item.setMerchant(vendor);
												}
											})
											.create();
		mDialog.show();
    }

    private void showCategoryDialog()
    {
		final boolean[] check = Category.getCategoryCheck(categoryList, item.getCategory());
		
		categoryAdapter = new CategoryListViewAdapter(this, categoryList, check);
    	View view = View.inflate(this, R.layout.me_category, null);
    	ListView categoryListView = (ListView) view.findViewById(R.id.categoryListView);
    	categoryListView.setAdapter(categoryAdapter);
    	categoryListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{				
				for (int i = 0; i < check.length; i++)
				{
					check[i] = false;
				}
				check[position] = true;
				categoryAdapter.setCheck(check);
				categoryAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle(R.string.choose_category)
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										boolean flag = false;
										for (int i = 0; i < check.length; i++)
										{
											if (check[i])
											{
												item.setCategory(categoryList.get(i));
												flag = true;
												break;
											}
										}
										if (!flag)
										{
											item.setCategory(null);
										}

										categoryTextView.setText(item.getCategory().getName());
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
		
		if (Utils.isNetworkConnected())
		{
			for (Category category : categoryList)
			{
				if (category.hasUndownloadedIcon())
				{
					sendDownloadCategoryIconRequest(category);
				}
			}
		}
    }
    
    private void showTagDialog()
    {
		final boolean[] check = Tag.getTagsCheck(tagList, item.getTags());
		
		tagAdapter = new TagListViewAdapter(this, tagList, check);
    	View view = View.inflate(this, R.layout.me_tag, null);
    	ListView tagListView = (ListView) view.findViewById(R.id.tagListView);
    	tagListView.setAdapter(tagAdapter);
    	tagListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				check[position] = !check[position];
				tagAdapter.setCheck(check);
				tagAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle(R.string.choose_tag)
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<Tag> tags = new ArrayList<Tag>();
										for (int i = 0; i < tagList.size(); i++)
										{
											if (check[i])
											{
												tags.add(tagList.get(i));
											}
										}
										item.setTags(tags);
										refreshTagView();
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
		
		if (Utils.isNetworkConnected())
		{
			for (Tag tag : tagList)
			{
				if (tag.hasUndownloadedIcon())
				{
					sendDownloadTagIconRequest(tag);
				}
			}
		}
    }
    
    private void showMemberDialog()
    {
		final boolean[] check = User.getUsersCheck(userList, item.getRelevantUsers());
		
		memberAdapter = new MemberListViewAdapter(EditItemActivity.this, userList, check);
    	View view = View.inflate(EditItemActivity.this, R.layout.me_member, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				check[position] = !check[position];
				memberAdapter.setCheck(check);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
    							.setTitle(R.string.choose_member)
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> users = new ArrayList<User>();
										for (int i = 0; i < userList.size(); i++)
										{
											if (check[i])
											{
												users.add(userList.get(i));
											}
										}
										item.setRelevantUsers(users);
										refreshMemberView();
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
    }
    
    private void showManagerDialog()
    {
		final List<User> memberList = User.removeCurrentUserFromList(userList);
		final boolean[] check = User.getUsersCheck(memberList, appPreference.getCurrentUser().constructListWithManager());
		
		final MemberListViewAdapter memberAdapter = new MemberListViewAdapter(this, memberList, check);
    	View view = View.inflate(this, R.layout.me_member, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				check[position] = !check[position];
				memberAdapter.setCheck(check);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle(R.string.choose_manager)
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> managerList = new ArrayList<User>();
										for (int i = 0; i < check.length; i++)
										{
											if (check[i])
											{
												managerList.add(memberList.get(i));
											}
										}

										if (managerList.size() == 0)
										{
											Utils.showToast(EditItemActivity.this, "未选择汇报对象");
										}
										else
										{
											report = new Report();
									    	report.setTitle("预审批的报告");
									    	report.setStatus(Report.STATUS_SUBMITTED);
									    	report.setSender(appPreference.getCurrentUser());
									    	report.setCreatedDate(Utils.getCurrentTime());									    	
											report.setManagerList(managerList);
											report.setIsProveAhead(true);
									    	dbManager.insertReport(report);
									    	report.setLocalID(dbManager.getLastInsertReportID());					

											ReimApplication.showProgressDialog();
											if (newItem)
											{
												if (!item.getInvoicePath().equals("") && item.getServerID() == -1)
												{
													sendUploadImageRequest();
												}
												else
												{
													sendCreateItemRequest();													
												}
											}
											else
											{
												if (!item.getInvoicePath().equals("") && item.getServerID() == -1)
												{
													sendUploadImageRequest();
												}
												else
												{
													sendModifyItemRequest();													
												}
											}											
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
    }

    private void sendDownloadInvoiceRequest()
    {
		DownloadImageRequest request = new DownloadImageRequest(item.getInvoiceID(), DownloadImageRequest.INVOICE_QUALITY_ORIGINAL);
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
								Utils.showToast(EditItemActivity.this, "图片保存失败");
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
							Utils.showToast(EditItemActivity.this, "图片下载失败");
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
							if (categoryAdapter != null)
							{
								categoryAdapter.setCategory(categoryList);
								categoryAdapter.notifyDataSetChanged();								
							}
							
							if (item.getCategory() != null && item.getCategory().getServerID() == category.getServerID())
							{
								item.setCategory(category);
								Bitmap categoryIcon = BitmapFactory.decodeFile(item.getCategory().getIconPath());
								if (categoryIcon != null)
								{
									categoryImageView.setImageBitmap(categoryIcon);
								}
							}
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
							tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
							tagAdapter.setTag(tagList);
							tagAdapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
        
    private void sendUploadImageRequest()
    {
		UploadImageRequest request = new UploadImageRequest(item.getInvoicePath(), HttpConstant.IMAGE_TYPE_INVOICE);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
					item.setInvoiceID(response.getImageID());
					DBManager.getDBManager().updateItem(item);
					
					if (newItem)
					{
						sendCreateItemRequest();
					}
					else
					{
						sendModifyItemRequest();
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditItemActivity.this, "上传图片失败");
						}
					});
				}
			}
		});
    }
    
    private void sendCreateItemRequest()
    {
    	CreateItemRequest request = new CreateItemRequest(item);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateItemResponse response = new CreateItemResponse(httpResponse);
				if (response.getStatus())
				{
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					item.setServerID(response.getItemID());
					item.setCreatedDate(response.getCreateDate());
					
					dbManager.insertItem(item);
					item.setLocalID(dbManager.getLastInsertItemID());
					newItem = false;
					sendApproveReportRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditItemActivity.this, "创建条目失败");
						}
					});
				}
			}
		});
    }
    
    private void sendModifyItemRequest()
    {
    	ModifyItemRequest request = new ModifyItemRequest(item);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyItemResponse response = new ModifyItemResponse(httpResponse);
				if (response.getStatus())
				{
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					item.setServerUpdatedDate(item.getLocalUpdatedDate());
					dbManager.updateItem(item);
					sendApproveReportRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditItemActivity.this, "修改条目失败");
						}
					});			
				}
			}
		});
    }
    
    private void sendApproveReportRequest()
    {    	
    	item.setBelongReport(report);
    	dbManager.updateItemByServerID(item);   	
    	
    	CreateReportRequest request = new CreateReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					report.setServerID(response.getReportID());
					dbManager.updateReportByLocalID(report);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditItemActivity.this, "创建审批报告成功");
							finish();
						}
					});					
				}
				else
				{
					dbManager.deleteReport(report.getLocalID());
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(EditItemActivity.this, "创建审批报告失败");
							finish();
						}
					});								
				}
			}
		});
    }
    
    private void sendVendorsRequest(String category, double latitude, double longitude)
    {
    	ReimApplication.showProgressDialog();
		GetVendorsRequest request = new GetVendorsRequest(category, latitude, longitude);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetVendorsResponse response = new GetVendorsResponse(httpResponse);
				if (response.getStatus())
				{
					vendorList = response.getVendorList();
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							if (vendorList.size() > 0)
							{
								showVendorDialog();
							}
							else 
							{
								Utils.showToast(EditItemActivity.this, "未获取到任何商家, 请手动输入");								
							}
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
							Utils.showToast(EditItemActivity.this, "获取商家列表失败, 请手动输入");
						}
					});					
				}
			}
		});
    }

    private void sendLocationRequest(final double latitude, final double longitude)
    {
    	final BCLocation address = BCLocation.locationWithLatitude(latitude, longitude);
    	new Thread(new Runnable()
		{
			public void run()
			{
		    	locationAdapter.setCurrentCity(address.getCity());
		    	runOnUiThread(new Runnable()
				{
					public void run()
					{
				    	locationAdapter.notifyDataSetChanged();						
					}
				});
			}
		}).start();
    }

    public class ReimLocationListener implements BDLocationListener
    {
    	public void onReceiveLocation(BDLocation location)
    	{
    		if (location != null)
    		{
    			currentLocation = location;
    			locationClient.stop();
    			if (Utils.isNetworkConnected())
				{
        			sendLocationRequest(currentLocation.getLatitude(), currentLocation.getLongitude());			
				}
    		}
    	}
    }
}