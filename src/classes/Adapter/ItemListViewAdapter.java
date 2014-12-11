package classes.Adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Tag;
import classes.User;
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
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private ItemFilter itemFilter;
	private List<Item> itemList;
	private List<Item> originalList;
	private int interval;
	private int sideLength;
	private int iconCount;
	private final Object mLock = new Object();

	public ItemListViewAdapter(Context context, List<Item> items)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.itemList = new ArrayList<Item>(items);
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		this.interval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
		this.sideLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);	
		this.iconCount = (metrics.widthPixels - padding * 2 + interval) / (sideLength + interval);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
		}
		
		ImageView photoImageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		ImageView statusImageView = (ImageView)convertView.findViewById(R.id.statusImageView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView vendorTextView = (TextView)convertView.findViewById(R.id.vendorTextView);
		LinearLayout iconLayout = (LinearLayout)convertView.findViewById(R.id.iconLayout);
		ImageView categoryImageView = (ImageView)convertView.findViewById(R.id.categoryImageView);
		
		Item item = this.getItem(position);

		if (item.hasInvoice())
		{
			photoImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			photoImageView.setVisibility(View.GONE);
		}
		
		if (item.getStatus() == Item.STATUS_PROVE_AHEAD_APPROVED)
		{
			statusImageView.setImageResource(R.drawable.item_approved_list);
			statusImageView.setVisibility(View.VISIBLE);
		}
		else if (item.isProveAhead())
		{
			statusImageView.setImageResource(R.drawable.item_prove_ahead_list);
			statusImageView.setVisibility(View.VISIBLE);
		}
		else
		{
			statusImageView.setVisibility(View.INVISIBLE);					
		}

		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		amountTextView.setText(Utils.formatDouble(item.getAmount()));

		String vendor = item.getMerchant().equals("") ? context.getString(R.string.not_available) : item.getMerchant();
		vendorTextView.setText(vendor);
		
		String reportTitle = item.getBelongReport() == null ? context.getString(R.string.not_available) : item.getBelongReport().getTitle();
		reportTextView.setText(reportTitle);
		
		// category 和 tag 一共iconCount个
		Category category = item.getCategory();
		if (category != null)
		{
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				categoryImageView.setImageBitmap(bitmap);				
			}
		}

		iconLayout.removeAllViews();
		
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
	
	public void clear()
	{
		itemList.clear();
	}
	
	public void set(List<Item> items)
	{
		itemList.clear();
		itemList.addAll(items);
	}
	
	public Filter getFilter()
	{
		if (itemFilter == null)
		{
			itemFilter = new ItemFilter();
		}
		return itemFilter;
	}
	
	private class ItemFilter extends Filter
	{
		protected FilterResults performFiltering(CharSequence constraint)
		{
			FilterResults results = new FilterResults();
			if (originalList == null)
			{
				synchronized (mLock)
				{
					originalList = new ArrayList<Item>(itemList);
				}
			}
			if (constraint == null || constraint.length() == 0)
			{
				synchronized (mLock)
				{
					ArrayList<Item> list = new ArrayList<Item>(originalList);
					results.values = list;
					results.count = list.size();
				}
			}
			else
			{
				Locale locale = Locale.getDefault();
				String constraintString = constraint.toString().toLowerCase(locale);
				ArrayList<Item> newValues = new ArrayList<Item>();
				for (Item item : originalList)
				{
					if (item.getNote().contains(constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (item.getMerchant().contains(constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (Double.toString(item.getAmount()).contains(constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (item.getConsumer() != null && item.getConsumer().getNickname().contains(constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (item.getCategory() != null && item.getCategory().getName().contains(constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (userCanBeFiltered(item, constraintString))
					{
						newValues.add(item);
						continue;
					}
					if (tagCanBeFiltered(item, constraintString))
					{
						newValues.add(item);
						continue;
					}
				}
				
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			itemList = (ArrayList<Item>)results.values;
			if (results.count > 0)
			{
				notifyDataSetChanged();
			}
			else
			{
				notifyDataSetInvalidated(); 				
			}				
		}		
	}
	
	private boolean userCanBeFiltered(Item item, String constraint)
	{
		if (item.getRelevantUsers() == null)
		{
			return false;
		}
		
		for (User user : item.getRelevantUsers())
		{
			if (user.getNickname().contains(constraint))
			{
				return true;
			}
			if (user.getEmail().contains(constraint))
			{
				return true;
			}
			if (user.getPhone().contains(constraint))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean tagCanBeFiltered(Item item, String constraint)
	{
		if (item.getTags() == null)
		{
			return false;
		}
		
		for (Tag tag : item.getTags())
		{
			if (tag.getName().contains(constraint))
			{
				return true;
			}
		}
		return false;
	}
}
