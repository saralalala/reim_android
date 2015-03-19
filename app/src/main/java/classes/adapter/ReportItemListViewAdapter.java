package classes.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.Item;
import classes.utils.ReimApplication;
import classes.utils.Utils;

public class ReportItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	
	private List<Item> itemList;
	private List<Integer> chosenIDList;

	public ReportItemListViewAdapter(Context context, List<Item> items, List<Integer> chosenList)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.itemList = new ArrayList<Item>(items);
		this.chosenIDList = new ArrayList<Integer>(chosenList);
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
			
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
			ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);
			
			Item item = itemList.get(position - 1);

			int color = chosenIDList.contains(item.getLocalID()) ? R.color.list_item_selected : R.color.list_item_unselected;
			view.setBackgroundResource(color);

			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getVendor().isEmpty() ? context.getString(R.string.vendor_not_available) : item.getVendor();
			vendorTextView.setText(vendor);
			
			if (item.missingInfo())
			{
				warningImageView.setVisibility(View.VISIBLE);
			}
			else
			{
				Category category = item.getCategory();
				
				categoryImageView.setImageResource(R.drawable.default_icon);
				if (category != null && !category.getIconPath().isEmpty())
				{
					Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
					if (bitmap != null)
					{
						categoryImageView.setImageBitmap(bitmap);				
					}
				}
                else if (category == null)
                {
                    categoryImageView.setVisibility(View.INVISIBLE);
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
	
	public void set(List<Item> items, List<Integer> chosenList)
	{
		itemList.clear();
		itemList.addAll(items);

		chosenIDList.clear();
		chosenIDList.addAll(chosenList);
	}
	
	public void setChosenList(List<Integer> chosenList)
	{
		chosenIDList.clear();
		chosenIDList.addAll(chosenList);
	}
}