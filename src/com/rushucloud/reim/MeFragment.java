package com.rushucloud.reim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.UploadImageRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.UploadImageResponse;

import com.rushucloud.reim.me.FeedbackActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

import classes.AppPreference;
import classes.User;
import classes.Utils;
import classes.Adapter.MeListViewAdapater;
import database.DBManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
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
	private String avatarPath = "";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
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
					cropImage(data.getData());
				}
				else
				{
					Uri uri = Uri.parse(data.getAction());
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
					saveBitmapToFile(bitmap);
					getActivity().getContentResolver().delete(uri, null, null);
					sendUploadAvatarRequest();
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position == 0)
				{
					startActivity(new Intent(getActivity(), ProfileActivity.class));
				}
				
				if (position == 4)
				{
					final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
					mController.setShareContent("报销姐");
					mController.openShare(getActivity(), false);
				}
				
				if (position == 5)
				{
					startActivity(new Intent(getActivity(), FeedbackActivity.class));
				}
			}
		});
        
        sendDownloadAvatarRequest();
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
	
    private Boolean saveBitmapToFile(Bitmap bitmap)
    {
    	try
		{    		
    		AppPreference appPreference = AppPreference.getAppPreference();
    		Matrix matrix = new Matrix();
    		matrix.postScale((float)0.5, (float)0.5);
    		
    		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    		
    		File compressedBitmapFile = new File(appPreference.getProfileImageDirectory(), Utils.getImageName());
    		compressedBitmapFile.createNewFile();
    		
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		bitmap.compress(CompressFormat.JPEG, 90, outputStream);
    		byte[] bitmapData = outputStream.toByteArray();
    		
    		FileOutputStream fileOutputStream = new FileOutputStream(compressedBitmapFile);
    		fileOutputStream.write(bitmapData);
    		fileOutputStream.flush();
    		fileOutputStream.close();	
    		
    		avatarPath = appPreference.getProfileImageDirectory() + "/" + Utils.getImageName();
    		return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
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
					User user = dbManager.getUser(AppPreference.getAppPreference().getCurrentUserID());
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(Utils.getCurrentTime());
					if (dbManager.updateUser(user))
					{
						getActivity().runOnUiThread(new Runnable()
						{
							public void run()
							{
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
								adapter.notifyDataSetChanged();
								Toast.makeText(getActivity(), "头像上传失败", Toast.LENGTH_SHORT).show();
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
    	final User user = dbManager.getUser(AppPreference.getAppPreference().getCurrentUserID());
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarPath());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					saveBitmapToFile(response.getBitmap());
					user.setAvatarPath(avatarPath);
					dbManager.updateUser(user);
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							meListView.setAdapter(adapter);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		});
    }
}
