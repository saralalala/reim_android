package com.rushucloud.reim.item;

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
import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Vendor;
import classes.Adapter.CategoryExpandableListAdapter;
import classes.Adapter.LocationListViewAdapter;
import classes.Adapter.MemberListViewAdapter;
import classes.Adapter.TagListViewAdapter;
import classes.Adapter.VendorListViewAdapter;
import classes.Utils.AppPreference;
import classes.Utils.Utils;

import cn.beecloud.BCLocation;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Selection;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
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

	private LocationListViewAdapter locationAdapter;
	private CategoryExpandableListAdapter categoryAdapter;
	private VendorListViewAdapter vendorAdapter;
	private TagListViewAdapter tagAdapter;
	private MemberListViewAdapter memberAdapter;
	
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
	private PopupWindow vendorPopupWindow;
	
	private TextView locationTextView;
	private PopupWindow locationPopupWindow;
	
	private ImageView categoryImageView;
	private TextView categoryTextView;
	private PopupWindow categoryPopupWindow;
	
	private LinearLayout tagLayout;
	private ImageView addTagImageView;
	private PopupWindow tagPopupWindow;
	
	private LinearLayout memberLayout;
	private ImageView addMemberImageView;
	private PopupWindow memberPopupWindow;
	
	private EditText noteEditText;
	
	private PopupWindow managerPopupWindow;

	private static AppPreference appPreference;
	private static DBManager dbManager;
	
	private Item item;
	private Report report;
	
	private List<Vendor> vendorList = null;
	private List<Category> categoryList = null;
	private List<List<Category>> subCategoryList = null;
	private List<Boolean> check = null;
	private List<List<Boolean>> subCheck = null;
	private List<Tag> tagList = null;
	private List<User> userList = null;

	private boolean fromReim;
	private boolean newItem = false;
		
	private LocationClient locationClient = null;
	private BDLocationListener listener = new ReimLocationListener();
	private BDLocation currentLocation;
	private boolean[] locationCheck;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reim_edit);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditItemActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
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
		if(resultCode == Activity.RESULT_OK)
		{
			try
			{
				if (requestCode == PICK_IMAGE)
				{
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
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
				else if (requestCode == TAKE_PHOTO)
				{
					Bitmap bitmap = BitmapFactory.decodeFile(appPreference.getTempInvoicePath());
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

		int currentGroupID = appPreference.getCurrentGroupID();
		
		initCategoryList();
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
			item.setConsumedDate(Utils.getCurrentTime());
			List<User> relevantUsers = new ArrayList<User>();
			relevantUsers.add(appPreference.getCurrentUser());
			item.setRelevantUsers(relevantUsers);
		}
		else
		{
			newItem = false;
			MobclickAgent.onEvent(this, "UMENG_EDIT_ITEM");
			item = dbManager.getItemByLocalID(itemLocalID);			
		}
	}
	
	private void initCategoryList()
	{
		int currentGroupID = appPreference.getCurrentGroupID();
		if (categoryList == null)
		{
			categoryList = dbManager.getGroupCategories(currentGroupID);			
		}
		else
		{
			categoryList.clear();
			categoryList.addAll(dbManager.getGroupCategories(currentGroupID));
		}

		if (subCategoryList == null)
		{
			subCategoryList = new ArrayList<List<Category>>();
		}
		else
		{
			subCategoryList.clear();
		}
		
		for (Category category : categoryList)
		{
			List<Category> subCategories = dbManager.getSubCategories(category.getServerID(), currentGroupID);
			subCategoryList.add(subCategories);
		}
	}
	
	private void resetCheck()
	{
		for (int i = 0; i < check.size(); i++)
		{
			check.set(i, false);
		}
		
		for (List<Boolean> booleans : subCheck)
		{
			for (int i = 0; i < booleans.size(); i++)
			{
				booleans.set(i, false);
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
						Builder buider = new Builder(EditItemActivity.this);
						buider.setTitle(R.string.option);
						buider.setMessage(R.string.prompt_save_prove_ahead_item);
						buider.setPositiveButton(R.string.only_save, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													saveItem();												
												}
											});
						buider.setNeutralButton(R.string.send_to_approve, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													if (Utils.isNetworkConnected())
													{
														showManagerWindow();
													}
													else
													{
														Utils.showToast(EditItemActivity.this, "网络未连接，无法发送审批");
													}
												}
											});
						buider.setNegativeButton(R.string.cancel, null);
						buider.create().show();
					}
					else
					{
						saveItem();
					}
				}
				catch (NumberFormatException e)
				{
					Utils.showToast(EditItemActivity.this, "数字输入格式不正确");
					amountEditText.requestFocus();
				}
				catch (Exception e)
				{
					e.printStackTrace();
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
		
		initStatusView();
		initTypeView();
		initInvoiceView();
		initTimeView();
		initVendorView();
		initLocationView();
		initCategoryView();
		initTagView();
		initMemberView();
		initNoteView();
		initManagerView();
	}
	
	private void initStatusView()
	{
		TextView actualCostTextView = (TextView)findViewById(R.id.actualCostTextView);
		TextView budgetTextView = (TextView)findViewById(R.id.budgetTextView);
		TextView approvedTextView = (TextView)findViewById(R.id.approvedTextView);

		amountEditText = (EditText)findViewById(R.id.amountEditText);
		amountEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
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
			approvedTextView.setVisibility(View.GONE);
		}
	}
	
	private void initTypeView()
	{
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
					hideSoftKeyboard();
					showTypeWindow();
				}
			}
		});
		
		// init type window
		View typeView = View.inflate(this, R.layout.window_reim_type, null);
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
				typePopupWindow.dismiss();
				
				item.setIsProveAhead(proveAheadRadio.isChecked());
				item.setNeedReimbursed(needReimToggleButton.isChecked());
				
				String temp = item.isProveAhead() ? getString(R.string.prove_ahead) : getString(R.string.consumed);
				if (item.needReimbursed())
				{
					temp += "/" + getString(R.string.need_reimburse);
				}
				typeTextView.setText(temp);
			}
		});

		typePopupWindow = Utils.constructBottomPopupWindow(this, typeView);
	}
	
	private void initInvoiceView()
	{		
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
		addInvoiceImageView.setImageResource(R.drawable.add_tag_button);
		addInvoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				showPictureWindow();
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
		View pictureView = View.inflate(this, R.layout.window_picture, null); 
		
		Button cameraButton = (Button) pictureView.findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempInvoiceUri());
				startActivityForResult(intent, TAKE_PHOTO);
			}
		});
		cameraButton = Utils.resizeWindowButton(cameraButton);
		
		Button galleryButton = (Button) pictureView.findViewById(R.id.galleryButton);
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
		galleryButton = Utils.resizeWindowButton(galleryButton);
		
		Button cancelButton = (Button) pictureView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
			}
		});
		cancelButton = Utils.resizeWindowButton(cancelButton);
		
		picturePopupWindow = Utils.constructBottomPopupWindow(this, pictureView);		
	}
	
	private void initTimeView()
	{
		// init time
		int time = item.getConsumedDate() > 0 ? item.getConsumedDate() : Utils.getCurrentTime();
		timeTextView = (TextView)findViewById(R.id.timeTextView);
		timeTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showTimeWindow();
			}
		});
		timeTextView.setText(Utils.secondToStringUpToDay(time));
		
		// init time window
		View timeView = View.inflate(this, R.layout.window_reim_date, null);
		
		Button confirmButton = (Button) timeView.findViewById(R.id.confirmButton);
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
		confirmButton = Utils.resizeShortButton(confirmButton, 30);
		
		datePicker = (DatePicker) timeView.findViewById(R.id.datePicker);
		
		timePopupWindow = Utils.constructBottomPopupWindow(this, timeView);
	}
	
	private void initVendorView()
	{
		// init vendor		
		vendorTextView = (TextView)findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getVendor());
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
				
				showVendorWindow();
			}
		});
		
		// init vendor window
		View vendorView = View.inflate(this, R.layout.window_reim_vendor, null);
		
		final EditText vendorEditText = (EditText) vendorView.findViewById(R.id.vendorEditText);
		vendorEditText.setText(item.getVendor());
		vendorEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		vendorAdapter = new VendorListViewAdapter(this);
		ListView vendorListView = (ListView) vendorView.findViewById(R.id.vendorListView);
		vendorListView.setAdapter(vendorAdapter);
		vendorListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Vendor vendor = vendorAdapter.getItem(position);
				vendorEditText.setText(vendor.getName());
			}
		});
		
		ImageView backImageView = (ImageView) vendorView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				vendorPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) vendorView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(vendorEditText.getWindowToken(), 0);
				
				vendorPopupWindow.dismiss();
				
				item.setVendor(vendorEditText.getText().toString());
				vendorTextView.setText(item.getVendor());
			}
		});

		vendorPopupWindow = Utils.constructHorizontalPopupWindow(this, vendorView);
	}
	
	private void initLocationView()
	{		
		// init location
		String cityName = item.getLocation().equals("") ? getString(R.string.no_location) : item.getLocation();
		locationTextView = (TextView)findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);
		locationTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				showLocationWindow();
			}
		});

		// init location window
		View locationView = View.inflate(this, R.layout.window_reim_location, null);
		
		final EditText locationEditText = (EditText) locationView.findViewById(R.id.locationEditText);
		locationEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
    	if (!item.getLocation().equals(""))
		{
        	locationEditText.setText(item.getLocation());			
		}

		locationAdapter = new LocationListViewAdapter(this, item.getLocation());
		locationCheck = locationAdapter.getCheck();
		
		ListView locationListView = (ListView) locationView.findViewById(R.id.locationListView);
		locationListView.setAdapter(locationAdapter);
		locationListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				for (int i = 0; i < locationCheck.length; i++)
				{
					locationCheck[i] = false;
				}
				
				if (position == 0 && !locationAdapter.getCurrentCity().equals(getString(R.string.no_location)))
				{
					locationEditText.setText(locationAdapter.getCurrentCity());
				}
				else if (position > 1)
				{
					locationEditText.setText(locationAdapter.getCityList().get(position - 2));
					locationCheck[position - 2] = true;
					locationAdapter.setCheck(locationCheck);
					locationAdapter.notifyDataSetChanged();
				}
			}
		});
		
		ImageView backImageView = (ImageView) locationView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				locationPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) locationView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(locationEditText.getWindowToken(), 0);
				locationPopupWindow.dismiss();
				
				item.setLocation(locationEditText.getText().toString());
				locationTextView.setText(item.getLocation());
			}
		});

		locationPopupWindow = Utils.constructHorizontalPopupWindow(this, locationView);
	}
	
	private void initCategoryView()
	{
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
					showCategoryWindow();
				}				
			}
		});
		
		String categoryName = item.getCategory() == null ? getString(R.string.not_available) : item.getCategory().getName();
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
					showCategoryWindow();
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
		
		// init category window
		check = Category.getCategoryCheck(categoryList, item.getCategory());
		subCheck = new ArrayList<List<Boolean>>();
		for (List<Category> categories : subCategoryList)
		{
			subCheck.add(Category.getCategoryCheck(categories, item.getCategory()));
		}
		
		categoryAdapter = new CategoryExpandableListAdapter(this, categoryList, subCategoryList, check, subCheck);
    	View categoryView = View.inflate(this, R.layout.window_reim_category, null);
    	ExpandableListView categoryListView = (ExpandableListView) categoryView.findViewById(R.id.categoryListView);
    	categoryListView.setAdapter(categoryAdapter);
    	categoryListView.setOnGroupClickListener(new OnGroupClickListener()
		{
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
			{
				resetCheck();
				check.set(groupPosition, true);
				categoryAdapter.setCheck(check, subCheck);
				categoryAdapter.notifyDataSetChanged();
				return false;
			}
		});
    	categoryListView.setOnChildClickListener(new OnChildClickListener()
		{
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id)
			{
				resetCheck();
				subCheck.get(groupPosition).set(childPosition, true);
				categoryAdapter.setCheck(check, subCheck);
				categoryAdapter.notifyDataSetChanged();
				return false;
			}
		});
		    	
		ImageView backImageView = (ImageView) categoryView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				categoryPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) categoryView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				categoryPopupWindow.dismiss();
				
				boolean flag = false;
				for (int i = 0; i < check.size(); i++)
				{
					if (check.get(i))
					{
						item.setCategory(categoryList.get(i));
						flag = true;
						break;
					}
				}
				
				if (!flag)
				{
					for (int i = 0; i < subCheck.size(); i++)
					{
						List<Boolean> booleans = subCheck.get(i);
						for (int j = 0; j < booleans.size(); j++)
						{
							if (booleans.get(j))
							{
								item.setCategory(subCategoryList.get(i).get(j));
								flag = true;
								break;
							}
						}
						if (flag)
						{
							break;
						}
					}
				}
				
				if (!flag)
				{
					item.setCategory(null);
				}
				else
				{
					categoryTextView.setText(item.getCategory().getName());
					Bitmap bitmap = BitmapFactory.decodeFile(item.getCategory().getIconPath());
					if (bitmap != null)
					{
						categoryImageView.setImageBitmap(bitmap);
					}
					else
					{
						categoryImageView.setImageResource(R.drawable.default_icon);
					}
				}
			}
		});

		categoryPopupWindow = Utils.constructHorizontalPopupWindow(this, categoryView);	
	}
	
	private void initTagView()
	{
		// init tag
		tagLayout = (LinearLayout) findViewById(R.id.tagLayout);
		
		addTagImageView = (ImageView) findViewById(R.id.addTagImageView);
		addTagImageView.setOnClickListener(new View.OnClickListener()
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
					showTagWindow();
				}
				else
				{
					Utils.showToast(EditItemActivity.this, "当前组无任何标签");
				}														
			}
		});
				
		refreshTagView();

		// init tag window
		final boolean[] check = Tag.getTagsCheck(tagList, item.getTags());
		
		tagAdapter = new TagListViewAdapter(this, tagList, check);
    	View tagView = View.inflate(this, R.layout.window_reim_tag, null);
    	ListView tagListView = (ListView) tagView.findViewById(R.id.tagListView);
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
		
		ImageView backImageView = (ImageView) tagView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				tagPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) tagView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				tagPopupWindow.dismiss();
				
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
		});

		tagPopupWindow = Utils.constructHorizontalPopupWindow(this, tagView);	
	}
	
	private void initMemberView()
	{	
		// init member
		memberLayout = (LinearLayout) findViewById(R.id.memberLayout);

		addMemberImageView = (ImageView) findViewById(R.id.addMemberImageView);
		addMemberImageView.setOnClickListener(new View.OnClickListener()
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
					showMemberWindow();
				}
				else
				{
					Utils.showToast(EditItemActivity.this, R.string.no_member);
				}											
			}
		});
		
		refreshMemberView();	

		// init member window
		final boolean[] check = User.getUsersCheck(userList, item.getRelevantUsers());
		
		memberAdapter = new MemberListViewAdapter(EditItemActivity.this, userList, check);
    	View memberView = View.inflate(EditItemActivity.this, R.layout.window_reim_member, null);
    	ListView userListView = (ListView) memberView.findViewById(R.id.userListView);
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
		
		ImageView backImageView = (ImageView) memberView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				memberPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) memberView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				memberPopupWindow.dismiss();
				
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
		});

		memberPopupWindow = Utils.constructHorizontalPopupWindow(this, memberView);	
	}

	private void initNoteView()
	{
		noteEditText = (EditText)findViewById(R.id.noteEditText);
		noteEditText.setText(item.getNote());
		noteEditText.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				Spannable spanText = ((EditText)v).getText();
				Selection.setSelection(spanText, spanText.length());
				
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
	}
	
	private void initManagerView()
	{
		final List<User> memberList = User.removeCurrentUserFromList(userList);
		final boolean[] check = User.getUsersCheck(memberList, appPreference.getCurrentUser().constructListWithManager());
		
		final MemberListViewAdapter memberAdapter = new MemberListViewAdapter(this, memberList, check);
    	View managerView = View.inflate(this, R.layout.window_reim_manager, null);
    	ListView userListView = (ListView) managerView.findViewById(R.id.userListView);
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

		ImageView backImageView = (ImageView) managerView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				managerPopupWindow.dismiss();
			}
		});
		
		TextView confirmTextView = (TextView) managerView.findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				managerPopupWindow.dismiss();
				
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
			    	report.setTitle(getString(R.string.report_prove_ahead));
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
		});

		managerPopupWindow = Utils.constructHorizontalPopupWindow(this, managerView);	
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

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 126, metrics);
		int tagVerticalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		int tagHorizontalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, metrics);
		int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics);
		
		int space = 0;
		LinearLayout layout = new LinearLayout(this);
		int tagCount = item.getTags() != null ? item.getTags().size() : 0;
		for (int i = 0; i < tagCount; i++)
		{
			String name = item.getTags().get(i).getName();
			
			View view = View.inflate(this, R.layout.grid_tag, null);

			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(name);
			
			Paint textPaint = new Paint();
			textPaint.setTextSize(textSize);
			Rect textRect = new Rect();
			textPaint.getTextBounds(name, 0, name.length(), textRect);
			int width = textRect.width() + padding;
			
			if (space - width - tagHorizontalInterval <= 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = tagVerticalInterval;
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
				params.leftMargin = tagHorizontalInterval;
				layout.addView(view, params);
				space -= width + tagHorizontalInterval;
			}
		}
		
	}

	private void refreshMemberView()
	{
		memberLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 126, metrics);
		int iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		int iconVerticalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		int iconHorizontalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		int iconMaxCount = (layoutMaxLength + iconHorizontalInterval) / (iconWidth + iconHorizontalInterval);
		iconHorizontalInterval = (layoutMaxLength - iconWidth * iconMaxCount) / (iconMaxCount - 1);
		
		LinearLayout layout = new LinearLayout(this);
		int memberCount = item.getRelevantUsers() != null ? item.getRelevantUsers().size() : 0;
		for (int i = 0; i < memberCount; i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconVerticalInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				memberLayout.addView(layout);
			}
			
			User user = item.getRelevantUsers().get(i);
			Bitmap avatar = BitmapFactory.decodeFile(user.getAvatarPath());
			
			View memberView = View.inflate(this, R.layout.grid_member, null);
			
			ImageView avatarImageView = (ImageView) memberView.findViewById(R.id.avatarImageView);
			if (avatar != null)
			{
				avatarImageView.setImageBitmap(avatar);
			}
			
			TextView nameTextView = (TextView) memberView.findViewById(R.id.nameTextView);
			nameTextView.setText(user.getNickname());
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconHorizontalInterval;
			
			layout.addView(memberView, params);
		}
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
    
    private void showTypeWindow()
    {
		typePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		typePopupWindow.update();
		
		Utils.dimBackground(this);
    }
    
    private void showPictureWindow()
    {
		picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		Utils.dimBackground(this);
    }
    
    private void showTimeWindow()
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

    private void showVendorWindow()
    {
    	vendorPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	vendorPopupWindow.update();
		
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

    private void showLocationWindow()
    {
    	locationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	locationPopupWindow.update();
    }
    
    private void showCategoryWindow()
    {
    	categoryPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	categoryPopupWindow.update();
		
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

    private void showTagWindow()
    {
    	tagPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	tagPopupWindow.update();
		
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

    private void showMemberWindow()
    {
    	memberPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	memberPopupWindow.update();
    }
    
    private void showManagerWindow()
    {
    	managerPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	managerPopupWindow.update();
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
							initCategoryList();
							if (categoryAdapter != null)
							{
								categoryAdapter.setCategory(categoryList, subCategoryList);
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

	private void sendDownloadVendorImageRequest(int index)
	{
		final Vendor vendor = vendorList.get(index);
		DownloadImageRequest request = new DownloadImageRequest(vendor.getPhotoURL());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					vendor.setPhoto(response.getBitmap());
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							vendorAdapter.setVendorList(vendorList);
							vendorAdapter.notifyDataSetChanged();
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
		GetVendorsRequest request = new GetVendorsRequest(category, latitude, longitude);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetVendorsResponse response = new GetVendorsResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							vendorList = response.getVendorList();
							
							if (vendorList.size() > 0)
							{
								vendorAdapter.setVendorList(vendorList);
								vendorAdapter.notifyDataSetChanged();
								
								for (int i = 0 ; i < vendorList.size(); i++)
								{
									Vendor vendor = vendorList.get(i);
									if (vendor.getPhoto() == null && !vendor.getPhotoURL().equals(""))
									{
										sendDownloadVendorImageRequest(i);
									}
								}
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
						if (locationTextView.equals(getString(R.string.no_location)))
						{
							locationTextView.setText(address.getCity());
						}
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