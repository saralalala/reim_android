package com.rushucloud.reim;

import com.rushucloud.reim.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageActivity extends Activity
{
	private ImageView imageView;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_image);
		viewInitialise();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void viewInitialise()
	{
		try
		{
			String imagePath = getIntent().getStringExtra("imagePath");
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			imageView = (ImageView)findViewById(R.id.imageView);
			imageView.setImageBitmap(bitmap);
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
