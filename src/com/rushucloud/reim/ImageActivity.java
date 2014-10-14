package com.rushucloud.reim;

import com.rushucloud.reim.R;

import android.app.ActionBar;
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
			ActionBar actionBar = getActionBar();
			actionBar.hide();
			
			String imagePath = getIntent().getStringExtra("imagePath");
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			imageView = (ImageView)findViewById(R.id.imageView);
			imageView.setImageBitmap(bitmap);

			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			LayoutParams params = imageView.getLayoutParams();
			params.height = dm.widthPixels;
			params.width = dm.widthPixels;
			
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
