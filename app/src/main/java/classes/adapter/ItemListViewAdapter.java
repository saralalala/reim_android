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

import classes.base.Category;
import classes.base.Item;
import classes.base.Tag;
import classes.base.User;
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
		this.itemList = new ArrayList<>(items);
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
			TextView typeTextView = (TextView) view.findViewById(R.id.typeTextView);
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

            int visibility = item.getType() == Item.TYPE_REIM? View.GONE : View.VISIBLE;
            typeTextView.setVisibility(visibility);

			if (item.getType() == Item.TYPE_BUDGET && item.isAaApproved())
			{
                typeTextView.setText(R.string.status_budget);
                typeTextView.setBackgroundResource(R.drawable.status_item_approved);
			}
			else if (item.getType() == Item.TYPE_BUDGET)
			{
                typeTextView.setText(R.string.status_budget);
                typeTextView.setBackgroundResource(R.drawable.status_item_approve_ahead);
			}
			else if (item.getType() == Item.TYPE_BORROWING && item.isAaApproved())
            {
                typeTextView.setText(R.string.status_borrowing);
                typeTextView.setBackgroundResource(R.drawable.status_item_approved);
            }
            else if (item.getType() == Item.TYPE_BORROWING)
            {
                typeTextView.setText(R.string.status_borrowing);
                typeTextView.setBackgroundResource(R.drawable.status_item_approve_ahead);
            }

			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getVendor().isEmpty()? context.getString(R.string.vendor_not_available) : item.getVendor();
			vendorTextView.setText(vendor);
			
			String reportTitle = item.getBelongReport() == null? context.getString(R.string.report_not_available) : item.getBelongReport().getTitle();
			reportTextView.setText(reportTitle);
			
			Category category = item.getCategory();
			if (category != null)
			{
                ViewUtils.setImageViewBitmap(category, categoryImageView);
			}
            else
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
					originalList = new ArrayList<>(itemList);
				}
			}
			if (constraint == null || constraint.length() == 0)
			{
				synchronized (mLock)
				{
					ArrayList<Item> list = new ArrayList<>(originalList);
					results.values = list;
					results.count = list.size();
				}
			}
			else
			{
				Locale locale = Locale.getDefault();
				String constraintString = constraint.toString().toLowerCase(locale);
				ArrayList<Item> newValues = new ArrayList<>();
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