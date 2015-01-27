package com.rushucloud.reim;

import uk.co.senab.photoview.PhotoView;
import classes.utils.ViewUtils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;

public class ImageActivity extends Activity
{	
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
			ViewUtils.showToast(this, R.string.failed_to_read_image);
			finish();
		}
		
		PhotoView photoView = (PhotoView)findViewById(R.id.photoView);
		photoView.setImageBitmap(bitmap);
	}
}