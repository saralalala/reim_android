package com.rushucloud.reim.common;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import classes.adapter.GalleryAdapter;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class GalleryActivity extends Activity
{
    // Widgets
    private TextView noImageTextView;
    private GalleryAdapter adapter;
    private Button confirmButton;
    private ImageLoader imageLoader;

    // View
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);

        initImageLoader();
        initView();
        loadImages();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("GalleryActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("GalleryActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
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
                goBack();
            }
        });

        noImageTextView = (TextView) findViewById(R.id.noImageTextView);

        final int maxCount = getIntent().getIntExtra("maxCount", -1);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader, maxCount, new Utils.ExtraCallBack()
        {
            public void execute()
            {
                int selectedCount = adapter.getSelectedCount();
                String title = selectedCount == 0 ? getString(R.string.confirm) : getString(R.string.confirm) + " (" + selectedCount + "/" + maxCount + ")";
                confirmButton.setText(title);
            }
        });

        GridView galleryGridView = (GridView) findViewById(R.id.galleryGridView);
        galleryGridView.setAdapter(adapter);
        galleryGridView.setOnScrollListener(new PauseOnScrollListener(imageLoader, true, true));

        confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ArrayList<String> selectedList = adapter.getSelectedList();
                Intent intent = new Intent();
                intent.putExtra("paths", selectedList.toArray(new String[selectedList.size()]));
                ViewUtils.goBackWithResult(GalleryActivity.this, intent);
            }
        });
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
            }
        }.start();
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    private ArrayList<String> getGalleryPaths()
    {
        ArrayList<String> pathList = new ArrayList<>();

        Cursor cursor = null;
        try
        {
            // only looking for jpg and png files
            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, Media.MIME_TYPE + "=?" + " or " + Media.MIME_TYPE + "=?",
                                                new String[]{"image/jpeg", "image/png"}, Media.DATE_TAKEN);

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
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        // show the latest photo on top
        Collections.reverse(pathList);
        return pathList;
    }
}