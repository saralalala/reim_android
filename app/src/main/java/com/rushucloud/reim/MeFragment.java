package com.rushucloud.reim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.me.AboutActivity;
import com.rushucloud.reim.me.CategoryActivity;
import com.rushucloud.reim.me.FeedbackActivity;
import com.rushucloud.reim.me.InviteActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ManagerActivity;
import com.rushucloud.reim.me.MessageListActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.TagActivity;
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

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.URLDef;
import netUtils.request.DownloadImageRequest;
import netUtils.response.DownloadImageResponse;

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
    private RelativeLayout categoryLayout;
    private RelativeLayout tagLayout;

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
        // init profile
		nicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);
		companyTextView = (TextView) view.findViewById(R.id.companyTextView);	
		
		avatarImageView = (CircleImageView) view.findViewById(R.id.avatarImageView);
		
        RelativeLayout profileLayout = (RelativeLayout) view.findViewById(R.id.profileLayout);
        profileLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
                ViewUtils.goForward(getActivity(), ProfileActivity.class);
			}
		});

        // init message
        RelativeLayout messageLayout = (RelativeLayout) view.findViewById(R.id.messageLayout);
        messageLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                ReimApplication.setUnreadMessagesCount(0);
                ReimApplication.setHasUnreadMessages(false);
                ViewUtils.goForward(getActivity(), MessageListActivity.class);
			}
		});

        tipImageView = (ImageView) view.findViewById(R.id.tipImageView);

        // init invite
        RelativeLayout inviteLayout = (RelativeLayout) view.findViewById(R.id.inviteLayout);
        inviteLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                ViewUtils.goForward(getActivity(), InviteActivity.class);
			}
		});

        // init manager
        managerTextView = (TextView) view.findViewById(R.id.managerTextView);

        RelativeLayout defaultManagerLayout = (RelativeLayout) view.findViewById(R.id.defaultManagerLayout);
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
                    ViewUtils.goForward(getActivity(), ManagerActivity.class);
                }
            }
        });

        // init invoice
        RelativeLayout invoiceLayout = (RelativeLayout) view.findViewById(R.id.invoiceLayout);
        invoiceLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                ViewUtils.goForward(getActivity(), InvoiceTitleActivity.class);
			}
		});

        // init category
        categoryLayout = (RelativeLayout) view.findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CATEGORT_SETTING");
                ViewUtils.goForward(getActivity(), CategoryActivity.class);
            }
        });

        // init tag
        tagLayout = (RelativeLayout) view.findViewById(R.id.tagLayout);
        tagLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_TAG_SETTING");
                ViewUtils.goForward(getActivity(), TagActivity.class);
            }
        });

        // init about
        RelativeLayout aboutLayout = (RelativeLayout) view.findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_ABOUT");
                ViewUtils.goForward(getActivity(), AboutActivity.class);
            }
        });

        // init feedback
        RelativeLayout feedbackLayout = (RelativeLayout) view.findViewById(R.id.feedbackLayout);
        feedbackLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_OPINION");
                ViewUtils.goForward(getActivity(), FeedbackActivity.class);
            }
        });
        
//        RelativeLayout shareLayout = (RelativeLayout) view.findViewById(R.id.shareLayout);
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

	public void loadProfileView()
	{
		currentUser = appPreference.getCurrentUser();
        Group currentGroup = appPreference.getCurrentGroup();
		
		if (currentUser != null)
		{
            ViewUtils.setImageViewBitmap(currentUser, avatarImageView);
			nicknameTextView.setText(currentUser.getNickname());
	        
			if (currentUser.hasUndownloadedAvatar() && PhoneUtils.isNetworkConnected())
			{
		        sendDownloadAvatarRequest();
			}

            User manager = currentUser.getDefaultManager();
            if (manager != null)
            {
                managerTextView.setText(manager.getNickname());
            }
            else
            {
                managerTextView.setText(R.string.null_string);
            }

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
        else if (!currentUser.getAppliedCompany().isEmpty())
        {
            companyTextView.setText(currentUser.getAppliedCompany() + ViewUtils.getString(R.string.waiting_for_approve));
        }
        else
        {
            companyTextView.setText(R.string.no_company);
        }

        showTip();
	}

    public  void showTip()
    {
        if (tipImageView == null)
        {
            tipImageView = (ImageView) view.findViewById(R.id.tipImageView);
        }
        else
        {
            int visibility = ReimApplication.hasUnreadMessages()? View.VISIBLE : View.GONE;
            tipImageView.setVisibility(visibility);
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
					avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					currentUser.setAvatarLocalPath(avatarPath);
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
				else if (getUserVisibleHint())
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

    @SuppressWarnings("unused")
	private void showShareDialog()
    {
    	SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
    	sinaSsoHandler.addToSocialSDK();
    	
    	UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(), "1103305832", "l8eKHcEiAMCnhV50");
    	qqSsoHandler.addToSocialSDK();

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
}