package com.rushucloud.reim;

import java.io.FileNotFoundException;
import java.io.IOException;

import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.Request.User.InviteRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;
import netUtils.Response.User.InviteResponse;

import com.rushucloud.reim.me.FeedbackActivity;
import com.rushucloud.reim.me.InviteActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

import classes.AppPreference;
import classes.User;
import classes.Utils;
import classes.Adapter.MeListViewAdapater;
import database.DBManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v4.app.Fragment;

public class MeFragment extends Fragment
{
	private static final int PICK_IMAGE = 0;
	private static final int TAKE_PHOTO = 1;
	private static final int CROP_IMAGE = 2;
	
	private MeListViewAdapater adapter;
	private ListView meListView;
	
	private User currentUser;
	private Uri originalImageUri;
	private String avatarPath;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        currentUser = DBManager.getDBManager().getUser(AppPreference.getAppPreference().getCurrentUserID());
	    return inflater.inflate(R.layout.fragment_me, container, false);  
	}

	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("MeFragment");
        viewInitialise();
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MeFragment");
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
					originalImageUri = null;
					cropImage(data.getData());
				}
				else if (requestCode == TAKE_PHOTO)
				{
					originalImageUri = data.getData();
					cropImage(data.getData());					
				}
				else
				{
					Uri newImageUri = Uri.parse(data.getAction());
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), newImageUri);
					avatarPath = Utils.saveBitmapToFile(bitmap, HttpConstant.IMAGE_TYPE_AVATAR);
					
					if (!avatarPath.equals(""))
					{
						sendUploadAvatarRequest();
					}
					else
					{
						Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();
					}					
					
					if (originalImageUri != null)
					{
						getActivity().getContentResolver().delete(originalImageUri, null, null);							
					}
					getActivity().getContentResolver().delete(newImageUri, null, null);	
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
		
	private void viewInitialise()
	{
        adapter = new MeListViewAdapater(this); 
        meListView = (ListView)getActivity().findViewById(R.id.meListView);
        meListView.setAdapter(adapter);
        meListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 0:
						MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
						startActivity(new Intent(getActivity(), ProfileActivity.class));
						break;
					case 2:
						startActivity(new Intent(getActivity(), InviteActivity.class));
						break;
					case 3:
						MobclickAgent.onEvent(getActivity(), "UMENG_MINE_INVITE");
						showInviteDialog();
						break;
					case 4:
					{
						MobclickAgent.onEvent(getActivity(), "UMENG_MINE_RECOMMEND");
						final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
						mController.setShareContent("报销姐");
						mController.openShare(getActivity(), false);
						break;
					}
					case 5:
						MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_FEEDBACK");
						startActivity(new Intent(getActivity(), FeedbackActivity.class));
						break;
					default:
						break;
				}
			}
		});
        
        if (currentUser.getAvatarPath().equals("") && currentUser.getImageID() != -1)
		{
            sendDownloadAvatarRequest();			
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
					DBManager dbManager = DBManager.getDBManager();
					currentUser.setImageID(response.getImageID());
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(Utils.getCurrentTime());
					currentUser.setServerUpdatedDate(Utils.getCurrentTime());
					dbManager.updateUser(currentUser);
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							meListView.setAdapter(adapter);
							adapter.notifyDataSetChanged();
							Toast.makeText(getActivity(), "头像上传成功", Toast.LENGTH_SHORT).show();
						}
					});	
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getActivity(), "头像上传失败", Toast.LENGTH_SHORT).show();
						}
					});				
				}
			}
		});
    }

    private void sendDownloadAvatarRequest()
    {
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(currentUser.getImageID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					currentUser.setAvatarPath(avatarPath);
					currentUser.setLocalUpdatedDate(Utils.getCurrentTime());
					currentUser.setServerUpdatedDate(currentUser.getLocalUpdatedDate());
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
								Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(getActivity(), "头像下载失败", Toast.LENGTH_SHORT).show();
						}
					});						
				}
			}
		});
    }

    private void showInviteDialog()
    {
		View view = View.inflate(getActivity(), R.layout.profile_invite_dialog, null);
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
											Toast.makeText(getActivity(), "手机号或邮箱不能为空", Toast.LENGTH_SHORT).show();
										}
										else if (!Utils.isEmailOrPhone(username))
										{
											Toast.makeText(getActivity(), "手机号或邮箱格式不正确", Toast.LENGTH_SHORT).show();
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
    	InviteRequest request = new InviteRequest(username);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final InviteResponse response = new InviteResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getActivity(), "邀请已发送", Toast.LENGTH_SHORT).show();
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getActivity(), "邀请发送失败，" + response.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
    }
}
