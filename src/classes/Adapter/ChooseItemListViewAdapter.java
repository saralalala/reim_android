package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Item;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseItemListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Item> itemList;
	private boolean[] check;

	public ChooseItemListViewAdapter(Context context, List<Item> items, boolean[] checkList)
	{
		itemList = new ArrayList<Item>(items);
		check = checkList;
		layoutInflater = LayoutInflater.from(context);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_item_item, parent, false);
		}

		if (check[position])
		{
			convertView.setBackgroundColor(Color.rgb(102, 204, 255));
		}
		else
		{
			convertView.setBackgroundColor(Color.WHITE);			
		}
		
		ImageView imageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView infoTextView = (TextView)convertView.findViewById(R.id.infoTextView);
		TextView categoryTextView = (TextView)convertView.findViewById(R.id.categoryTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Item item = this.getItem(position);

		if (item.getImage() == null)
		{
			imageView.setImageResource(R.drawable.default_invoice);
		}
		else
		{
			imageView.setImageBitmap(item.getImage());			
		}
		
		amountTextView.setText("￥" + Double.toString(item.getAmount()));

		String note = item.getNote().equals("") ? "N/A" : item.getNote();
		infoTextView.setText(note);
		
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
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
