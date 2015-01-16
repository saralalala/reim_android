package com.rushucloud.reim.start;

import java.io.FileNotFoundException;
import java.io.IOException;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.User.ModifyUserResponse;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.User.ModifyUserRequest;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.ImageActivity;
import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class CompleteInfoActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private AppPreference appPreference;
	private DBManager dbManager;

	private ImageView avatarImageView;
	private EditText nicknameEditText;
	private PopupWindow picturePopupWindow;

	private User currentUser;
	private String avatarPath = "";
	private boolean newAvatar = false;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_complete_info);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CompleteInfoActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CompleteInfoActivity");
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
					avatarPath = Utils.saveBitmapToFile(bitmap, NetworkConstant.IMAGE_TYPE_AVATAR);
					newAvatar = true;
					avatarImageView.setImageBitmap(bitmap);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(CompleteInfoActivity.this, SignInActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
        currentUser = AppPreference.getAppPreference().getCurrentUser();
	}
	
	private void initView()
	{
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(CompleteInfoActivity.this, SignInActivity.class));
				finish();
			}
		});

		avatarImageView = (ImageView) findViewById(R.id.avatarImageView);
		avatarImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!avatarPath.equals(""))
				{
					Intent intent = new Intent(CompleteInfoActivity.this, ImageActivity.class);
					intent.putExtra("imagePath", avatarPath);
					startActivity(intent);
				}
			}
		});
		avatarImageView.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				showPictureWindow();
				return false;
			}
		});
		
		nicknameEditText = (EditText)findViewById(R.id.nicknameEditText);
		nicknameEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());

		Button completeButton = (Button)findViewById(R.id.completeButton);
		completeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(CompleteInfoActivity.this, "UMENG_LOGIN");
				hideSoftKeyboard();
				
				String nickname = nicknameEditText.getText().toString();
				currentUser.setNickname(nickname);
				
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(CompleteInfoActivity.this, R.string.error_update_network_unavailable);
					startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
					finish();
				}
				else if (!newAvatar && nickname.equals(""))
				{
					startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
					finish();
				}
				else if (newAvatar)
				{
					ReimProgressDialog.show();
					sendUploadAvatarRequest();
				}
				else
				{
					ReimProgressDialog.show();
					sendModifyUserInfoRequest();
				}
			}
		});
		completeButton = Utils.resizeLongButton(completeButton);
		
		// init picture window
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
		
		RelativeLayout baseLayout = (RelativeLayout)findViewById(R.id.baseLayout);
		baseLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
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

    private void showPictureWindow()
    {
		picturePopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		Utils.dimBackground(this);
    }
    
    private void sendUploadAvatarRequest()
    {
		UploadImageRequest request = new UploadImageRequest(avatarPath, NetworkConstant.IMAGE_TYPE_AVATAR);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final UploadImageResponse response = new UploadImageResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					currentUser.setAvatarID(response.getImageID());
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(currentTime);
					currentUser.setServerUpdatedDate(currentTime);
					dbManager.updateUser(currentUser);
					newAvatar = false;
					
					if (currentUser.getNickname().equals(""))
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ReimProgressDialog.dismiss();
								Utils.showToast(CompleteInfoActivity.this, R.string.succeed_in_modifying_user_info);
								startActivity(new Intent(CompleteInfoActivity.this, WelcomeActivity.class));
								finish();
							}
						});	
					}
					else
					{
						sendModifyUserInfoRequest();
					}
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							Utils.showToast(CompleteInfoActivity.this, R.string.failed_to_upload_avatar, response.getErrorMessage());
						}
					});				
				}
			}
		});
    }

	private void sendModifyUserInfoRequest()
	{
		ModifyUserRequest request = new ModifyUserRequest(currentUser);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
				if (response.getStatus())
				{
					dbManager.updateUser(currentUser);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							Utils.showToast(CompleteInfoActivity.this, R.string.succeed_in_modifying_user_info);
							startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
							finish();
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
							Utils.showToast(CompleteInfoActivity.this, R.string.failed_to_upload_avatar, response.getErrorMessage());
						}
					});						
				}
			}
		});
	}

	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
	}
}