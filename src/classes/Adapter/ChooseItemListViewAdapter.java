package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Item;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseItemListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private List<Item> itemList;
	private boolean[] check;

	public ChooseItemListViewAdapter(Context context, List<Item> items, boolean[] checkList)
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
			convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
		}

		int color = check[position] ? R.color.list_item_selected : R.color.list_item_unselected;
		convertView.setBackgroundResource(color);
		
		ImageView photoImageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView vendorTextView = (TextView)convertView.findViewById(R.id.vendorTextView);
		
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
			statusTextView.setText(context.getString(R.string.item_approved));
			statusTextView.setBackgroundResource(R.drawable.item_approved_list);
			statusTextView.setTextColor(context.getResources().getColor(R.color.item_approved));
			statusTextView.setVisibility(View.VISIBLE);
		}
		else if (item.isProveAhead())
		{
			statusTextView.setText(context.getString(R.string.prove_ahead));
			statusTextView.setBackgroundResource(R.drawable.item_approved_list);
			statusTextView.setTextColor(context.getResources().getColor(R.color.item_prove_ahead));
			statusTextView.setVisibility(View.VISIBLE);			
		}
		else
		{
			statusTextView.setVisibility(View.INVISIBLE);					
		}
		
		String amount = context.getString(R.string.rmb) + Double.toString(item.getAmount());
		amountTextView.setText(amount);

		String vendor = item.getMerchant().equals("") ? context.getString(R.string.notAvailable) : item.getMerchant();
		vendorTextView.setText(vendor);
		
		String reportTitle = item.getBelongReport() == null ? context.getString(R.string.notAvailable) : item.getBelongReport().getTitle();
		reportTextView.setText(reportTitle);
		
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
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
