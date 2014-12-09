package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Category;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Category> categoryList;
	private boolean[] check;
	private int selectedColor;
	private int unselectedColor;
	
	public CategoryListViewAdapter(Context context, List<Category> categories, boolean[] checkList)
	{
		this.layoutInflater = LayoutInflater.from(context);
		
		this.categoryList = new ArrayList<Category>(categories);
		this.check = checkList;
		this.selectedColor = context.getResources().getColor(R.color.major_dark);
		this.unselectedColor = context.getResources().getColor(R.color.font_major_dark);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_category, parent, false);
		}

		ImageView iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		
		Category category = categoryList.get(position);
		if (!category.getIconPath().equals(""))
		{
			Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
			if (bitmap != null)
			{
				iconImageView.setImageBitmap(bitmap);				
			}
		}
		
		if (category.getName().equals(""))
		{
			nameTextView.setText(R.string.notAvailable);
		}
		else
		{
			nameTextView.setText(category.getName());			
		}

		if (check != null)
		{
			int color = check[position] ? selectedColor : unselectedColor;
			nameTextView.setTextColor(color);			
		}
		
		return convertView;
	}
	
	public int getCount()
	{
		return categoryList.size();
	}

	public Category getItem(int position)
	{
		return categoryList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setCategory(List<Category> categories)
	{
		categoryList.clear();
		categoryList.addAll(categories);
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}
