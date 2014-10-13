package com.rushucloud.reim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classes.AppPreference;
import classes.Category;
import classes.Item;
import classes.Tag;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.R;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditItemActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;

	private static AppPreference appPreference;
	private static DBManager dbManager;
	
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
	private Uri imageUri;
	
	private List<String> vendorList = null;
	private List<Category> categoryList = null;
	private List<Tag> tagList = null;
	private List<User> userList = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_edit_item);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
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
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_PICK);
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
					item.setImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
					cropImage(data.getData());
				}
				else
				{
					imageUri = Uri.parse(data.getAction());
					item.setImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
					saveBitmapToFile(item.getImage());
					this.getContentResolver().delete(imageUri, null, null);		
					invoiceImageView.setImageBitmap(item.getImage());
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
	
	private void dataInitialise()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		vendorList = new ArrayList<String>();
		vendorList.add("肯德基");
		vendorList.add("麦当劳");
		vendorList.add("必胜客");
		vendorList.add("羊蝎子");

		int currentGroupID = appPreference.getCurrentGroupID();
		categoryList = dbManager.getGroupCategories(currentGroupID);
		tagList = dbManager.getGroupTags(currentGroupID);
		userList = dbManager.getGroupUsers(currentGroupID);
		
		Intent intent = this.getIntent();
		int itemLocalID = intent.getIntExtra("itemLocalID", -1);
		item = dbManager.getItemByLocalID(itemLocalID);
		if (item == null)
		{
			item = new Item();
			item.setCategory(categoryList.get(0));
			item.setMerchant(vendorList.get(0));
		}
	}
	
	private void viewInitialise()
	{
		amountEditText = (EditText)findViewById(R.id.amountEditText);
		amountEditText.setText(Double.toString(item.getAmount()));
		
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
		
		proveAheadCheckBox = (CheckBox)findViewById(R.id.proveAheadCheckBox);
		proveAheadCheckBox.setChecked(item.isProveAhead());
		
		needReimCheckBox = (CheckBox)findViewById(R.id.needReimCheckBox);
		needReimCheckBox.setChecked(item.needReimbursed());

		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		categoryTextView.setText(categoryName);
		
		tagTextView = (TextView)findViewById(R.id.tagTextView);
		tagTextView.setText(Tag.tagListToString(item.getTags()));
		
		timeTextView = (TextView)findViewById(R.id.timeTextView);
		timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));
		
		memberTextView = (TextView)findViewById(R.id.memberTextView);
		memberTextView.setText(User.userListToString(item.getRelevantUsers()));
		
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
		
		if (item.getImage() == null)
		{
			invoiceImageView.setImageResource(R.drawable.default_invoice);
		}
		else
		{
			invoiceImageView.setImageBitmap(item.getImage());			
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
	
	private void buttonInitialise()
	{
		Button vendorButton = (Button)findViewById(R.id.vendorButton);
		vendorButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				final String vendor = item.getMerchant();
				int index = vendorList.indexOf(vendor);
				if (index == -1)
				{
					index = 0;
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
		});
		
		Button categoryButton = (Button)findViewById(R.id.categoryButton);
		categoryButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
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
		
		Button tagButton = (Button)findViewById(R.id.tagButton);
		tagButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				final boolean[] check = Tag.getTagsCheck(tagList, item.getTags());
				AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
													.setTitle(R.string.chooseTag)
													.setMultiChoiceItems(Tag.getTagNames(tagList), 
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
															tagTextView.setText(Tag.tagListToString(tags));
														}
													})
													.setNegativeButton(R.string.cancel, null)
													.create();
				mDialog.show();															
			}
		});
		
		Button timeButton = (Button)findViewById(R.id.timeButton);
		timeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				View view = View.inflate(EditItemActivity.this, R.layout.reim_date_time, null);
				
				Calendar calendar = Calendar.getInstance();
				if (item.getConsumedDate() == -1)
				{
					calendar.setTimeInMillis(System.currentTimeMillis());
					int month = calendar.get(Calendar.MONTH);
					calendar.set(Calendar.MONTH, month+1);
				}
				else
				{
					calendar.setTimeInMillis((long)item.getConsumedDate() * 1000);
				}

				final DatePicker datePicker = (DatePicker)view.findViewById(R.id.datePicker);
				final TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker);
				
				datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)-1, calendar.get(Calendar.DAY_OF_MONTH), null);
				
				timePicker.setIs24HourView(true);
				timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
				timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

				AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
													.setView(view)
													.setTitle(R.string.chooseTime)
													.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															GregorianCalendar greCal = new GregorianCalendar(datePicker.getYear(), 
																	datePicker.getMonth()+1, datePicker.getDayOfMonth(), 
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
				hideSoftKeyboard();
				final boolean[] check = User.getUsersCheck(userList, item.getRelevantUsers());
				AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
													.setTitle(R.string.member)
													.setMultiChoiceItems(User.getUserNames(userList), 
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
															memberTextView.setText(User.userListToString(users));
														}
													})
													.setNegativeButton(R.string.cancel, null)
													.create();
				mDialog.show();
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					item.setAmount(Double.valueOf(amountEditText.getText().toString()));
					item.setConsumer(dbManager.getUser(appPreference.getCurrentUserID()));
					item.setNote(noteEditText.getText().toString());
					item.setIsProveAhead(proveAheadCheckBox.isChecked());
					item.setNeedReimbursed(needReimCheckBox.isChecked());
					item.setLocalUpdatedDate(Utils.getCurrentTime());
					if (dbManager.syncItem(item))
					{
						AlertDialog mDialog = new AlertDialog.Builder(EditItemActivity.this)
															.setTitle("保存成功")
															.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
															{
																public void onClick(DialogInterface dialog, int which)
																{
																	finish();
																}
															})
															.create();
						mDialog.show();
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

    private void cropImage(Uri uri)
    {
    	Intent intent = new Intent("com.android.camera.action.CROP");
    	intent.setDataAndType(uri, "image/*");
    	intent.putExtra("crop", "true");
    	intent.putExtra("aspectX", 1);
    	intent.putExtra("aspectY", 1);
    	intent.putExtra("outputX", item.getImage().getHeight());
    	intent.putExtra("outputY", item.getImage().getHeight());
    	intent.putExtra("return-data", false);
    	startActivityForResult(intent, CROP_IMAGE);
    }

    private String getInvoiceName()
    {
    	int time = Utils.getCurrentTime();
    	
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)time * 1000);
		String result = "";
		result += calendar.get(Calendar.YEAR);
		
		if (calendar.get(Calendar.MONTH) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.MONTH);
		
		if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.DAY_OF_MONTH);
		
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.HOUR_OF_DAY);	
		
		if (calendar.get(Calendar.MINUTE) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.MINUTE) + ".jpg";
		
		return result;
    }

    private Boolean saveBitmapToFile(Bitmap bitmap)
    {
    	try
		{    		
    		Matrix matrix = new Matrix();
    		matrix.postScale((float)0.5, (float)0.5);
    		
    		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    		
    		File compressedBitmapFile = new File(appPreference.getInvoiceImageDirectory(), getInvoiceName());
    		compressedBitmapFile.createNewFile();
    		
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		bitmap.compress(CompressFormat.JPEG, 90, outputStream);
    		byte[] bitmapData = outputStream.toByteArray();
    		
    		FileOutputStream fileOutputStream = new FileOutputStream(compressedBitmapFile);
    		fileOutputStream.write(bitmapData);
    		fileOutputStream.flush();
    		fileOutputStream.close();	
    		
    		item.setInvoicePath(appPreference.getInvoiceImageDirectory() + "/" + getInvoiceName());
    		return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
    }
}