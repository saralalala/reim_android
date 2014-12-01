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
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import classes.Adapter.MeListViewAdapter;
import classes.Adapter.OperationListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v4.app.Fragment;

public class MeFragment extends Fragment
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private boolean hasInit = false;

	private View view;
	private MeListViewAdapter adapter;
	private ListView meListView;
	
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
	        initView();
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
	        initView();
		}
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
    	if (!getUserVisibleHint())
		{
			return false;
		}
    	
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
		
	private void initView()
	{
        View divider = getActivity().getLayoutInflater().inflate(R.layout.list_divider, null);
        
        adapter = new MeListViewAdapter(this); 
        meListView = (ListView)getActivity().findViewById(R.id.meListView);
        meListView.addHeaderView(divider);
        meListView.setAdapter(adapter);
        meListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
				startActivity(new Intent(getActivity(), ProfileActivity.class));
			}
		});
        
        if (Utils.isNetworkConnected())
		{
            if (currentUser.hasUndownloadedAvatar())
    		{
                sendDownloadAvatarRequest();			
    		}
		}

        int[] operationList = { R.string.defaultManager, R.string.invite, R.string.myInvites, R.string.getInvoice };
        boolean[] checkList = { true, false, true, false };
        
        OperationListViewAdapter operationAdapter = new OperationListViewAdapter(getActivity(), operationList, checkList);
        ListView operationListView = (ListView)getActivity().findViewById(R.id.operationListView);
        operationListView.addHeaderView(divider);
        operationListView.setAdapter(operationAdapter);
        operationListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position - 1)
				{
					case 0:
					{
						if (currentUser.getGroupID() == -1)
						{
							Utils.showToast(getActivity(), "你还没加入任何组");			
						}
						else
						{
							startActivity(new Intent(getActivity(), ManagerActivity.class));
						}
						break;
					}
					case 1:
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
						break;
					}
					case 2:
						startActivity(new Intent(getActivity(), InviteActivity.class));
						break;
					case 3:
						startActivity(new Intent(getActivity(), InvoiceTitleActivity.class));
						break;
//					case 4:
//						MobclickAgent.onEvent(getActivity(), "UMENG_MINE_RECOMMEND");
//						showShareDialog();
//						break;
					default:
						break;
				}
			}
		});

        int[] otherList = { R.string.settings, R.string.customService };
        boolean[] otherCheckList = { true, false };
        
        OperationListViewAdapter otherAdapter = new OperationListViewAdapter(getActivity(), otherList, otherCheckList);
        ListView otherListView = (ListView)getActivity().findViewById(R.id.otherListView);
        otherListView.addHeaderView(divider);
        otherListView.setAdapter(otherAdapter);
        otherListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position - 1)
				{
					case 0:
						startActivity(new Intent(getActivity(), SettingsActivity.class));
						break;
					case 1:
						showFeedbackDialog();
						break;
					default:
						break;
				}
			}
		});

        mController = UMServiceFactory.getUMSocialService("com.umeng.share");
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
					currentUser.setImageID(response.getImageID());
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(currentTime);
					currentUser.setServerUpdatedDate(currentTime);
					dbManager.updateUser(currentUser);
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							meListView.setAdapter(adapter);
							adapter.notifyDataSetChanged();
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
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(currentUser.getImageID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
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
								adapter.notifyDataSetChanged();
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