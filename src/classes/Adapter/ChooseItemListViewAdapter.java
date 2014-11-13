package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Item;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
		}


		int color = check[position] ? R.color.list_item_selected : R.color.list_item_not_selected;
		convertView.setBackgroundResource(color);
		
		ImageView imageView = (ImageView)convertView.findViewById(R.id.photoImageView);
		TextView reportTextView = (TextView)convertView.findViewById(R.id.reportTextView);
		TextView vendorTextView = (TextView)convertView.findViewById(R.id.vendorTextView);
		TextView categoryTextView = (TextView)convertView.findViewById(R.id.categoryTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Item item = this.getItem(position);

		Bitmap bitmap = BitmapFactory.decodeFile(item.getInvoicePath());
		if (item.getImageID() != -1 || bitmap != null)
		{
			imageView.setImageResource(R.drawable.default_invoice);
		}
		else
		{
			imageView.setImageResource(R.drawable.default_avatar);			
		}
		
		amountTextView.setText("￥" + Double.toString(item.getAmount()));

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
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
