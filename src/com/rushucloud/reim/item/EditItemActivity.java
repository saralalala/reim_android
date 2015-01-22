package com.rushucloud.reim.item;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Item.GetVendorsResponse;
import netUtils.Response.Item.ModifyItemResponse;
import netUtils.Response.Report.CreateReportResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.GetVendorsRequest;
import netUtils.Request.Item.ModifyItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import classes.Category;
import classes.Image;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.User;
import classes.Vendor;
import classes.adapter.CategoryExpandableListAdapter;
import classes.adapter.LocationListViewAdapter;
import classes.adapter.MemberListViewAdapter;
import classes.adapter.TagListViewAdapter;
import classes.adapter.VendorListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import cn.beecloud.BCLocation;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.rushucloud.reim.GalleryActivity;
import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

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
import android.view.ViewGroup;
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
import android.widget.TextView;
import android.widget.ToggleButton;

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
	private ImageView addInvoiceImageView;
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

	private List<ImageView> removeList = null;
	boolean removeImageViewShown = false;
	
	private static AppPreference appPreference;
	private static DBManager dbManager;
	
	private Item item;
	private Report report;
	private List<Image> originInvoiceList;

	private List<Vendor> vendorList = null;
	private List<Category> categoryList = null;
	private List<List<Category>> subCategoryList = null;
	private List<Boolean> check = null;
	private List<List<Boolean>> subCheck = null;
	private List<Tag> tagList = null;
	private List<User> userList = null;

	private boolean fromReim;
	private boolean newItem = false;
	private int imageTaskCount;
		
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
		ReimProgressDialog.setProgressDialog(this);
		locationClient.registerLocationListener(listener);
		getLocation();
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
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (removeImageViewShown)
			{
				for (ImageView removeImageView : removeList)
				{
					removeImageView.setVisibility(View.INVISIBLE);
				}
				removeImageViewShown = false;
			}
			else
			{
				goBack();		
			}			
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
					String[] paths = data.getStringArrayExtra("paths");

					for (int i = 0; i < paths.length; i++)
					{
						String filePath = PhoneUtils.getInvoiceFilePath();
						boolean result = PhoneUtils.copyFile(paths[i], filePath);
						if (result)
						{
							Image image = new Image();
							image.setPath(filePath);
							item.getInvoices().add(image);
						}
					}
					
					refreshInvoiceView();
				}
				else if (requestCode == TAKE_PHOTO)
				{
					Bitmap bitmap = BitmapFactory.decodeFile(appPreference.getTempInvoicePath());
					String invoicePath = PhoneUtils.saveBitmapToFile(bitmap, NetworkConstant.IMAGE_TYPE_INVOICE);
					if (!invoicePath.equals(""))
					{
						Image image = new Image();
						image.setPath(invoicePath);
						item.getInvoices().add(image);
					}
					else
					{
						ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
					}
					
					refreshInvoiceView();
				}
			}
			catch (Exception e)
			{
				ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
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
			if (!categoryList.isEmpty())
			{
				item.setCategory(categoryList.get(0));				
			}
			item.setConsumedDate(Utils.getCurrentTime());
			item.setInvoices(new ArrayList<Image>());
			List<User> relevantUsers = new ArrayList<User>();
			relevantUsers.add(appPreference.getCurrentUser());
			item.setRelevantUsers(relevantUsers);
			originInvoiceList = new ArrayList<Image>();
		}
		else
		{
			newItem = false;
			MobclickAgent.onEvent(this, "UMENG_EDIT_ITEM");
			item = dbManager.getItemByLocalID(itemLocalID);
			originInvoiceList = new ArrayList<Image>(item.getInvoices());
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
				goBack();
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
					
					if (fromReim && item.isProveAhead() && !item.isPaApproved())
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
													if (PhoneUtils.isNetworkConnected())
													{
														showManagerWindow();
													}
													else
													{
														ViewUtils.showToast(EditItemActivity.this, R.string.error_send_network_unavailable);
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
					ViewUtils.showToast(EditItemActivity.this, R.string.error_number_wrong_format);
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
		amountEditText.setOnFocusChangeListener(ViewUtils.getEditTextFocusChangeListener());
		if (item.getAmount() == 0)
		{
			amountEditText.requestFocus();
		}
		else
		{
			amountEditText.setText(Utils.formatDouble(item.getAmount()));
		}
		
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
				if (fromReim && !item.isPaApproved())
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

		typePopupWindow = ViewUtils.constructBottomPopupWindow(this, typeView);
	}
	
	private void initInvoiceView()
	{		
		// init invoice		
		invoiceLayout = (LinearLayout)findViewById(R.id.invoiceLayout);
				
		addInvoiceImageView = new ImageView(this);
		addInvoiceImageView.setImageResource(R.drawable.add_tag_button);
		addInvoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				if (item.getInvoices().size() == Item.MAX_INVOICE_COUNT)
				{
					ViewUtils.showToast(EditItemActivity.this, R.string.prompt_max_image_count);
				}
				else
				{
					showPictureWindow();					
				}
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
		cameraButton = ViewUtils.resizeWindowButton(cameraButton);
		
		Button galleryButton = (Button) pictureView.findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();

				Intent intent = new Intent(EditItemActivity.this, GalleryActivity.class);
				intent.putExtra("maxCount", Item.MAX_INVOICE_COUNT - item.getInvoices().size());
				startActivityForResult(intent, PICK_IMAGE);
			}
		});
		galleryButton = ViewUtils.resizeWindowButton(galleryButton);
		
		Button cancelButton = (Button) pictureView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
			}
		});
		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
		
		picturePopupWindow = ViewUtils.constructBottomPopupWindow(this, pictureView);

		if (!PhoneUtils.isNetworkConnected())
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_download_invoice);				
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
		confirmButton = ViewUtils.resizeShortButton(confirmButton, 30);
		
		datePicker = (DatePicker) timeView.findViewById(R.id.datePicker);
		
		timePopupWindow = ViewUtils.constructBottomPopupWindow(this, timeView);
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
		vendorEditText.setOnFocusChangeListener(ViewUtils.getEditTextFocusChangeListener());
		
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

		vendorPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, vendorView);
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
		locationEditText.setOnFocusChangeListener(ViewUtils.getEditTextFocusChangeListener());
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
				locationTextView.setText(locationEditText.getText().toString());
			}
		});

		locationPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, locationView);
	}
	
	private void initCategoryView()
	{
		// init category
		categoryImageView = (ImageView) findViewById(R.id.categoryImageView);
		categoryImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!item.isPaApproved())
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
				if (!item.isPaApproved())
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
			
			if (item.getCategory().hasUndownloadedIcon() && PhoneUtils.isNetworkConnected())
			{
				sendDownloadCategoryIconRequest(item.getCategory());
			}
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

		categoryPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, categoryView);	
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
				if (!tagList.isEmpty())
				{
					showTagWindow();
				}
				else
				{
					ViewUtils.showToast(EditItemActivity.this, R.string.no_tags);
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

		tagPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, tagView);	
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
				if (!userList.isEmpty())
				{
					showMemberWindow();
				}
				else
				{
					ViewUtils.showToast(EditItemActivity.this, R.string.no_member);
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

		memberPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, memberView);	
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

				if (managerList.isEmpty())
				{
					ViewUtils.showToast(EditItemActivity.this, R.string.no_manager);
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

					ReimProgressDialog.show();
					if (newItem)
					{
						int localID = dbManager.insertItem(item);
						item = dbManager.getItemByLocalID(localID);
						newItem = false;
					}
					else
					{
						dbManager.updateItemByLocalID(item);
						item = dbManager.getItemByLocalID(item.getLocalID());
					}

					List<Image> invoiceList = new ArrayList<Image>();
					for (Image image : item.getInvoices())
					{
						if (image.isNotUploaded())
						{
							invoiceList.add(image);
						}
					}
					imageTaskCount = invoiceList.size();
					
					if (imageTaskCount > 0)
					{
						for (Image image : invoiceList)
						{
							if (image.isNotUploaded())
							{
								sendUploadImageRequest(image);
							}
						}
					}
					else
					{						
						if (item.getServerID() == -1)
						{
							sendCreateItemRequest();
						}
						else
						{
							sendModifyItemRequest();													
						}
					}
				}
			}
		});

		managerPopupWindow = ViewUtils.constructHorizontalPopupWindow(this, managerView);	
	}

	private void refreshInvoiceView()
	{
		invoiceLayout.removeAllViews();
		
		if (removeList == null)
		{
			removeList = new ArrayList<ImageView>();
		}
		else
		{
			removeList.clear();
		}
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, metrics);
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, metrics);
		int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
		int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
		int maxCount = (layoutMaxLength + horizontalPadding) / (width + horizontalPadding);
		horizontalPadding = (layoutMaxLength - width * maxCount) / (maxCount - 1);

		LinearLayout layout = new LinearLayout(this);
		int invoiceCount = item.getInvoices() != null ? item.getInvoices().size() : 0;
		for (int i = 0; i < invoiceCount + 1; i++)
		{
			if (i > Item.MAX_INVOICE_COUNT)
			{
				break;
			}
			
			if (i % maxCount == 0)
			{
				layout = new LinearLayout(this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				if (i != 0)
				{
					params.topMargin = verticalPadding;					
				}
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				invoiceLayout.addView(layout);
			}
			
			if (i < invoiceCount)
			{
				final int index = i;
				final Bitmap bitmap = item.getInvoices().get(index).getBitmap();
				
				View view = View.inflate(this, R.layout.grid_invoice, null);

				final ImageView removeImageView = (ImageView) view.findViewById(R.id.removeImageView);
				removeImageView.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						item.getInvoices().remove(index);
						refreshInvoiceView();
					}
				});
				removeList.add(removeImageView);
				
				ImageView invoiceImageView = (ImageView) view.findViewById(R.id.invoiceImageView);
				invoiceImageView.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						if (bitmap != null && removeImageView.getVisibility() != View.VISIBLE)
						{
							hideSoftKeyboard();
							removeImageView.setVisibility(View.INVISIBLE);
							Intent intent = new Intent(EditItemActivity.this, ImageActivity.class);
							intent.putExtra("imagePath", item.getInvoices().get(index).getPath());
							startActivity(intent);
						}
					}
				});
				invoiceImageView.setOnLongClickListener(new View.OnLongClickListener()
				{
					public boolean onLongClick(View v)
					{
						for (ImageView removeImageView : removeList)
						{
							removeImageView.setVisibility(View.VISIBLE);
						}
						removeImageViewShown = true;
						return false;
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
				layout.addView(view, params);
			}
			else
			{
				int addButtonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, metrics);
				int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(addButtonWidth, addButtonWidth);
				params.topMargin = padding;
				
				ViewGroup viewGroup = (ViewGroup) addInvoiceImageView.getParent();
				if (viewGroup != null)
				{
					viewGroup.removeView(addInvoiceImageView);
				}
				layout.addView(addInvoiceImageView, params);
			}
			
		}
	}

	private void refreshTagView()
	{
		tagLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 126, metrics);
		int tagVerticalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, metrics);
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
 
    private void showTypeWindow()
    {    	
		typePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		typePopupWindow.update();
		
		ViewUtils.dimBackground(this);
    }
    
    private void showPictureWindow()
    {
		picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		ViewUtils.dimBackground(this);
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

		ViewUtils.dimBackground(this);
    }

    private void showVendorWindow()
    {
    	vendorPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	vendorPopupWindow.update();
		
		if (!PhoneUtils.isNetworkConnected())
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.error_get_vendor_network_unavailable);
		}
		else if (currentLocation != null)
		{
			double latitude = currentLocation.getLatitude();
			double longitude = currentLocation.getLongitude();
			String category = item.getCategory() == null ? "" : item.getCategory().getName();
			sendVendorsRequest(category, latitude, longitude);
		}
		else if (!PhoneUtils.isLocalisationEnabled())
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.error_gps_unavailable);
		}
		else
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_get_gps_info);    	
		}
    }

    private void showLocationWindow()
    {
    	getLocation();
    	
    	locationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	locationPopupWindow.update();
    }
    
    private void showCategoryWindow()
    {
    	categoryPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
    	categoryPopupWindow.update();
		
		if (PhoneUtils.isNetworkConnected())
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
		
		if (PhoneUtils.isNetworkConnected())
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

    private void goBack()
    {
		for (Image newImage : item.getInvoices())
		{
			boolean imageExists = false;
			for (Image oldImage : originInvoiceList)
			{
				if (newImage.getPath().equals(oldImage.getPath()))
				{
					imageExists = true;
					break;
				}
			}
			if (!imageExists)
			{
				newImage.deleteFile();
			}
		}
		finish();
    }
    
    private void saveItem()
    {
    	if (dbManager.syncItem(item))
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
			finish();
		}
		else
		{
			ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_item);
		}
    }
    
    private void sendDownloadInvoiceRequest(final Image image)
    {
		DownloadImageRequest request = new DownloadImageRequest(image.getServerID(), DownloadImageRequest.INVOICE_QUALITY_ORIGINAL);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					final String invoicePath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_INVOICE);
					if (!invoicePath.equals(""))
					{
						image.setPath(invoicePath);
						dbManager.updateImageByServerID(image);
						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								int index = item.getInvoices().indexOf(image);
								if (index != -1)
								{
									item.getInvoices().set(index, image);
								}
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
								ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
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
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_download_invoice);
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
					String iconPath = PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
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
					String iconPath = PhoneUtils.saveIconToFile(response.getBitmap(), tag.getIconID());
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
        
    private void sendUploadImageRequest(final Image image)
    {
		UploadImageRequest request = new UploadImageRequest(image.getPath(), NetworkConstant.IMAGE_TYPE_INVOICE);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
					image.setServerID(response.getImageID());
					dbManager.updateImageByLocalID(image);
					
					imageTaskCount--;
					if (imageTaskCount == 0)
					{
						if (item.getServerID() == -1)
						{
							sendCreateItemRequest();
						}
						else
						{
							sendModifyItemRequest();
						}
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_upload_invoice);
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
					dbManager.updateItemByLocalID(item);
					sendApproveReportRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_create_item);
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_modify_item);
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_creating_report);
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_create_report);
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
							
							if (!vendorList.isEmpty())
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
								ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_get_vendor_no_data);								
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_get_vendor);
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
				final String cityName = address.getCity();
		    	locationAdapter.setCurrentCity(cityName);
		    	
		    	runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (locationTextView.getText().toString().equals(getString(R.string.no_location)))
						{
							locationTextView.setText(cityName);
						}
				    	locationAdapter.notifyDataSetChanged();						
					}
				});
			}
		}).start();
    }

    private void getLocation()
    {
		if (PhoneUtils.isLocalisationEnabled() && PhoneUtils.isNetworkConnected())
		{
	    	LocationClientOption option = new LocationClientOption();
	    	option.setLocationMode(LocationMode.Hight_Accuracy);
	    	option.setScanSpan(5000);
	    	option.setIsNeedAddress(false);
	    	option.setNeedDeviceDirect(false);
	    	locationClient.setLocOption(option);
	    	locationClient.start();
		}
    }
    
    public class ReimLocationListener implements BDLocationListener
    {
    	public void onReceiveLocation(BDLocation location)
    	{
    		if (location != null)
    		{
    			currentLocation = location;
    			locationClient.stop();
    			if (PhoneUtils.isNetworkConnected())
				{
        			sendLocationRequest(currentLocation.getLatitude(), currentLocation.getLongitude());			
				}
    		}
    	}
    }
}