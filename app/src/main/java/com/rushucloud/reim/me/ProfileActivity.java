package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.SingleImageActivity;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.FileNotFoundException;
import java.io.IOException;

import classes.Group;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.request.UploadImageRequest;
import netUtils.request.user.SignOutRequest;
import netUtils.response.UploadImageResponse;
import netUtils.response.user.SignOutResponse;

public class ProfileActivity extends Activity
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private CircleImageView avatarImageView;
	private PopupWindow picturePopupWindow;
	
	private TextView emailTextView;
	private TextView phoneTextView;
	private TextView nicknameTextView;
	
	private TextView companyTextView;
	private ImageView companyNextImageView;
	private RelativeLayout companyLayout;

	private AppPreference appPreference;

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
		ReimProgressDialog.setContext(this);
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
				switch (requestCode)
				{
					case PICK_IMAGE:
					{
						cropImage(data.getData());
						break;
					}
					case TAKE_PHOTO:
					{
						cropImage(appPreference.getTempAvatarUri());
						break;
					}
					case CROP_IMAGE:
					{
						Bitmap bitmap = BitmapFactory.decodeFile(appPreference.getTempAvatarPath());
						avatarPath = PhoneUtils.saveBitmapToFile(bitmap, NetworkConstant.IMAGE_TYPE_AVATAR);
						
						if (!avatarPath.isEmpty() && PhoneUtils.isNetworkConnected())
						{
							ViewUtils.showToast(this, R.string.succeed_in_saving_avatar);
							avatarImageView.setImageBitmap(bitmap);
							sendUploadAvatarRequest();
						}
						else if (avatarPath.isEmpty())
						{
							ViewUtils.showToast(this, R.string.failed_to_save_avatar);
						}
						else
						{
							ViewUtils.showToast(this, R.string.error_upload_avatar_network_unavailable);
						}
						break;
					}
					default:
						break;
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

        TextView signOutTextView = (TextView) findViewById(R.id.signOutTextView);
        signOutTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String message = getString(R.string.prompt_sign_out) + currentUser.getNickname();
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle(R.string.tip);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (PhoneUtils.isNetworkConnected())
                        {
                            sendSignOutRequest();
                        }
                        else
                        {
                            ViewUtils.showToast(ProfileActivity.this, R.string.error_sign_out_network_unavailable);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        });

		initAvatarView();

        // init nickname
        nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);

        RelativeLayout nicknameLayout = (RelativeLayout) findViewById(R.id.nicknameLayout);
        nicknameLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(ProfileActivity.this, NicknameActivity.class));
            }
        });

		// init email
		emailTextView = (TextView) findViewById(R.id.emailTextView);
		
		RelativeLayout emailLayout = (RelativeLayout) findViewById(R.id.emailLayout);
		emailLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ProfileActivity.this, EmailActivity.class));
			}
		});

		// init phone
		phoneTextView = (TextView) findViewById(R.id.phoneTextView);
		
		RelativeLayout phoneLayout = (RelativeLayout) findViewById(R.id.phoneLayout);
		phoneLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ProfileActivity.this, PhoneActivity.class));
			}
		});

		// init company
		companyTextView = (TextView) findViewById(R.id.companyTextView);
		companyNextImageView = (ImageView) findViewById(R.id.companyNextImageView);
		
		companyLayout = (RelativeLayout) findViewById(R.id.companyLayout);
        companyLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ProfileActivity.this, CompanyActivity.class));
			}
		});

        // init password
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
				if (currentUser != null && !currentUser.getAvatarPath().isEmpty())
				{
					Intent intent = new Intent(ProfileActivity.this, SingleImageActivity.class);
					intent.putExtra("imagePath", currentUser.getAvatarPath());
					startActivity(intent);
				}
				else if (currentUser != null)
				{
					showPictureWindow();					
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
		
		Button cancelButton = (Button) pictureView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				picturePopupWindow.dismiss();
			}
		});
		
		picturePopupWindow = ViewUtils.buildBottomPopupWindow(this, pictureView);
	}
	
	private void loadInfoView()
	{		
		currentUser = appPreference.getCurrentUser();
		currentGroup = appPreference.getCurrentGroup();
		currentUser.setIsAdmin(true);

        ViewUtils.setImageViewBitmap(currentUser, avatarImageView);
		
		String email = currentUser != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : getString(R.string.not_binding);
		emailTextView.setText(email);
		
		String phone = currentUser != null && !currentUser.getPhone().isEmpty() ? currentUser.getPhone() : getString(R.string.not_binding);
		phoneTextView.setText(phone);
		
		String nickname = currentUser != null && !currentUser.getNickname().isEmpty() ? currentUser.getNickname() : getString(R.string.empty);
		nicknameTextView.setText(nickname);
		
		String companyName = currentGroup != null ? currentGroup.getName() : getString(R.string.empty);	
		companyTextView.setText(companyName);
		
        if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
		{
        	companyLayout.setClickable(false);
        	companyNextImageView.setVisibility(View.INVISIBLE);			
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

		ViewUtils.dimBackground(this);
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
		UploadImageRequest request = new UploadImageRequest(avatarPath, NetworkConstant.IMAGE_TYPE_AVATAR);
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
							ViewUtils.showToast(ProfileActivity.this, R.string.succeed_in_uploading_avatar);
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
							ViewUtils.showToast(ProfileActivity.this, R.string.failed_to_upload_avatar);
						}
					});				
				}
			}
		});
    }

    private void sendSignOutRequest()
    {
        ReimProgressDialog.show();
        SignOutRequest request = new SignOutRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                SignOutResponse response = new SignOutResponse(httpResponse);
                if (response.getStatus())
                {
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setCurrentUserID(-1);
                    appPreference.setCurrentGroupID(-1);
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.setServerToken("");
                    appPreference.setLastSyncTime(0);
                    appPreference.saveAppPreference();

                    ReimApplication.setTabIndex(0);
                    ReimApplication.setReportTabIndex(0);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
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
                            ViewUtils.showToast(ProfileActivity.this, R.string.failed_to_sign_out);
                        }
                    });
                }
            }
        });
    }

}