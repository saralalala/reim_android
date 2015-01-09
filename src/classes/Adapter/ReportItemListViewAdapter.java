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
		if (position == 0)
		{
			return layoutInflater.inflate(R.layout.list_report_new_item, parent, false);
		}
		else
		{
			View view = layoutInflater.inflate(R.layout.list_report_item_edit, parent, false);
			
			if (check != null)
			{
				int color = check[position - 1] ? R.color.list_item_selected : R.color.list_item_unselected;
				view.setBackgroundResource(color);
			}
			
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
			ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);
			
			Item item = itemList.get(position - 1);

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
			
			return view;
		}
	}
	
	public int getCount()
	{
		return itemList.size() + 1;
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