package com.rushucloud.reim;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import classes.adapter.GalleryAdapter;
import classes.utils.PhoneUtils;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class GalleryActivity extends Activity
{
	private TextView noImageTextView;
	private GalleryAdapter adapter;
	private ImageLoader imageLoader;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_gallery);

		initImageLoader();
		initView();
		loadImages();
	}

	private void initImageLoader()
	{
		try
		{
			String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp_tmp";
			new File(CACHE_DIR).mkdirs();

			File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(), CACHE_DIR);

			DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565).build();
			ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getBaseContext()).defaultDisplayImageOptions(defaultOptions)
					.discCache(new UnlimitedDiscCache(cacheDir)).memoryCache(new WeakMemoryCache());

			ImageLoaderConfiguration config = builder.build();
			imageLoader = ImageLoader.getInstance();
			imageLoader.init(config);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void initView()
	{
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		noImageTextView = (TextView) findViewById(R.id.noImageTextView);
		
		int maxCount = getIntent().getIntExtra("maxCount", -1);
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader, maxCount);
		
		GridView galleryGridView = (GridView) findViewById(R.id.galleryGridView);
		galleryGridView.setAdapter(adapter);
		galleryGridView.setFastScrollEnabled(true);
		galleryGridView.setOnScrollListener(new PauseOnScrollListener(imageLoader, true, true));

		Button confirmButton = (Button) findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				ArrayList<String> selectedList = adapter.getSelectedList();
				Intent intent = new Intent();
				intent.putExtra("paths", selectedList.toArray(new String[selectedList.size()]));
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		// resize button
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		int marginPixels = PhoneUtils.dpToPixel(getResources(), 16);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_border_dark);
		double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		ViewGroup.LayoutParams params = confirmButton.getLayoutParams();
		params.width = metrics.widthPixels - marginPixels * 2;
		params.height = (int)(params.width * ratio);
		
		confirmButton.setLayoutParams(params);
	}

	private void loadImages()
	{
		final Handler handler = new Handler();
		new Thread()
		{
			public void run()
			{
				Looper.prepare();
				handler.post(new Runnable()
				{
					public void run()
					{
						adapter.setImageList(getGalleryPaths());
						adapter.notifyDataSetChanged();
						if (adapter.isEmpty())
						{
							noImageTextView.setVisibility(View.VISIBLE);
						}
						else
						{
							noImageTextView.setVisibility(View.GONE);
						}
					}
				});
				Looper.loop();
			};
		}.start();
	}

	private ArrayList<String> getGalleryPaths()
	{
		ArrayList<String> pathList = new ArrayList<String>();

		try
		{
			// only looking for jpg and png files
			StringBuilder selection = new StringBuilder();
			selection.append(Media.MIME_TYPE).append("=?");
			selection.append(" or ");
			selection.append(Media.MIME_TYPE).append("=?");

			Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), 
														new String[] { "image/jpeg", "image/png" }, Media.DATE_TAKEN);

			if (cursor != null && cursor.getCount() > 0)
			{
				while (cursor.moveToNext())
				{
					pathList.add(cursor.getString(cursor.getColumnIndex(Media.DATA)));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// show the latest photo on top
		Collections.reverse(pathList);
		return pathList;
	}
}
