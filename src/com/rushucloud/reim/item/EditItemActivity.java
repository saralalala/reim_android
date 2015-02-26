package com.rushucloud.reim.item;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Response.DownloadImageResponse;
import netUtils.Request.DownloadImageRequest;
import classes.Category;
import classes.Image;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.User;
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
import com.rushucloud.reim.MultipleImageActivity;
import com.rushucloud.reim.R;
import com.rushucloud.reim.report.EditReportActivity;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class EditItemActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int PICK_VENDOR = 2;
	private static final int PICK_LOCATION = 3;
	private static final int PICK_CATEGORY = 4;
	private static final int PICK_TAG = 5;
	private static final int PICK_MEMBER = 6;
	
	private EditText amountEditText;
	
	private PopupWindow typePopupWindow;
	private TextView typeTextView;
	private RadioButton consumedRadio;
	private RadioButton proveAheadRadio;
	private ToggleButton needReimToggleButton;
	
	private LinearLayout invoiceLayout;
	private ImageView addInvoiceImageView;
	private PopupWindow picturePopupWindow;
	
	private TextView timeTextView;
	private PopupWindow timePopupWindow;
	private DatePicker datePicker;
	private TimePicker timePicker;
	
	private TextView vendorTextView;
	
	private TextView locationTextView;
	
	private ImageView categoryImageView;
	private TextView categoryTextView;
	
	private LinearLayout tagLayout;
	private ImageView addTagImageView;
	
	private LinearLayout memberLayout;
	private ImageView addMemberImageView;
	
	private EditText noteEditText;

	private List<ImageView> removeList = null;
	boolean removeImageViewShown = false;
	
	private static AppPreference appPreference;
	private static DBManager dbManager;
	
	private Item item;
	private List<Image> originInvoiceList;

	private boolean fromReim;
	private boolean newItem = false;
		
	private LocationClient locationClient = null;
	private BDLocationListener listener = new ReimLocationListener();
	private BDLocation currentLocation;
	private String currentCity = "";
	
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
	
	@SuppressWarnings("unchecked")
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			switch (requestCode)
			{
				case PICK_IMAGE:
				{
					try
					{
						String[] paths = data.getStringArrayExtra("paths");

						ReimProgressDialog.show();
						Bitmap bitmap;
						for (int i = 0; i < paths.length; i++)
						{
							bitmap = BitmapFactory.decodeFile(paths[i]);
							String invoicePath = PhoneUtils.saveBitmapToFile(bitmap, NetworkConstant.IMAGE_TYPE_INVOICE);
							if (!invoicePath.equals(""))
							{
								Image image = new Image();
								image.setPath(invoicePath);
								item.getInvoices().add(image);
							}
						}
						
						refreshInvoiceView();
						ReimProgressDialog.dismiss();
					}
					catch (Exception e)
					{
						ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
						e.printStackTrace();
					}
					break;
				}
				case TAKE_PHOTO:
				{
					try
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
					catch (Exception e)
					{
						ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
						e.printStackTrace();
					}
					break;
				}
				case PICK_VENDOR:
				{
					item.setVendor(data.getStringExtra("vendor"));
					vendorTextView.setText(item.getVendor());
					break;
				}
				case PICK_LOCATION:
				{
					item.setLocation(data.getStringExtra("location"));
					String location = item.getLocation().isEmpty() ? getString(R.string.no_location) : item.getLocation();
					locationTextView.setText(location);
					break;
				}
				case PICK_CATEGORY:
				{
					Category category = (Category) data.getSerializableExtra("category");
					item.setCategory(category);
					
					if (category != null)
					{
						categoryTextView.setText(category.getName());
						Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
						if (bitmap != null)
						{
							categoryImageView.setImageBitmap(bitmap);
						}
						else
						{
							categoryImageView.setImageResource(R.drawable.default_icon);
						}
						
						if (category.hasUndownloadedIcon())
						{
							sendDownloadCategoryIconRequest(category);
						}
					}
					break;
				}
				case PICK_TAG:
				{
					List<Tag> tags = (List<Tag>) data.getSerializableExtra("tags");
					item.setTags(tags);
					refreshTagView();
					break;
				}
				case PICK_MEMBER:
				{
					List<User> users = (List<User>) data.getSerializableExtra("users");
					item.setRelevantUsers(users);
					refreshMemberView();
					break;
				}
				default:
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		locationClient = new LocationClient(getApplicationContext());

		List<Category> categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
		
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
					
					if (newItem)
					{
						item.setCreatedDate(item.getLocalUpdatedDate());
					}
					
					if (fromReim && item.isProveAhead() && !item.isPaApproved())
					{
						Builder buider = new Builder(EditItemActivity.this);
						buider.setTitle(R.string.option);
						buider.setMessage(R.string.prompt_save_prove_ahead_item);
						buider.setPositiveButton(R.string.only_save, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
										    		dbManager.syncItem(item);
													ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
													finish();
												}
											});
						buider.setNeutralButton(R.string.send_to_approve, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													Report report;
													if (item.getBelongReport() == null)
													{
														report = new Report();
												    	report.setTitle(getString(R.string.report_prove_ahead));
												    	report.setSender(appPreference.getCurrentUser());
												    	report.setCreatedDate(Utils.getCurrentTime());
												    	report.setLocalUpdatedDate(Utils.getCurrentTime());
														report.setIsProveAhead(true);
												    	dbManager.insertReport(report);
												    	report.setLocalID(dbManager.getLastInsertReportID());
														
												    	item.setBelongReport(report);														
													}
													else
													{
														report = item.getBelongReport();
													}
													
										    		dbManager.syncItem(item);
													ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);													

													Bundle bundle = new Bundle();
													bundle.putSerializable("report", report);
													Intent intent = new Intent(EditItemActivity.this, EditReportActivity.class);
													intent.putExtras(bundle);
													startActivity(intent);
													finish();
												}
											});
						buider.setNegativeButton(R.string.cancel, null);
						buider.create().show();
					}
					else
					{
			    		dbManager.syncItem(item);
						ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
						finish();
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
	}
	
	private void initStatusView()
	{
		TextView actualCostTextView = (TextView)findViewById(R.id.actualCostTextView);
		TextView budgetTextView = (TextView)findViewById(R.id.budgetTextView);
		TextView approvedTextView = (TextView)findViewById(R.id.approvedTextView);

		amountEditText = (EditText)findViewById(R.id.amountEditText);
		amountEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
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
		consumedRadio = (RadioButton)typeView.findViewById(R.id.consumedRadio);
		proveAheadRadio = (RadioButton)typeView.findViewById(R.id.proveAheadRadio);
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
				
		needReimToggleButton = (ToggleButton)typeView.findViewById(R.id.needReimToggleButton);
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

		removeList = new ArrayList<ImageView>();
		
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
				hideSoftKeyboard();
				showTimeWindow();
			}
		});
		timeTextView.setText(Utils.secondToStringUpToMinute(time));
		
		// init time window
		View timeView = View.inflate(this, R.layout.window_reim_time, null);
		
		Button confirmButton = (Button) timeView.findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				timePopupWindow.dismiss();
				
				GregorianCalendar greCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), 
						datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				item.setConsumedDate((int)(greCal.getTimeInMillis() / 1000));
				timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));
			}
		});
		confirmButton = ViewUtils.resizeShortButton(confirmButton, 30, true);
		
		datePicker = (DatePicker) timeView.findViewById(R.id.datePicker);
		
		timePicker = (TimePicker) timeView.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		
		resizePicker();
		
		timePopupWindow = ViewUtils.constructBottomPopupWindow(this, timeView);
	}
	
	private void initVendorView()
	{		
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
				
				String category = item.getCategory() != null ? item.getCategory().getName() : getString(R.string.null_string);
				Intent intent = new Intent(EditItemActivity.this, PickVendorActivity.class);
				intent.putExtra("vendor", item.getVendor());
				intent.putExtra("category", category);
				if (currentLocation != null)
				{
					intent.putExtra("latitude", currentLocation.getLatitude());
					intent.putExtra("longitude", currentLocation.getLongitude());					
				}
				startActivityForResult(intent, PICK_VENDOR);
			}
		});
	}
	
	private void initLocationView()
	{
		String cityName = item.getLocation().equals("") ? getString(R.string.no_location) : item.getLocation();
		locationTextView = (TextView)findViewById(R.id.locationTextView);
		locationTextView.setText(cityName);
		locationTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				Intent intent = new Intent(EditItemActivity.this, PickLocationActivity.class);
				intent.putExtra("location", item.getLocation());
				intent.putExtra("currentCity", currentCity);
				startActivityForResult(intent, PICK_LOCATION);
			}
		});
	}
	
	private void initCategoryView()
	{
		RelativeLayout categoryLayout = (RelativeLayout) findViewById(R.id.categoryLayout);
		categoryLayout.setOnClickListener(new View.OnClickListener()
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
					Intent intent = new Intent(EditItemActivity.this, PickCategoryActivity.class);
					intent.putExtra("category", item.getCategory());
					startActivityForResult(intent, PICK_CATEGORY);
				}				
			}
		});

		categoryImageView = (ImageView) findViewById(R.id.categoryImageView);		
		categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		
		if (item.getCategory() != null)
		{
			categoryTextView.setText(item.getCategory().getName());
			
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
		else
		{
			categoryTextView.setText(R.string.not_available);			
		}
	}
	
	private void initTagView()
	{
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
				Intent intent = new Intent(EditItemActivity.this, PickTagActivity.class);
				intent.putExtra("tags", (Serializable) item.getTags());
				startActivityForResult(intent, PICK_TAG);
			}
		});
				
		refreshTagView();
	}
	
	private void initMemberView()
	{
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
				Intent intent = new Intent(EditItemActivity.this, PickMemberActivity.class);
				intent.putExtra("users", (Serializable) item.getRelevantUsers());
				startActivityForResult(intent, PICK_MEMBER);							
			}
		});
		
		refreshMemberView();
		
		if (item.getRelevantUsers() != null)
		{
			for (User user : item.getRelevantUsers())
			{
				if (user.hasUndownloadedAvatar())
				{
					sendDownloadAvatarRequest(user);
				}
			}
		}
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
	
	private void refreshInvoiceView()
	{
		invoiceLayout.removeAllViews();

		removeList.clear();
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 96);
		int width = PhoneUtils.dpToPixel(getResources(), 40);
		int verticalPadding = PhoneUtils.dpToPixel(getResources(), 5);
		int horizontalPadding = PhoneUtils.dpToPixel(getResources(), 5);
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
							
							ArrayList<String> pathList = new ArrayList<String>();
							for (Image image : item.getInvoices())
							{
								if (!image.getPath().equals(""))
								{
									pathList.add(image.getPath());
								}
							}
							
							int pageIndex = pathList.indexOf(item.getInvoices().get(index).getPath());
							
							Bundle bundle = new Bundle();
							bundle.putStringArrayList("imagePath", pathList);
							bundle.putInt("index", pageIndex);
							
							Intent intent = new Intent(EditItemActivity.this, MultipleImageActivity.class);
							intent.putExtras(bundle);
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
				int addButtonWidth = PhoneUtils.dpToPixel(getResources(), 30);
				int padding = PhoneUtils.dpToPixel(getResources(), 10);
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
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 126);
		int tagVerticalInterval = PhoneUtils.dpToPixel(getResources(), 17);
		int tagHorizontalInterval = PhoneUtils.dpToPixel(getResources(), 10);
		int padding = PhoneUtils.dpToPixel(getResources(), 24);
		int textSize = PhoneUtils.dpToPixel(getResources(), 16);
		
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
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 126);
		int iconWidth = PhoneUtils.dpToPixel(getResources(), 50);
		int iconVerticalInterval = PhoneUtils.dpToPixel(getResources(), 18);
		int iconHorizontalInterval = PhoneUtils.dpToPixel(getResources(), 18);
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

    private void showTypeWindow()
    {    	
		consumedRadio.setChecked(!item.isProveAhead());
		proveAheadRadio.setChecked(item.isProveAhead());
		needReimToggleButton.setChecked(item.needReimbursed());
		
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
		
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		
		timePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		timePopupWindow.update();

		ViewUtils.dimBackground(this);
    }
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(noteEditText.getWindowToken(), 0);  	
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
							item.setCategory(category);
							Bitmap categoryIcon = BitmapFactory.decodeFile(item.getCategory().getIconPath());
							if (categoryIcon != null)
							{
								categoryImageView.setImageBitmap(categoryIcon);
							}
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
							int index = item.getRelevantUsers().indexOf(user);
							item.getRelevantUsers().set(index, user);
							refreshMemberView();
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
				currentCity = address.getCity();
		    	
		    	runOnUiThread(new Runnable()
				{
					public void run()
					{
						if (locationTextView.getText().toString().equals(getString(R.string.no_location)))
						{
							item.setLocation(currentCity);
							locationTextView.setText(currentCity);
						}					
					}
				});
			}
		}).start();
    }

    private void getLocation()
    {
		if (PhoneUtils.isLocalisationEnabled() || PhoneUtils.isNetworkConnected())
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

    private void resizePicker()
    {
    	int yearWidth = PhoneUtils.dpToPixel(this, 60);
    	int width = PhoneUtils.dpToPixel(this, 40);
    	int dateMargin = PhoneUtils.dpToPixel(this, 15);
    	int timeMargin = PhoneUtils.dpToPixel(this, 5);
    	
    	LinearLayout datePickerContainer = (LinearLayout) datePicker.getChildAt(0);
    	LinearLayout dateSpinner = (LinearLayout) datePickerContainer.getChildAt(0);
    	
    	NumberPicker yearPicker = (NumberPicker) dateSpinner.getChildAt(0);
		LayoutParams params = new LayoutParams(yearWidth, LayoutParams.WRAP_CONTENT);
		params.rightMargin = dateMargin;
		yearPicker.setLayoutParams(params);
    	
    	NumberPicker monthPicker = (NumberPicker) dateSpinner.getChildAt(1);
    	params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
		params.rightMargin = dateMargin;
		monthPicker.setLayoutParams(params);
    	
    	NumberPicker datePicker = (NumberPicker) dateSpinner.getChildAt(2);
    	params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
		datePicker.setLayoutParams(params);
		
		List<NumberPicker> pickerList = findNumberPickers(timePicker);
		for (int i = 0; i < pickerList.size(); i++)
		{
			NumberPicker picker = pickerList.get(i);
	    	params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
	    	if (i == 0)
			{
				params.rightMargin = timeMargin;
			}
	    	else
	    	{
	    		params.leftMargin = timeMargin;
	    	}
	    	picker.setLayoutParams(params);
		}
	}
    
	private List<NumberPicker> findNumberPickers(ViewGroup viewGroup)
	{
		List<NumberPicker> pickerList = new ArrayList<NumberPicker>();
		View child = null;
		if (null != viewGroup)
		{
			for (int i = 0; i < viewGroup.getChildCount(); i++)
			{
				child = viewGroup.getChildAt(i);
				if (child instanceof NumberPicker)
				{
					pickerList.add((NumberPicker) child);
				}
				else if (child instanceof LinearLayout)
				{
					List<NumberPicker> result = findNumberPickers((ViewGroup) child);
					if (result.size() > 0)
					{
						return result;
					}
				}
			}
		}
		return pickerList;
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