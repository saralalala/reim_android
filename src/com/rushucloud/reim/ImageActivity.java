package com.rushucloud.reim;

import classes.Utils.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageActivity extends Activity
{
	private ImageView imageView;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ImageActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ImageActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		String imagePath = getIntent().getStringExtra("imagePath");
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		
		if (bitmap == null)
		{
			Utils.showToast(this, "读取图片文件失败");
			finish();
		}
		
		imageView = (ImageView)findViewById(R.id.imageView);
		imageView.setImageBitmap(bitmap);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		double imageRatio = ((double)bitmap.getHeight())/bitmap.getWidth();
		double screenRatio = ((double)metrics.heightPixels)/metrics.widthPixels;
		
		LayoutParams params = imageView.getLayoutParams();
		if (imageRatio > screenRatio)
		{
			params.height = metrics.heightPixels;
			params.width = (int) (metrics.heightPixels / imageRatio);
		}
		else
		{
			params.height = (int) (metrics.widthPixels * imageRatio);
			params.width = metrics.widthPixels;
		}
		
		imageView.setLayoutParams(params);
		imageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
}
