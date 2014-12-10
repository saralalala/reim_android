package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Utils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReportItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	
	private List<Item> itemList;
	
	private int screenWidth;
	private int interval;
	private int padding;
	private int sideLength;
	private int iconCount;

	public ReportItemListViewAdapter(Context context, List<Item> items)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.itemList = new ArrayList<Item>(items);
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		screenWidth = metrics.widthPixels;
		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		interval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
		sideLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_report_item_edit, parent, false);
		}
		
		TextView amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
		TextView vendorTextView = (TextView) convertView.findViewById(R.id.vendorTextView);
		LinearLayout iconLayout = (LinearLayout) convertView.findViewById(R.id.iconLayout);
		ImageView categoryImageView = (ImageView) convertView.findViewById(R.id.categoryImageView);
		ImageView warningImageView = (ImageView) convertView.findViewById(R.id.warningImageView);
		
		Item item = this.getItem(position);

		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountTextView.setText(Utils.formatDouble(item.getAmount()));

		String vendor = item.getMerchant().equals("") ? context.getString(R.string.notAvailable) : item.getMerchant();
		vendorTextView.setText(vendor);
		
		// category 和 tag 一共iconCount个
		Category category = item.getCategory();
		if (category == null)
		{
			warningImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				categoryImageView.setImageBitmap(bitmap);				
			}					
		}
		
		iconLayout.removeAllViews();
		
		amountTextView.measure(0,0);

		// category 和 tag 一共iconCount个
		iconCount = (screenWidth - amountTextView.getMeasuredWidth() - padding * 3 + interval) / (sideLength + interval);
		iconCount = 1;
		for (int i = 0; i < iconCount; i++)
		{
			ImageView iconImageView = new ImageView(context);
			iconImageView.setImageResource(R.drawable.food);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
			params.rightMargin = interval;
			iconLayout.addView(iconImageView, params);
		}

		iconLayout.addView(categoryImageView);
		
		return convertView;
	}
	
	public int getCount()
	{
		return itemList.size();
	}

	public Item getItem(int position)
	{
		return itemList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void set(List<Item> items)
	{
		itemList.clear();
		itemList.addAll(items);
	}
}