package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.AppPreference;
import classes.Item;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
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
		
		AppPreference appPreference = AppPreference.getAppPreference();
		
		Item item = this.getItem(position);

		Bitmap bitmap = item.getImage() == null ? appPreference.getDefaultInvoice() : item.getImage();
		imageView.setImageBitmap(bitmap);
		
		String amount = "￥" + Double.toString(item.getAmount());
		amountTextView.setText(amount);

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
}
