package com.rushucloud.reim;

import java.util.HashMap;
import java.util.Map;

import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.DownloadImageResponse;
import com.mechat.mechatlibrary.MCClient;
import com.mechat.mechatlibrary.MCOnlineConfig;
import com.mechat.mechatlibrary.MCUserConfig;
import com.rushucloud.reim.me.MessageActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.SendInviteActivity;
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

import classes.Group;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.Utils;
import classes.Widget.CircleImageView;
import database.DBManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MeFragment extends Fragment
{
	private boolean hasInit = false;
	
	private AppPreference appPreference;
	private DBManager dbManager;

	private View view;	
	private TextView nicknameTextView;
	private TextView companyTextView;
	private CircleImageView avatarImageView;
	
	private Group currentGroup;
	private User currentUser;
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
			hasInit = true;
	        loadProfileView();
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
		
        RelativeLayout profileLayout = (RelativeLayout) getActivity().findViewById(R.id.profileLayout);
        profileLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
				startActivity(new Intent(getActivity(), ProfileActivity.class));
			}
		});

        RelativeLayout myInvitesLayout = (RelativeLayout) getActivity().findViewById(R.id.messageLayout);
        myInvitesLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), MessageActivity.class));
			}
		});

        RelativeLayout inviteLayout = (RelativeLayout) getActivity().findViewById(R.id.inviteLayout);
        inviteLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), SendInviteActivity.class));
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
		currentGroup = appPreference.getCurrentGroup();	
		
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
		
		if (currentGroup != null)
		{
			companyTextView.setText(currentGroup.getName());
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