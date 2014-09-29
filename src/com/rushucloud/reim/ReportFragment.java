package com.rushucloud.reim;

import classes.Utils;
import netUtils.HttpConstant;
import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.UploadImageRequest;
import netUtils.Response.UploadImageResponse;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v4.app.Fragment;

public class ReportFragment extends Fragment {

	private Uri uri = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.fragment_report, container, false);  
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState); 
		
        uri=Uri.parse("content://media/external/images/media/267766");

		final ImageView imageView = (ImageView)getActivity().findViewById(R.id.chooseImageView);
		imageView.setImageURI(uri);	        
        
        Button button=(Button) getActivity().findViewById(R.id.chooseImageButton);
        button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent, 0);
			}
		}); 
		
        Button button2=(Button) getActivity().findViewById(R.id.uploadButton);
        button2.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{				
				String path = Utils.getPathFromUri(getActivity(), uri);
				UploadImageRequest request = new UploadImageRequest(path, HttpConstant.IMAGE_TYPE_AVATAR);
//				String path = "http://api.1in1.cn/static/a1/d0/42/0/2014/09/038694bb07b22ade25c039cb96ba8694.compressedBitmapFile";
//				DownloadImageRequest request = new DownloadImageRequest(path);
				request.sendRequest(new HttpConnectionCallback()
				{
					public void execute(Object object)
					{
						try
						{
							final UploadImageResponse response2 = new UploadImageResponse(object);
//							final DownloadImageResponse response2 = new DownloadImageResponse(object);
//							getActivity().runOnUiThread(new Runnable()
//							{
//								
//								@Override
//								public void run()
//								{
//									imageView.setImageBitmap(response2.getBitmap());									
//								}
//							});
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
			}
		});
    }  
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(data!=null)
		{
			uri = data.getData();
			ImageView imageView = (ImageView)getActivity().findViewById(R.id.chooseImageView);
			imageView.setImageURI(uri);			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
