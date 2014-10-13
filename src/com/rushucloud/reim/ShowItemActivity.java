package com.rushucloud.reim;

import classes.Item;
import classes.Tag;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.R;

import database.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowItemActivity extends Activity
{
	private Item item;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_show_item);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void dataInitialise()
	{
		DBManager dbManager = DBManager.getDBManager();
		Intent intent = this.getIntent();
		int itemLocalID = intent.getIntExtra("item", -1);
		dbManager.getItemByLocalID(itemLocalID);
		if (item == null)
		{
			item = new Item();
		}
	}
	
	private void viewInitialise()
	{		
		CheckBox proveAheadCheckBox = (CheckBox)findViewById(R.id.proveAheadCheckBox);
		proveAheadCheckBox.setChecked(item.isProveAhead());
		
		CheckBox needReimCheckBox = (CheckBox)findViewById(R.id.needReimCheckBox);
		needReimCheckBox.setChecked(item.needReimbursed());
		
		TextView amountTextView = (TextView)findViewById(R.id.amountTextView);
		amountTextView.setText(Double.toString(item.getAmount()));
		
		TextView vendorTextView = (TextView)findViewById(R.id.vendorTextView);
		vendorTextView.setText(item.getMerchant());

		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		TextView categoryTextView = (TextView)findViewById(R.id.categoryTextView);
		categoryTextView.setText(categoryName);
		
		TextView tagTextView = (TextView)findViewById(R.id.tagTextView);
		tagTextView.setText(Tag.tagListToString(item.getTags()));
		
		TextView timeTextView = (TextView)findViewById(R.id.timeTextView);
		timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));
		
		TextView memberTextView = (TextView)findViewById(R.id.memberTextView);
		memberTextView.setText(User.userListToString(item.getRelevantUsers()));
		
		TextView noteTextView = (TextView)findViewById(R.id.noteTextView);
		noteTextView.setText(item.getNote());
		
		ImageView invoiceImageView = (ImageView)findViewById(R.id.invoiceImageView);
		invoiceImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!item.getInvoicePath().equals(""))
				{
					Intent intent = new Intent(ShowItemActivity.this, ImageActivity.class);
					intent.putExtra("imagePath", item.getInvoicePath());
					startActivity(intent);
				}
			}
		});
		
		if (item.getImage() == null)
		{
			invoiceImageView.setImageResource(R.drawable.default_invoice);
		}
		else
		{
			invoiceImageView.setImageBitmap(item.getImage());			
		}
	}
	
	private void buttonInitialise()
	{		
		Button backButton = (Button)findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
}