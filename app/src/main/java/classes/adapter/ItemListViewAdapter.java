package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.Category;
import classes.Item;
import classes.Tag;
import classes.User;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class ItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private ItemFilter itemFilter;
	private List<Item> itemList;
	private List<Item> originalList;
	private final Object mLock = new Object();

	public ItemListViewAdapter(Context context, List<Item> items)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);		
		this.itemList = new ArrayList<Item>(items);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		Item item = this.getItem(position);
		if (!item.getConsumedDateGroup().isEmpty())
		{
			View view = layoutInflater.inflate(R.layout.list_header, parent, false);
			
			String date = item.getConsumedDateGroup();
			TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
			headerTextView.setText(Utils.dateToWeekday(date) + " " + date);
			
			return view;
		}
		else
		{
			View view = layoutInflater.inflate(R.layout.list_item, parent, false);
			
			ImageView photoImageView = (ImageView) view.findViewById(R.id.photoImageView);
			TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);
			TextView proveTextView = (TextView) view.findViewById(R.id.proveTextView);
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView reportTextView = (TextView) view.findViewById(R.id.reportTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);			

			if (item.hasInvoice())
			{
				photoImageView.setVisibility(View.VISIBLE);
			}
			else
			{
				photoImageView.setVisibility(View.GONE);
			}

			statusTextView.setText(item.getStatusString());
			statusTextView.setBackgroundResource(item.getStatusBackground());
			
			if (item.isProveAhead() && item.isPaApproved())
			{
				proveTextView.setVisibility(View.VISIBLE);
				proveTextView.setBackgroundResource(R.drawable.status_item_approved);
			}
			else if (item.isProveAhead())
			{
				proveTextView.setVisibility(View.VISIBLE);
				proveTextView.setBackgroundResource(R.drawable.status_item_prove_ahead);
			}
			else
			{
				proveTextView.setVisibility(View.GONE);
			}

			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getVendor().isEmpty() ? context.getString(R.string.vendor_not_available) : item.getVendor();
			vendorTextView.setText(vendor);
			
			String reportTitle = item.getBelongReport() == null ? context.getString(R.string.report_not_available) : item.getBelongReport().getTitle();
			reportTextView.setText(reportTitle);
			
			Category category = item.getCategory();
			
			categoryImageView.setImageResource(R.drawable.default_icon);
			if (category != null)
			{
                ViewUtils.setImageViewBitmap(category, categoryImageView);
			}
            else if (category == null)
            {
                categoryImageView.setVisibility(View.GONE);
            }
			
			return view;
		}
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
					if (item.getVendor().contains(constraintString))
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