package com.rushucloud.reim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.GetLocationRequest;
import netUtils.Request.Item.GetVendorsRequest;
import netUtils.Request.Item.ModifyItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Item.CreateItemResponse;
import netUtils.Response.Item.GetLocationResponse;
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
import classes.Adapter.MemberListViewAdapter;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditItemActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;

	private static AppPreference appPreference;
	private static DBManager dbManager;
	private LocationClient locationClient = null;
	private BDLocationListener listener = new ReimLocationListener();
	private BDLocation currentLocation;
	private String currentCity;
	private String locationInvalid;
	private int getLocationTryTimes = 2;
	private boolean fromReim;
	
	private EditText amountEditText;
	private EditText vendorEditText;
	private EditText locationEditText;
	private EditText noteEditText;
	private CheckBox proveAheadCheckBox;
	private CheckBox needReimCheckBox;
	private ImageView invoiceImageView;
	private TextView paAmountTextView;
	private TextView categoryTextView;
	private TextView tagTextView;
	private TextView timeTextView;
	private TextView memberTextView;
	
	private Item item;
	private Report report;
	
	private List<String> vendorList = null;
	private List<String> cityList = null;
	private List<Category> categoryList = null;
	private List<Tag> tagList = null;
	private List<User> userList = null;
	
	private boolean newItem = false;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_edit_item);
		initData();
		initView();
		initButton();
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
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(null);
		menu.add(0, 0, 0, "从图库选取");
		menu.add(0, 1, 0, "用相机拍摄");
	}
	
	public boolean onContextItemSelected(MenuItem item)
	{
		if (item.getItemId() == 0)
		{
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_IMAGE);
		}
		else
		{
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
			startActivityForResult(intent, TAKE_PHOTO);
		}
			
		return super.onContextItemSelected(item);
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
						item.setImageID(-1);
					}
					else
					{
						Utils.showToast(EditItemActivity.this, "图片保存失败");
					}
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
		
		cityList = new ArrayList<String>();
		locationInvalid = getString(R.string.locationInvalid);
		currentCity = locationInvalid;
		cityList.add(getString(R.string.currentLocation) + currentCity);
		cityList.addAll(Arrays.asList(getResources().getStringArray(R.array.cityArray)));

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
		ReimApplication.setProgressDialog(this);
		
		amountEditText = (EditText)findViewById(R.id.amountEditText);
		if (item.getAmount() != 0)
		{
			amountEditText.setText(Utils.formatDouble(item.getAmount()));			
		}
		
		vendorEditText = (EditText)findViewById(R.id.vendorEditText);
		vendorEditText.addTextChangedListener(new TextWatcher()
		{
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				item.setMerchant(s.toString());
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{
				
			}
			
			public void afterTextChanged(Editable s)
			{
				
			}
		});
		vendorEditText.setText(item.getMerchant());
		
		locationEditText = (EditText)findViewById(R.id.locationEditText);
		locationEditText.addTextChangedListener(new TextWatcher()
		{
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				item.setLocation(s.toString());
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{
				
			}
			
			public void afterTextChanged(Editable s)
			{
				
			}
		});
		locationEditText.setText(item.getLocation());
		
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
		
		proveAheadCheckBox = (CheckBox)findViewById(R.id.proveAheadCheckBox);
		proveAheadCheckBox.setChecked(item.isProveAhead());
		proveAheadCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
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
		if (!fromReim || item.getStatus() == Item.STATUS_PROVE_AHEAD_APPROVED)
		{
			proveAheadCheckBox.setEnabled(false);
		}
		
		needReimCheckBox = (CheckBox)findViewById(R.id.needReimCheckBox);
		needReimCheckBox.setChecked(item.needReimbursed());
		needReimCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
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
		
		paAmountTextView = (TextView)findViewById(R.id.paAmountTextView);
		if (item.getPaAmount() != 0)
		{
			paAmountTextView.setText(getResources().getString(R.string.paAmount) + "¥" + Utils.formatDouble(item.getAmount()));
			paAmountTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			paAmountTextView.setVisibility(View.GONE);
		}

		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		categoryTextView.setText(categoryName);
		
		tagTextView = (TextView)findViewById(R.id.tagTextView);
		tagTextView.setText(Tag.getTagsNameString(item.getTags()));
		
		timeTextView = (TextView)findViewById(R.id.timeTextView);
		if (item.getConsumedDate() != -1 && item.getConsumedDate() != 0)
		{
			timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));			
		}
		
		memberTextView = (TextView)findViewById(R.id.memberTextView);
		memberTextView.setText(User.getUsersNameString(item.getRelevantUsers()));
		
		invoiceImageView = (ImageView)findViewById(R.id.invoiceImageView);
		invoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!item.getInvoicePath().equals(""))
				{
					Intent intent = new Intent(EditItemActivity.this, ImageActivity.class);
					intent.putExtra("imagePath", item.getInvoicePath());
					startActivity(intent);
				}
				else
				{
					invoiceImageView.showContextMenu();
				}
			}
		});
		invoiceImageView.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				hideSoftKeyboard();
				return false;
			}
		});
		registerForContextMenu(invoiceImageView);

		Bitmap bitmap = BitmapFactory.decodeFile(item.getInvoicePath());
		if (bitmap != null)
		{
			invoiceImageView.setImageBitmap(bitmap);
		}
		else // item has invoice path but the file was deleted
		{
			invoiceImageView.setImageResource(R.drawable.default_invoice);
			if (item.getImageID() != -1 && item.getImageID() != 0 && Utils.isNetworkConnected())
			{
				sendDownloadImageRequest();
			}
			else if (item.getImageID() != -1 && item.getImageID() != 0 && !Utils.isNetworkConnected())
			{
				Utils.showToast(EditItemActivity.this, "网络未连接，无法下载图片");				
			}
		}
		
		LinearLayout baseLayout = (LinearLayout)findViewById(R.id.baseLayout);
		baseLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
	private void initButton()
	{		
		Button categoryButton = (Button)findViewById(R.id.categoryButton);
		categoryButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_CATEGORY");
				}
				if (!newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_CATEGORY");
				}
				
				hideSoftKeyboard();
				final Category category = item.getCategory();
				int index = Category.getIndexOfCategory(categoryList, category);
				if (index == -1)
				{
					index = 0;
				}
				AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
													.setTitle(R.string.chooseCategory)
													.setSingleChoiceItems(Category.getCategoryNames(categoryList), 
															index, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															item.setCategory(categoryList.get(which));
														}
													})
													.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															categoryTextView.setText(item.getCategory().getName());
														}
													})
													.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															item.setCategory(category);
														}
													})
													.create();
				mDialog.show();
			}
		});	
		categoryButton.setEnabled(item.getPaAmount() == 0);
		
		Button vendorButton = (Button)findViewById(R.id.vendorButton);
		vendorButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MERCHANT");
				}
				if (!newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MERCHANT");
				}
				
				hideSoftKeyboard();			
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
		
		Button locationButton = (Button)findViewById(R.id.locationButton);
		locationButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				showLocationDialog(); 
			}
		});
		
		Button tagButton = (Button)findViewById(R.id.tagButton);
		tagButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TAG");
				}
				if (!newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TAG");
				}
				
				hideSoftKeyboard();
				if (tagList.size() > 0)
				{
					final boolean[] check = Tag.getTagsCheck(tagList, item.getTags());
					AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
														.setTitle(R.string.chooseTag)
														.setMultiChoiceItems(Tag.getTagsName(tagList), 
																check, new DialogInterface.OnMultiChoiceClickListener()
														{
															public void onClick(DialogInterface dialog, int which, boolean isChecked)
															{
																check[which] = isChecked;
															}
														})
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
																tagTextView.setText(Tag.getTagsNameString(tags));
															}
														})
														.setNegativeButton(R.string.cancel, null)
														.create();
					mDialog.show();	
				}
				else
				{
					Utils.showToast(EditItemActivity.this, "当前组无任何标签");
				}														
			}
		});
		
		Button timeButton = (Button)findViewById(R.id.timeButton);
		timeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TIME");
				}
				if (!newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TIME");
				}
				
				View view = View.inflate(EditItemActivity.this, R.layout.reim_date_time, null);
				
				Calendar calendar = Calendar.getInstance();
				if (item.getConsumedDate() == -1 || item.getConsumedDate() == 0)
				{
					calendar.setTimeInMillis(System.currentTimeMillis());
				}
				else
				{
					calendar.setTimeInMillis((long)item.getConsumedDate() * 1000);
				}

				final DatePicker datePicker = (DatePicker)view.findViewById(R.id.datePicker);
				final TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker);
				datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
				
				timePicker.setIs24HourView(true);
				timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
				timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
				
				datePicker.clearFocus();
				timePicker.clearFocus();
				
				AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
													.setView(view)
													.setTitle(R.string.chooseTime)
													.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															GregorianCalendar greCal = new GregorianCalendar(datePicker.getYear(), 
																	datePicker.getMonth(), datePicker.getDayOfMonth(), 
																	timePicker.getCurrentHour(), timePicker.getCurrentMinute());
															item.setConsumedDate((int)(greCal.getTimeInMillis() / 1000));
															timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));
														}
													})
													.setNegativeButton(R.string.cancel, null)
													.create();
				mDialog.show();
			}
		});
		
		Button memberButton = (Button)findViewById(R.id.memberButton);
		memberButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MEMBER");
				}
				if (!newItem)
				{
					MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MEMBER");
				}
				
				hideSoftKeyboard();
				if (userList.size() > 0)
				{
					final boolean[] check = User.getUsersCheck(userList, item.getRelevantUsers());
					
					final MemberListViewAdapter memberAdapter = new MemberListViewAdapter(EditItemActivity.this, userList, check);
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
			    							.setTitle(R.string.member)
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
													memberTextView.setText(User.getUsersNameString(users));
												}
											})
											.setNegativeButton(R.string.cancel, null)
											.create();
			    	mDialog.show();
				}
				else
				{
					Utils.showToast(EditItemActivity.this, "当前组无任何其他成员");
				}
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
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
					item.setIsProveAhead(proveAheadCheckBox.isChecked());
					item.setNeedReimbursed(needReimCheckBox.isChecked());
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					
					if (fromReim && item.isProveAhead() && item.getPaAmount() == 0)
					{
						AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
											.setTitle("请选择操作")
											.setMessage("这是一条预审批的条目，您是想仅保存此条目还是要直接发送给上级审批？")
											.setPositiveButton(R.string.onlySave, new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													saveItem();												
												}
											})
											.setNeutralButton(R.string.sendToApprove, new OnClickListener()
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
														.setNegativeButton(R.string.confirm, new OnClickListener()
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
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
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
    
    private void showManagerDialog()
    {
		List<User> tempList = new ArrayList<User>();
		User defaultManager = dbManager.getUser(appPreference.getCurrentUser().getDefaultManagerID());
		if (defaultManager != null)
		{
			tempList.add(defaultManager);				
		}
		final List<User> memberList = User.removeCurrentUserFromList(userList);
		final boolean[] managerCheckList = User.getUsersCheck(memberList, tempList);
		
		final MemberListViewAdapter memberAdapter = new MemberListViewAdapter(this, memberList, managerCheckList);
    	View view = View.inflate(this, R.layout.me_member, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				managerCheckList[position] = !managerCheckList[position];
				memberAdapter.setCheck(managerCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择汇报对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> managerList = new ArrayList<User>();
										for (int i = 0; i < managerCheckList.length; i++)
										{
											if (managerCheckList[i])
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
									    	report.setUser(appPreference.getCurrentUser());
									    	report.setCreatedDate(Utils.getCurrentTime());									    	
											report.setManagerList(managerList);
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
    
    private void sendDownloadImageRequest()
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
					item.setImageID(response.getImageID());
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
    	
    	CreateReportRequest request = new CreateReportRequest(report, true);
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
		GetLocationRequest request = new GetLocationRequest(latitude, longitude);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetLocationResponse response = new GetLocationResponse(httpResponse);
				if (response.getStatus())
				{
					currentCity = getString(R.string.locationInvalid);
					cityList.set(0, getString(R.string.currentLocation) + currentCity);
				}
				else if (getLocationTryTimes > 0)
				{
					getLocationTryTimes--;
					sendLocationRequest(latitude, longitude);
				}
			}
		});
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
											.setTitle(R.string.chooseVendor)
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
													vendorEditText.setText(item.getMerchant());
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
    
    private void showLocationDialog()
    {
    	final String location = item.getLocation();
		int index = cityList.indexOf(location);
		if (index == -1)
		{
			if (currentCity.equals("") || currentCity.equals(locationInvalid))
			{
				index = 1;
				item.setLocation(cityList.get(1));
			}
			else
			{
				index = 0;
				item.setLocation(cityList.get(0));				
			}
		}
		String[] cities = cityList.toArray(new String[cityList.size()]);
		AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
											.setTitle(R.string.chooseLocation)
											.setSingleChoiceItems(cities, index, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													if (which == 0)
													{
														if (currentCity.equals("") || currentCity.equals(locationInvalid))
														{
															item.setLocation("");
														}
														else
														{
															item.setLocation(currentCity);
														}														
													}
													else
													{
														item.setLocation(cityList.get(which));														
													}
												}
											})
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													locationEditText.setText(item.getLocation());
												}
											})
											.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													item.setLocation(location);
												}
											})
											.create();
		mDialog.show();
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