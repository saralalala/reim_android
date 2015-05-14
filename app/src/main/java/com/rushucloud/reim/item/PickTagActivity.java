package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.TagListViewAdapter;
import classes.model.Tag;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ViewUtils;

public class PickTagActivity extends Activity
{
	private TagListViewAdapter tagAdapter;
	
	private List<Tag> tagList = new ArrayList<>();
	private boolean[] check;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reim_tag);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PickTagActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickTagActivity");
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
	
	@SuppressWarnings("unchecked")
	private void initData()
	{		
		int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();

		tagList = DBManager.getDBManager().getGroupTags(currentGroupID);

		List<Tag> chosenTags = (List<Tag>) getIntent().getSerializableExtra("tags");
		check = Tag.getTagsCheck(tagList, chosenTags);
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
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{				
				List<Tag> tags = new ArrayList<>();
				for (int i = 0; i < tagList.size(); i++)
				{
					if (check[i])
					{
						tags.add(tagList.get(i));
					}
				}
				
				Intent intent = new Intent();
				intent.putExtra("tags", (Serializable) tags);
                ViewUtils.goBackWithResult(PickTagActivity.this, intent);
			}
		});

    	ListView tagListView = (ListView) findViewById(R.id.tagListView);
		TextView noTagsTextView = (TextView) findViewById(R.id.noTagsTextView);
		
		if (tagList.isEmpty())
		{
			noTagsTextView.setVisibility(View.VISIBLE);
			tagListView.setVisibility(View.INVISIBLE);
		}
		else
		{
			noTagsTextView.setVisibility(View.INVISIBLE);
			tagListView.setVisibility(View.VISIBLE);
			
			tagAdapter = new TagListViewAdapter(this, tagList, check);
			
	    	tagListView.setAdapter(tagAdapter);
	    	tagListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					check[position] = !check[position];
					tagAdapter.setCheck(check);
					tagAdapter.notifyDataSetChanged();
				}
			});			
		}
	}

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}
