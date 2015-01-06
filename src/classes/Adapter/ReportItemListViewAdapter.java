package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Utils.Utils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	
	private List<Item> itemList;
	private boolean[] check;

	public ReportItemListViewAdapter(Context context, List<Item> items, boolean[] checkList)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.itemList = new ArrayList<Item>(items);
		this.check = checkList;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_report_item_edit, parent, false);
		}
		
		if (check != null)
		{
			int color = check[position] ? R.color.list_item_selected : R.color.list_item_unselected;
			convertView.setBackgroundResource(color);
		}
		
		TextView amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
		TextView vendorTextView = (TextView) convertView.findViewById(R.id.vendorTextView);
		ImageView categoryImageView = (ImageView) convertView.findViewById(R.id.categoryImageView);
		ImageView warningImageView = (ImageView) convertView.findViewById(R.id.warningImageView);
		
		Item item = this.getItem(position);

		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountTextView.setText(Utils.formatDouble(item.getAmount()));

		String vendor = item.getVendor().equals("") ? context.getString(R.string.not_available) : item.getVendor();
		vendorTextView.setText(vendor);
		
		// category 和 tag 一共iconCount个
		if (item.missingInfo())
		{
			warningImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			Category category = item.getCategory();
			
			categoryImageView.setImageResource(R.drawable.default_icon);
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				categoryImageView.setImageBitmap(bitmap);				
			}					
		}
		
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
	
	public void set(List<Item> items, boolean[] checkList)
	{
		itemList.clear();
		itemList.addAll(items);
		
		check = checkList;
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}