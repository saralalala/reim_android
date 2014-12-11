package com.rushucloud.reim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import netUtils.HttpConstant;
import netUtils.Request.CommonRequest;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.User.InviteRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.CommonResponse;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.User.InviteResponse;

import com.mechat.mechatlibrary.MCClient;
import com.mechat.mechatlibrary.MCOnlineConfig;
import com.mechat.mechatlibrary.MCUserConfig;
import com.rushucloud.reim.me.InviteActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ManagerActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.SettingsActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import classes.AppPreference;
import classes.Group;
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import classes.Widget.CircleImageView;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MeFragment extends Fragment
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private boolean hasInit = false;
	
	private AppPreference appPreference;
	private DBManager dbManager;

	private View view;	
	private TextView nicknameTextView;
	private TextView companyTextView;	
	private CircleImageView avatarImageView;
	private PopupWindow picturePopupWindow;
	
	private User currentUser;
	private Uri originalImageUri;
	private String avatarPath;
	
	private UMSocialService mController;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.fragment_me, container, false);
		}
		else
		{
			ViewGroup viewGroup = (ViewGroup) view.getParent();
			if (viewGroup != null)
			{
				viewGroup.removeView(view);
			}
		}
        currentUser = AppPreference.getAppPreference().getCurrentUser();
	    return view;
	}

	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("MeFragment");
		if (!hasInit)
		{
			initData();
	        initView();
	        loadProfileView();
			hasInit = true;
		}
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MeFragment");
	}

	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && hasInit)
		{
	        loadProfileView();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK && data != null)
		{
			try
			{
				if (requestCode == PICK_IMAGE)
				{
					originalImageUri = null;
					cropImage(data.getData());
				}
				else if (requestCode == TAKE_PHOTO)
				{
					originalImageUri = data.getData();
					cropImage(data.getData());					
				}
				else if (requestCode == CROP_IMAGE)
				{
					Uri newImageUri = Uri.parse(data.getAction());
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), newImageUri);
					avatarPath = Utils.saveBitmapToFile(bitmap, HttpConstant.IMAGE_TYPE_AVATAR);
					
					if (!avatarPath.equals("") && Utils.isNetworkConnected())
					{
						sendUploadAvatarRequest();
					}
					else if (avatarPath.equals(""))
					{
						Utils.showToast(getActivity(), "头像保存失败");						
					}
					else
					{
						Utils.showToast(getActivity(), "网络未连接，无法上传头像");
					}				
					
					if (originalImageUri != null)
					{
						getActivity().getContentResolver().delete(originalImageUri, null, null);							
					}
					getActivity().getContentResolver().delete(newImageUri, null, null);	
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
		if (ssoHandler != null)
		{
			ssoHandler.authorizeCallBack(requestCode, requestCode, data);
		}		
	}
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
	}
	
	private void initView()
	{
		nicknameTextView = (TextView) getActivity().findViewById(R.id.nicknameTextView);
		companyTextView = (TextView) getActivity().findViewById(R.id.companyTextView);	
		
		avatarImageView = (CircleImageView) getActivity().findViewById(R.id.avatarImageView);
		avatarImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (currentUser != null && !currentUser.getAvatarPath().equals(""))
				{
					Intent intent = new Intent(getActivity(), ImageActivity.class);
					intent.putExtra("imagePath", currentUser.getAvatarPath());
					getActivity().startActivity(intent);
				}
			}
		});
		avatarImageView.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				showPictureDialog();
				return false;
			}
		});
		
		final View pictureView = View.inflate(getActivity(), R.layout.window_picture, null); 

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
				params.height = (int)(cameraButton.getWidth() * ratio);;
				cancelButton.setLayoutParams(params);
			}
		});
		
		picturePopupWindow = Utils.constructPopupWindow(getActivity(), pictureView);
        
        RelativeLayout profileLayout = (RelativeLayout) getActivity().findViewById(R.id.profileLayout);
        profileLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
				startActivity(new Intent(getActivity(), ProfileActivity.class));
			}
		});
        
        RelativeLayout defaultManagerLayout = (RelativeLayout) getActivity().findViewById(R.id.defaultManagerLayout);
        defaultManagerLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (currentUser.getGroupID() <= 0)
				{
					Utils.showToast(getActivity(), "你还没加入任何组");			
				}
				else
				{
					startActivity(new Intent(getActivity(), ManagerActivity.class));
				}
			}
		});
        
        RelativeLayout inviteLayout = (RelativeLayout) getActivity().findViewById(R.id.inviteLayout);
        inviteLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_INVITE");
				if (Utils.isNetworkConnected())
				{
					showInviteDialog();		
				}
				else
				{
					Utils.showToast(getActivity(), "网络未连接，无法发送邀请");
				}
			}
		});
        
        RelativeLayout myInvitesLayout = (RelativeLayout) getActivity().findViewById(R.id.myInvitesLayout);
        myInvitesLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), InviteActivity.class));
			}
		});
        
        RelativeLayout invoiceLayout = (RelativeLayout) getActivity().findViewById(R.id.invoiceLayout);
        invoiceLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), InvoiceTitleActivity.class));
			}
		});
        
        RelativeLayout settingsLayout = (RelativeLayout) getActivity().findViewById(R.id.settingsLayout);
        settingsLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});
        
        RelativeLayout customServiceLayout = (RelativeLayout) getActivity().findViewById(R.id.customServiceLayout);
        customServiceLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showFeedbackDialog();
			}
		});        
        
//        RelativeLayout shareLayout = (RelativeLayout) getActivity().findViewById(R.id.shareLayout);
//        shareLayout.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_RECOMMEND");
//				showShareDialog();
//			}
//		});
        
        mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	}

	private void loadProfileView()
	{	
		currentUser = appPreference.getCurrentUser();
		Group group = dbManager.getGroup(appPreference.getCurrentGroupID());	
		
		if (currentUser != null)
		{
			if (!currentUser.getAvatarPath().equals(""))
			{
				Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatarPath());
				if (bitmap != null)
				{
					avatarImageView.setImageBitmap(bitmap);						
				}
			}
			
			nicknameTextView.setText(currentUser.getNickname());					
		}
		else
		{
			avatarImageView.setImageResource(R.drawable.default_avatar);
			nicknameTextView.setText(R.string.not_available);
		}
		
		if (group != null)
		{
			companyTextView.setText(group.getName());
		}
		else
		{
			companyTextView.setText(R.string.not_available);
		}
        
		if (Utils.isNetworkConnected())
		{
		    if (currentUser.hasUndownloadedAvatar())
			{
		        sendDownloadAvatarRequest();			
			}
		}
	}
	
    private void cropImage(Uri uri)
    {
		try
		{
	    	Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
	    	Intent intent = new Intent("com.android.camera.action.CROP");
	    	intent.setDataAndType(uri, "image/*");
	    	intent.putExtra("crop", "true");
	    	intent.putExtra("aspectX", 1);
	    	intent.putExtra("aspectY", 1);
	    	intent.putExtra("outputX", bitmap.getWidth());
	    	intent.putExtra("outputY", bitmap.getWidth());
	    	intent.putExtra("return-data", false);
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

    private void showPictureDialog()
    {
		picturePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		picturePopupWindow.update();

		Utils.dimBackground(getActivity());
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
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							loadProfileView();
							Utils.showToast(getActivity(), "头像上传成功");
						}
					});	
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							Utils.showToast(getActivity(), "头像上传失败");
						}
					});				
				}
			}
		});
    }

    private void sendDownloadAvatarRequest()
    {
    	DownloadImageRequest request = new DownloadImageRequest(currentUser.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					int currentTime = Utils.getCurrentTime();
					avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(currentTime);
					currentUser.setServerUpdatedDate(currentTime);
					if (dbManager.updateUser(currentUser))
					{
						getActivity().runOnUiThread(new Runnable()
						{
							public void run()
							{
								loadProfileView();
							}
						});						
					}
					else
					{
						getActivity().runOnUiThread(new Runnable()
						{
							public void run()
							{
								Utils.showToast(getActivity(), "头像保存失败");
							}
						});						
					}
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							Utils.showToast(getActivity(), "头像下载失败");
						}
					});						
				}
			}
		});
    }

    private void showInviteDialog()
    {
		View view = View.inflate(getActivity(), R.layout.me_invite_dialog, null);
		final EditText usernameEditText = (EditText)view.findViewById(R.id.usernameEditText);
		usernameEditText.requestFocus();
		
    	AlertDialog mDialog = new AlertDialog.Builder(getActivity())
								.setTitle("邀请")
								.setView(view)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String username = usernameEditText.getText().toString();
										if (username.equals(""))
										{
											Utils.showToast(getActivity(), "手机号或邮箱不能为空");
										}
										else if (!Utils.isEmailOrPhone(username))
										{
											Utils.showToast(getActivity(), "手机号或邮箱格式不正确");
										}
										else
										{
											sendInviteRequest(username);
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
    
    private void sendInviteRequest(String username)
    {
    	ReimApplication.showProgressDialog();
    	InviteRequest request = new InviteRequest(username);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final InviteResponse response = new InviteResponse(httpResponse);
				if (response.getStatus())
				{
					sendCommonRequest();
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							Utils.showToast(getActivity(), "邀请发送失败，" + response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
    private void sendCommonRequest()
    {
    	ReimApplication.showProgressDialog();
    	CommonRequest request = new CommonRequest();
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);
				if (response.getStatus())
				{
					int currentGroupID = response.getGroup().getServerID();

					// update AppPreference
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setCurrentGroupID(currentGroupID);
					appPreference.saveAppPreference();

					// update members
					DBManager dbManager = DBManager.getDBManager();
					dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

					User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
					if (localUser.getServerUpdatedDate() == response.getCurrentUser().getServerUpdatedDate())
					{
						if (localUser.getAvatarPath().equals(""))
						{
							dbManager.updateUser(response.getCurrentUser());
						}
					}
					else
					{
						dbManager.syncUser(response.getCurrentUser());
					}

					// update categories
					dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

					// update tags
					dbManager.updateGroupTags(response.getTagList(), currentGroupID);

					// update group info
					dbManager.syncGroup(response.getGroup());
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							Utils.showToast(getActivity(), "邀请已发送");
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							Utils.showToast(getActivity(), "邀请发送失败，" + response.getErrorMessage());
						}
					});
				}
			}
		});
    }

    @SuppressWarnings("unused")
	private void showShareDialog()
    {
    	String appID = "wx16afb8ec2cc4dc19";
    	String appSecret = "2e97f0d75dd7f371803785172682893a";
    	
    	UMWXHandler wxHandler = new UMWXHandler(getActivity(), appID, appSecret);
    	wxHandler.addToSocialSDK();
    	
    	UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(), appID, appSecret);
    	wxCircleHandler.setToCircle(true);
    	wxCircleHandler.addToSocialSDK();
    	
    	SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
    	sinaSsoHandler.addToSocialSDK();
    	
    	UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(), "1103305832", "l8eKHcEiAMCnhV50");
    	qqSsoHandler.addToSocialSDK();

    	WeiXinShareContent weiXinShareContent = new WeiXinShareContent();
    	weiXinShareContent.setShareContent("这是来自XAndroid版的微信分享");
    	weiXinShareContent.setTitle("微信分享");
    	weiXinShareContent.setTargetUrl("http://www.rushucloud.com");    	
    	mController.setShareMedia(weiXinShareContent);

    	CircleShareContent circleShareContent = new CircleShareContent();
    	circleShareContent.setShareContent("这是来自XAndroid版的朋友圈分享");
    	circleShareContent.setTitle("朋友圈分享");
    	circleShareContent.setTargetUrl("http://www.rushucloud.com");    
    	mController.setShareMedia(circleShareContent);

    	SinaShareContent sinaShareContent = new SinaShareContent();
    	sinaShareContent.setShareContent("这是来自XAndroid版的新浪微博分享");
    	sinaShareContent.setTitle("新浪微博分享");
    	sinaShareContent.setTargetUrl("http://www.rushucloud.com");    
    	mController.setShareMedia(sinaShareContent);

    	QQShareContent qqShareContent = new QQShareContent();
    	qqShareContent.setShareContent("这是来自XAndroid版的QQ分享");
    	qqShareContent.setTitle("QQ分享");
    	qqShareContent.setTargetUrl("http://www.rushucloud.com");    
    	mController.setShareMedia(qqShareContent);

    	mController.getConfig().removePlatform(SHARE_MEDIA.QZONE, SHARE_MEDIA.TENCENT);
    	mController.getConfig().registerListener(new SnsPostListener()
    	{
    		public void onStart()
    		{
    			
    		}
    		
    		public void onComplete(SHARE_MEDIA platform, int stCode, SocializeEntity entity)
    		{
    			
    		}
    	});
    	
		mController.openShare(getActivity(), false);
    }

    private void showFeedbackDialog()
    {
		try
		{
			PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
	    	
			MCOnlineConfig onlineConfig = new MCOnlineConfig();
			MCClient.getInstance().startMCConversationActivity(onlineConfig);
			
			MCUserConfig mcUserConfig = new MCUserConfig();
			Map<String, String> userInfoExtra = new HashMap<String, String>();
			userInfoExtra.put("AndroidVersion",Integer.toString(Build.VERSION.SDK_INT));
			userInfoExtra.put("AppVersion", info.versionName);
			mcUserConfig.setUserInfo(getActivity(), null, userInfoExtra, null);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
    }
}