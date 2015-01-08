package com.rushucloud.reim.me;

import java.io.FileNotFoundException;
import java.io.IOException;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.Group.ModifyGroupRequest;
import netUtils.Request.User.ModifyUserRequest;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.Group.ModifyGroupResponse;
import netUtils.Response.User.ModifyUserResponse;

import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;
import classes.Group;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.DBManager;
import classes.Utils.Utils;
import classes.Widget.CircleImageView;
import classes.Widget.ReimProgressDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private CircleImageView avatarImageView;
	private PopupWindow picturePopupWindow;
	
	private TextView emailTextView;
	private PopupWindow emailPopupWindow;
	private EditText emailEditText;

	private TextView phoneTextView;
	private PopupWindow phonePopupWindow;
	private EditText phoneEditText;

	private TextView nicknameTextView;
	private PopupWindow nicknamePopupWindow;
	private EditText nicknameEditText;
	
	private TextView companyTextView;
	private ImageView companyNextImageView;
	private RelativeLayout companyLayout;
	private PopupWindow companyPopupWindow;
	private EditText companyEditText;
	
	private TextView managerTextView;

	private AppPreference appPreference;
	private DBManager dbManager;

	private Group currentGroup;
	private User currentUser;
	private String avatarPath;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_profile);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ProfileActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		loadInfoView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ProfileActivity");
		MobclickAgent.onPause(this);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			try
			{
				if (requestCode == PICK_IMAGE)
				{
					cropImage(data.getData());	
				}
				else if (requestCode == TAKE_PHOTO)
				{
					cropImage(appPreference.getTempAvatarUri());					
				}
				else if (requestCode == CROP_IMAGE)
				{
					Bitmap bitmap = BitmapFactory.decodeFile(appPreference.getTempAvatarPath());
					avatarPath = Utils.saveBitmapToFile(bitmap, HttpConstant.IMAGE_TYPE_AVATAR);
					
					if (!avatarPath.equals("") && Utils.isNetworkConnected())
					{
						Utils.showToast(this, "头像保存成功，正在上传");
						avatarImageView.setImageBitmap(bitmap);
						sendUploadAvatarRequest();
					}
					else if (avatarPath.equals(""))
					{
						Utils.showToast(this, "头像保存失败");
					}
					else
					{
						Utils.showToast(this, "网络未连接，无法上传头像");
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		currentUser = appPreference.getCurrentUser();
		currentGroup = appPreference.getCurrentGroup();
	}	
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		initAvatarView();
		initEmailView();
		initPhoneView();
		initNicknameView();
		initCompanyView();
		initManagerView();
		initPasswordView();
	}
	
	private void initAvatarView()
	{
		// init avatar
		RelativeLayout avatarLayout = (RelativeLayout) findViewById(R.id.avatarLayout);
		avatarLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showPictureWindow();
			}
		});
        
		avatarImageView = (CircleImageView) findViewById(R.id.avatarImageView);
		avatarImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (currentUser != null && !currentUser.getAvatarPath().equals(""))
				{
					Intent intent = new Intent(ProfileActivity.this, ImageActivity.class);
					intent.putExtra("imagePath", currentUser.getAvatarPath());
					startActivity(intent);
				}
			}
		});
		
		// init avatar window
		View pictureView = View.inflate(this, R.layout.window_picture, null);
		
		Button cameraButton = (Button) pictureView.findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempAvatarUri());
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
	
	private void initEmailView()
	{
		// init email
		emailTextView = (TextView) findViewById(R.id.emailTextView);
		
		RelativeLayout emailLayout = (RelativeLayout) findViewById(R.id.emailLayout);
		emailLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showEmailWindow();
				emailEditText.requestFocus();
			}
		});
        
        // init email window
    	View emailView = View.inflate(this, R.layout.window_me_email, null);
    	
    	emailEditText = (EditText) emailView.findViewById(R.id.emailEditText);
    	emailEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
    	if (currentUser != null)
		{
        	emailEditText.setText(currentUser.getEmail());			
		}
		
		ImageView backImageView = (ImageView) emailView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				emailPopupWindow.dismiss();
			}
		});
		
		TextView saveTextView = (TextView) emailView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalEmail = currentUser.getEmail();
				String newEmail = emailEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法修改");			
				}
				else if (newEmail.equals(originalEmail))
				{
					Utils.showToast(ProfileActivity.this, "邮箱与原有相同，无需修改");
				}
				else if (newEmail.equals(""))
				{
					Utils.showToast(ProfileActivity.this, "新邮箱不可为空");
				}
				else
				{
					currentUser.setEmail(newEmail);
					sendModifyUserInfoRequest();
				}
			}
		});

        LinearLayout baseLayout = (LinearLayout) emailView.findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
        
		emailPopupWindow = Utils.constructHorizontalPopupWindow(this, emailView);
	}
	
	private void initPhoneView()
	{
		// init phone
		phoneTextView = (TextView) findViewById(R.id.phoneTextView);
		
		RelativeLayout phoneLayout = (RelativeLayout) findViewById(R.id.phoneLayout);
		phoneLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showPhoneWindow();
				phoneEditText.requestFocus();
			}
		});
        
        // init phone window
    	View phoneView = View.inflate(this, R.layout.window_me_phone, null);
    	
    	phoneEditText = (EditText) phoneView.findViewById(R.id.phoneEditText);
    	phoneEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
    	if (currentUser != null)
		{
        	phoneEditText.setText(currentUser.getPhone());			
		}
		
		ImageView backImageView = (ImageView) phoneView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				phonePopupWindow.dismiss();
			}
		});
		
		TextView saveTextView = (TextView) phoneView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalPhone = currentUser.getPhone();
				String newPhone = phoneEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法修改");			
				}
				else if (!Utils.isPhone(newPhone))
				{
					Utils.showToast(ProfileActivity.this, "手机号格式不正确");
				}
				else if (newPhone.equals(originalPhone))
				{
					Utils.showToast(ProfileActivity.this, "手机与原有相同，无需修改");
				}
				else if (newPhone.equals(""))
				{
					Utils.showToast(ProfileActivity.this, "新手机不可为空");
				}
				else
				{
					currentUser.setPhone(newPhone);
					sendModifyUserInfoRequest();
				}
			}
		});

        LinearLayout baseLayout = (LinearLayout) phoneView.findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
        
		phonePopupWindow = Utils.constructHorizontalPopupWindow(this, phoneView);
	}
	
	private void initNicknameView()
	{
		// init nickname
		nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
		
		RelativeLayout nicknameLayout = (RelativeLayout) findViewById(R.id.nicknameLayout);
		nicknameLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showNicknameWindow();
				nicknameEditText.requestFocus();
			}
		});
        
        // init nickname window
    	View nicknameView = View.inflate(this, R.layout.window_me_nickname, null);
    	
    	nicknameEditText = (EditText) nicknameView.findViewById(R.id.nicknameEditText);
    	nicknameEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
    	if (currentUser != null)
		{
        	nicknameEditText.setText(currentUser.getNickname());			
		}
		
		ImageView backImageView = (ImageView) nicknameView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				nicknamePopupWindow.dismiss();
			}
		});
		
		TextView saveTextView = (TextView) nicknameView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalNickname = currentUser.getNickname();
				String newNickname = nicknameEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法修改");			
				}
				else if (newNickname.equals(originalNickname))
				{
					Utils.showToast(ProfileActivity.this, "昵称与原有相同，无需修改");
				}
				else if (newNickname.equals(""))
				{
					Utils.showToast(ProfileActivity.this, "新昵称不可为空");
				}
				else
				{
					currentUser.setNickname(newNickname);
					sendModifyUserInfoRequest();
				}
			}
		});

        LinearLayout baseLayout = (LinearLayout) nicknameView.findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
        
		nicknamePopupWindow = Utils.constructHorizontalPopupWindow(this, nicknameView);
	}
	
	private void initCompanyView()
	{
		// init company
		companyTextView = (TextView) findViewById(R.id.companyTextView);
		companyNextImageView = (ImageView) findViewById(R.id.companyNextImageView);
		
		companyLayout = (RelativeLayout) findViewById(R.id.companyLayout);
        companyLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				showCompanyWindow();
				companyEditText.requestFocus();
			}
		});
        
        // init company window
    	View companyView = View.inflate(this, R.layout.window_me_company, null);
    	
    	companyEditText = (EditText) companyView.findViewById(R.id.companyEditText);
    	companyEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
    	if (currentGroup != null)
		{
        	companyEditText.setText(currentGroup.getName());			
		}
		
		ImageView backImageView = (ImageView) companyView.findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				companyPopupWindow.dismiss();
			}
		});
		
		TextView saveTextView = (TextView) companyView.findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalName = currentGroup.getName();
				String newName = companyEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法修改");			
				}
				else if (newName.equals(originalName))
				{
					Utils.showToast(ProfileActivity.this, "名称与原有相同，无需修改");
				}
				else if (newName.equals(""))
				{
					Utils.showToast(ProfileActivity.this, "新名称不可为空");
				}
				else
				{
					ReimProgressDialog.show();
					sendModifyGroupRequest(newName);
				}
			}
		});

        LinearLayout baseLayout = (LinearLayout) companyView.findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
        
		companyPopupWindow = Utils.constructHorizontalPopupWindow(this, companyView);
	}

	private void initManagerView()
	{
		managerTextView = (TextView)findViewById(R.id.managerTextView);
		
        RelativeLayout defaultManagerLayout = (RelativeLayout) findViewById(R.id.defaultManagerLayout);
        defaultManagerLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (currentUser.getGroupID() <= 0)
				{
					Utils.showToast(ProfileActivity.this, "你还没加入任何组");			
				}
				else
				{
					startActivity(new Intent(ProfileActivity.this, ManagerActivity.class));
				}
			}
		});
	}
	
	private void initPasswordView()
	{
        RelativeLayout passwordLayout = (RelativeLayout) findViewById(R.id.passwordLayout);
        passwordLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
				overridePendingTransition(R.anim.window_horizontal_in, 0);
			}
		});
	}	
	
	private void loadInfoView()
	{		
		currentUser = appPreference.getCurrentUser();
		currentGroup = appPreference.getCurrentGroup();
		currentUser.setIsAdmin(true);
		
		if (!currentUser.getAvatarPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatarPath());
			if (bitmap != null)
			{
				avatarImageView.setImageBitmap(bitmap);
			}
		}
		
		String email = currentUser != null && !currentUser.getEmail().equals("") ? currentUser.getEmail() : getString(R.string.not_binding);
		emailTextView.setText(email);
		
		String phone = currentUser != null && !currentUser.getPhone().equals("") ? currentUser.getPhone() : getString(R.string.not_binding);
		phoneTextView.setText(phone);
		
		String nickname = currentUser != null && !currentUser.getNickname().equals("") ? currentUser.getNickname() : getString(R.string.null_string);
		nicknameTextView.setText(nickname);
		
		User manager = dbManager.getUser(currentUser.getDefaultManagerID());
		if (manager != null)
		{
			managerTextView.setText(manager.getNickname());			
		}
		
		String companyName = currentGroup != null ? currentGroup.getName() : getString(R.string.null_string);	
		companyTextView.setText(companyName);
		
        if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
		{
        	companyLayout.setClickable(false);
        	companyNextImageView.setVisibility(View.GONE);			
		}
        else
        {
        	companyLayout.setClickable(true);
        	companyNextImageView.setVisibility(View.VISIBLE);
        }
	}

    private void showPictureWindow()
    {
		picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		Utils.dimBackground(this);
    }
    
    private void showEmailWindow()
    {    	
		emailPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		emailPopupWindow.update();
    }
    
    private void showPhoneWindow()
    {
		phonePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		phonePopupWindow.update();
    }
    
    private void showNicknameWindow()
    {
		nicknamePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		nicknamePopupWindow.update();
    }
    
    private void showCompanyWindow()
    {
		companyPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		companyPopupWindow.update();
    }

    private void cropImage(Uri uri)
    {
		try
		{
	    	Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
	    	Intent intent = new Intent("com.android.camera.action.CROP");
	    	intent.setDataAndType(uri, "image/*");
	    	intent.putExtra("crop", "true");
	    	intent.putExtra("aspectX", 1);
	    	intent.putExtra("aspectY", 1);
	    	intent.putExtra("outputX", bitmap.getWidth());
	    	intent.putExtra("outputY", bitmap.getWidth());
	    	intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempAvatarUri());
	    	intent.putExtra("return-data", false);
	    	intent.putExtra("noFaceDetection", true);
	    	startActivityForResult(intent, CROP_IMAGE);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }

    private void sendUploadAvatarRequest()
    {
		UploadImageRequest request = new UploadImageRequest(avatarPath, HttpConstant.IMAGE_TYPE_AVATAR);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					DBManager dbManager = DBManager.getDBManager();
					currentUser.setAvatarID(response.getImageID());
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(currentTime);
					currentUser.setServerUpdatedDate(currentTime);
					dbManager.updateUser(currentUser);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							loadInfoView();
							Utils.showToast(ProfileActivity.this, "头像上传成功");
						}
					});	
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							loadInfoView();
							Utils.showToast(ProfileActivity.this, "头像上传失败");
						}
					});				
				}
			}
		});
    }
    
	private void sendModifyUserInfoRequest()
	{
		ReimProgressDialog.show();		
		ModifyUserRequest request = new ModifyUserRequest(currentUser);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyUserResponse response = new ModifyUserResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.updateUser(currentUser);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							Utils.showToast(ProfileActivity.this, "用户信息修改成功");
							emailPopupWindow.dismiss();
							phonePopupWindow.dismiss();
							nicknamePopupWindow.dismiss();
							companyPopupWindow.dismiss();
							loadInfoView();
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
							Utils.showToast(ProfileActivity.this, "用户信息修改失败");
							loadInfoView();
						}
					});						
				}
			}
		});
	}
	
	private void sendModifyGroupRequest(final String newName)
	{
		ModifyGroupRequest request = new ModifyGroupRequest(newName);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyGroupResponse response = new ModifyGroupResponse(httpResponse);
				if (response.getStatus())
				{
					currentGroup.setName(newName);
					dbManager.updateGroup(currentGroup);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							companyPopupWindow.dismiss();
							Utils.showToast(ProfileActivity.this, "修改成功");
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
							companyPopupWindow.dismiss();
							Utils.showToast(ProfileActivity.this, "修改失败");
						}
					});
				}
			}
		});
	}

	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);
	}
}