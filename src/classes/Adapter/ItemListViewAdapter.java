package classes.Adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.Item;
import classes.Tag;
import classes.User;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private ItemFilter itemFilter;
	private List<Item> itemList;
	private List<Item> originalList;
	private final Object mLock = new Object();

	public ItemListViewAdapter(Context context, List<Item> items)
	{
		itemList = new ArrayList<Item>(items);
		layoutInflater = LayoutInflater.from(context);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_item_item, parent, false);
		}
		
		ImageView imageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView vendorTextView = (TextView)convertView.findViewById(R.id.vendorTextView);
		TextView categoryTextView = (TextView)convertView.findViewById(R.id.categoryTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Item item = this.getItem(position);

		if (item.getImageID() != -1 || !item.getInvoicePath().equals(""))
		{
			imageView.setImageResource(R.drawable.default_invoice);
		}
		else
		{
			imageView.setImageResource(R.drawable.default_avatar);	
		}
		
		String amount = "￥" + Double.toString(item.getAmount());
		amountTextView.setText(amount);

		String vendor = item.getMerchant().equals("") ? "N/A" : item.getMerchant();
		vendorTextView.setText(vendor);
		
		String reportTitle = item.getBelongReport() == null ? "N/A" : item.getBelongReport().getTitle();
		reportTextView.setText(reportTitle);
		
		String categoryName = item.getCategory() == null ? "N/A" : item.getCategory().getName();
		categoryTextView.setText(categoryName);
		
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
		itemList = new ArrayList<Item>(items);
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
				// TODO 如何优雅的筛选item
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
