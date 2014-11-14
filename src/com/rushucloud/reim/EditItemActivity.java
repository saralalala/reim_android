package com.rushucloud.reim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.SyncUtils;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Item.CreateItemRequest;
import netUtils.Request.Item.GetVendorsRequest;
import netUtils.Request.Item.ModifyItemRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Response.DownloadImageResponse;
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
import classes.Adapter.MemberListViewAdapater;

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
import android.widget.Toast;

public class EditItemActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;

	private static AppPreference appPreference;
	private static DBManager dbManager;
	private LocationClient locationClient = null;
	private BDLocationListener listener = new ReimLocationListener();
	private boolean fromReim;
	
	private EditText amountEditText;
	private EditText vendorEditText;
	private EditText noteEditText;
	private CheckBox proveAheadCheckBox;
	private CheckBox needReimCheckBox;
	private ImageView invoiceImageView;
	private TextView categoryTextView;
	private TextView tagTextView;
	private TextView timeTextView;
	private TextView memberTextView;
	
	private Item item;
	private Report report;
	
	private List<String> vendorList = null;
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
		ReimApplication.setProgressDialog(this);
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
			goBack();
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
						Toast.makeText(EditItemActivity.this, "图片保存失败", Toast.LENGTH_SHORT).show();
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
		locationClient.registerLocationListener(listener);
		
		vendorList = new ArrayList<String>();

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
			amountEditText.setText(Double.toString(item.getAmount()));			
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
		if (!fromReim)
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
		else
		{
			invoiceImageView.setImageResource(R.drawable.default_invoice);
			if (item.getImageID() != -1 && item.getImageID() != 0)
			{
				DownloadImageRequest request = new DownloadImageRequest(item.getImageID());
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
										Toast.makeText(EditItemActivity.this, "图片保存失败", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(EditItemActivity.this, "图片下载失败", Toast.LENGTH_SHORT).show();
								}
							});								
						}
					}
				});
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
				if (Utils.isLocalisationEnabled() && Utils.isNetworkConnected())
				{
					getLocation();
				}
				else if (!Utils.isLocalisationEnabled())
				{
					Toast.makeText(EditItemActivity.this, "定位服务不可用，请打开定位服务或手动输入商家名称", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(EditItemActivity.this, "网络未连接，无法联网获取商家，请手动输入商家名称", Toast.LENGTH_SHORT).show();
				}
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
					Toast.makeText(EditItemActivity.this, "当前组无任何标签", Toast.LENGTH_SHORT).show();
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
					AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
														.setTitle(R.string.member)
														.setMultiChoiceItems(User.getUsersName(userList), 
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
					Toast.makeText(EditItemActivity.this, "当前组无任何其他成员", Toast.LENGTH_SHORT).show();
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
					item.setAmount(Double.valueOf(amountEditText.getText().toString()));
					item.setConsumer(appPreference.getCurrentUser());
					item.setNote(noteEditText.getText().toString());
					item.setIsProveAhead(proveAheadCheckBox.isChecked());
					item.setNeedReimbursed(needReimCheckBox.isChecked());
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					
					if (fromReim && item.isProveAhead())
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
														Toast.makeText(EditItemActivity.this, "网络未连接，无法发送审批", Toast.LENGTH_SHORT).show();
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
		
		Button cancelButton = (Button)findViewById(R.id.rejectButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				goBack();
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
    	ReimApplication.showProgressDialog();
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
			Toast.makeText(EditItemActivity.this, "条目保存成功", Toast.LENGTH_SHORT).show();
			if (Utils.canSyncToServer())
			{
				SyncUtils.syncAllToServer(null);							
			}
			goBack();
		}
		else
		{
			AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
												.setTitle("保存失败")
												.setNegativeButton(R.string.confirm, null)
												.create();
			mDialog.show();
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
		
		final MemberListViewAdapater memberAdapter = new MemberListViewAdapater(this, memberList, managerCheckList);
    	View view = View.inflate(this, R.layout.profile_user, null);
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
											Toast.makeText(EditItemActivity.this, "未选择汇报对象", Toast.LENGTH_SHORT).show();
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
												sendCreateItemRequest();
											}
											else
											{
												sendModifyItemRequest();
											}											
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
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
					dbManager.insertItem(item);
					item.setLocalID(dbManager.getLastInsertItemID());
					sendApproveReportRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Toast.makeText(EditItemActivity.this, "创建条目失败", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(EditItemActivity.this, "修改条目失败", Toast.LENGTH_SHORT).show();
						}
					});			
				}
			}
		});
    }
    
    private void sendApproveReportRequest()
    {    	
    	item.setBelongReport(report);
    	dbManager.updateItem(item);    	
    	
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
							Toast.makeText(EditItemActivity.this, "创建审批报告成功", Toast.LENGTH_SHORT).show();
							goBack();
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
							Toast.makeText(EditItemActivity.this, "创建审批报告失败", Toast.LENGTH_SHORT).show();
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
								Toast.makeText(EditItemActivity.this, "未获取到任何商家, 请手动输入", Toast.LENGTH_SHORT).show();								
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
							Toast.makeText(EditItemActivity.this, "获取商家列表失败, 请手动输入", Toast.LENGTH_SHORT).show();
						}
					});					
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
    
    public class ReimLocationListener implements BDLocationListener
    {
    	public void onReceiveLocation(BDLocation location)
    	{
    		if (location != null)
    		{
    			double latitude = location.getLatitude();
    			double longitude = location.getLongitude();
    			String category = item.getCategory() == null ? "" : item.getCategory().getName();
    			sendVendorsRequest(category, latitude, longitude);
    			locationClient.stop();
    		}
    		else
    		{
    			ReimApplication.dismissProgressDialog();
    			Toast.makeText(EditItemActivity.this, "定位失败，无法获取附近商家，请手动输入商家名", Toast.LENGTH_SHORT).show();    	
    		}
    	}
    }

	private void goBack()
	{
		if (fromReim)
		{
	    	ReimApplication.setTabIndex(0);
	    	Intent intent = new Intent(EditItemActivity.this, MainActivity.class);
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