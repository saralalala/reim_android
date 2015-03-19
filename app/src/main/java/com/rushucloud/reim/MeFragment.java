package com.rushucloud.reim;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mechat.mechatlibrary.MCClient;
import com.mechat.mechatlibrary.MCOnlineConfig;
import com.mechat.mechatlibrary.MCUserConfig;
import com.rushucloud.reim.me.AboutActivity;
import com.rushucloud.reim.me.CategoryActivity;
import com.rushucloud.reim.me.FeedbackActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ManagerActivity;
import com.rushucloud.reim.me.MessageListActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.SendInviteActivity;
import com.rushucloud.reim.me.TagActivity;
import com.rushucloud.reim.start.SignInActivity;
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

import java.util.HashMap;
import java.util.Map;

import classes.Group;
import classes.utils.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.User.SignOutRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.User.SignOutResponse;
import netUtils.URLDef;

public class MeFragment extends Fragment
{
	private boolean hasInit = false;
	
	private AppPreference appPreference;
	private DBManager dbManager;

	private View view;	
	private TextView nicknameTextView;
	private TextView companyTextView;
	private CircleImageView avatarImageView;
    private ImageView tipImageView;
    private TextView managerTextView;

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
        TextView signOutTextView = (TextView) getActivity().findViewById(R.id.signOutTextView);
        signOutTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String message = getString(R.string.prompt_sign_out) + currentUser.getNickname();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                            ViewUtils.showToast(getActivity(), R.string.error_sign_out_network_unavailable);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        });

        // init profile
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

        // init message
        RelativeLayout messageLayout = (RelativeLayout) getActivity().findViewById(R.id.messageLayout);
        messageLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                ReimApplication.setHasMessages(false);
				startActivity(new Intent(getActivity(), MessageListActivity.class));
			}
		});

        tipImageView = (ImageView) view.findViewById(R.id.tipImageView);

        // init invite
        RelativeLayout inviteLayout = (RelativeLayout) getActivity().findViewById(R.id.inviteLayout);
        inviteLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), SendInviteActivity.class));
			}
		});

        // init manager
        managerTextView = (TextView) getActivity().findViewById(R.id.managerTextView);

        RelativeLayout defaultManagerLayout = (RelativeLayout) getActivity().findViewById(R.id.defaultManagerLayout);
        defaultManagerLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (currentUser.getGroupID() <= 0)
                {
                    ViewUtils.showToast(getActivity(), R.string.error_no_group);
                }
                else
                {
                    startActivity(new Intent(getActivity(), ManagerActivity.class));
                }
            }
        });

        // init invoice
        RelativeLayout invoiceLayout = (RelativeLayout) getActivity().findViewById(R.id.invoiceLayout);
        invoiceLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), InvoiceTitleActivity.class));
			}
		});

        // init category
        RelativeLayout categoryLayout = (RelativeLayout) getActivity().findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CATEGORT_SETTING");
                startActivity(new Intent(getActivity(), CategoryActivity.class));
            }
        });

        // init tag
        RelativeLayout tagLayout = (RelativeLayout) getActivity().findViewById(R.id.tagLayout);
        tagLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_TAG_SETTING");
                startActivity(new Intent(getActivity(), TagActivity.class));
            }
        });

        // init feedback
        RelativeLayout feedbackLayout = (RelativeLayout) getActivity().findViewById(R.id.feedbackLayout);
        feedbackLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_OPINION");
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
            }
        });

        // init about
        RelativeLayout aboutLayout = (RelativeLayout) getActivity().findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_ABOUT");
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
        {
            categoryLayout.setVisibility(View.GONE);
            tagLayout.setVisibility(View.GONE);
        }
        else
        {
            categoryLayout.setVisibility(View.VISIBLE);
            tagLayout.setVisibility(View.VISIBLE);
        }
        
//        RelativeLayout customServiceLayout = (RelativeLayout) getActivity().findViewById(R.id.customServiceLayout);
//        customServiceLayout.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				showFeedbackDialog();
//			}
//		});
        
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
        Group currentGroup = appPreference.getCurrentGroup();
		
		if (currentUser != null)
		{
			if (!currentUser.getAvatarPath().isEmpty())
			{
				Bitmap bitmap = BitmapFactory.decodeFile(currentUser.getAvatarPath());
				if (bitmap != null)
				{
					avatarImageView.setImageBitmap(bitmap);						
				}
			}
			
			nicknameTextView.setText(currentUser.getNickname());
	        
			if (currentUser.hasUndownloadedAvatar() && PhoneUtils.isNetworkConnected())
			{
		        sendDownloadAvatarRequest();
			}
		}
		else
		{
			avatarImageView.setImageResource(R.drawable.default_avatar);
			nicknameTextView.setText(R.string.not_available);
		}

        String company = currentGroup != null? currentGroup.getName() : getString(R.string.no_company);
        companyTextView.setText(company);

        User manager = dbManager.getUser(currentUser.getDefaultManagerID());
        if (manager != null)
        {
            managerTextView.setText(manager.getNickname());
        }

        showTip();
	}

    public  void showTip()
    {
        int visibility = ReimApplication.hasMessages() ? View.VISIBLE : View.GONE;
        tipImageView.setVisibility(visibility);
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
					avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
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
								ViewUtils.showToast(getActivity(), R.string.failed_to_save_avatar);
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
							ViewUtils.showToast(getActivity(), R.string.failed_to_download_avatar);
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

                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(getActivity(), SignInActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                }
                else
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(getActivity(), R.string.failed_to_sign_out);
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
    	weiXinShareContent.setShareContent(getString(R.string.share_wechat_content));
    	weiXinShareContent.setTitle(getString(R.string.share_wechat));
    	weiXinShareContent.setTargetUrl(URLDef.SHARE_TARGET);    	
    	mController.setShareMedia(weiXinShareContent);

    	CircleShareContent circleShareContent = new CircleShareContent();
    	circleShareContent.setShareContent(getString(R.string.share_moment_content));
    	circleShareContent.setTitle(getString(R.string.share_moment));
    	circleShareContent.setTargetUrl(URLDef.SHARE_TARGET);    
    	mController.setShareMedia(circleShareContent);

    	SinaShareContent sinaShareContent = new SinaShareContent();
    	sinaShareContent.setShareContent(getString(R.string.share_weibo_content));
    	sinaShareContent.setTitle(getString(R.string.share_weibo));
    	sinaShareContent.setTargetUrl(URLDef.SHARE_TARGET);    
    	mController.setShareMedia(sinaShareContent);

    	QQShareContent qqShareContent = new QQShareContent();
    	qqShareContent.setShareContent(getString(R.string.share_qq_content));
    	qqShareContent.setTitle(getString(R.string.share_qq));
    	qqShareContent.setTargetUrl(URLDef.SHARE_TARGET);    
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

    @SuppressWarnings("unused")
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