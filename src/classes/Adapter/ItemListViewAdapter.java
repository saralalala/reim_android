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

public class ItemListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Item> itemList;

	public ItemListViewAdapter(Context context, List<Item> items)
	{
		itemList = new ArrayList<Item>(items);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.items_list_item, parent, false);
		}
		
		ImageView imageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView infoTextView = (TextView)convertView.findViewById(R.id.infoTextView);
		TextView categoryTextView = (TextView)convertView.findViewById(R.id.categoryTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Item item = this.getItem(position);
		
		imageView.setImageBitmap(item.getImage());
		reportTextView.setText(item.getBelongReport().getTitle());
		infoTextView.setText(item.getNote());
		categoryTextView.setText(item.getCategory().getName());
		amountTextView.setText(Double.toString(item.getAmount()));
		
		return convertView;
	}
	
	@Override
	public int getCount()
	{
		return itemList.size();
	}

	@Override
	public Item getItem(int position)
	{
		return itemList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}
}
