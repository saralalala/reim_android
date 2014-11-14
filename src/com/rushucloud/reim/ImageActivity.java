package com.rushucloud.reim;

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
		setContentView(R.layout.reim_image);
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
		try
		{
			String imagePath = getIntent().getStringExtra("imagePath");
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			imageView = (ImageView)findViewById(R.id.imageView);
			imageView.setImageBitmap(bitmap);

			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			
			double imageRatio = ((double)bitmap.getHeight())/bitmap.getWidth();
			double screenRatio = ((double)dm.heightPixels)/dm.widthPixels;
			
			LayoutParams params = imageView.getLayoutParams();
			if (imageRatio > screenRatio)
			{
				params.height = dm.heightPixels;
				params.width = (int) (dm.heightPixels / imageRatio);
			}
			else
			{
				params.height = (int) (dm.widthPixels * imageRatio);
				params.width = dm.widthPixels;
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
