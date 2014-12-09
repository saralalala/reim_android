package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Tag;

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

public class ItemTagGridViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Tag> tagList;
	private boolean[] check;
	private int selectedColor;
	private int unselectedColor;

	public ItemTagGridViewAdapter(Context context, List<Tag> tags)
	{
		this.layoutInflater = LayoutInflater.from(context);
		
		this.tagList = new ArrayList<Tag>(tags);
		this.check = new boolean[tagList.size()];
		for (int i = 0; i < tags.size(); i++)
		{
			check[i] = false;
		}
		this.selectedColor = context.getResources().getColor(R.color.major_dark);
		this.unselectedColor = context.getResources().getColor(R.color.font_major_dark);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_item_tag, parent, false);
		}

		ImageView iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		
		Tag tag = tagList.get(position);
		
		if (check[position])
		{
			iconImageView.setImageResource(R.drawable.tag_chosen);
			nameTextView.setTextColor(selectedColor);					
		}
		else
		{
			Bitmap icon = BitmapFactory.decodeFile(tag.getIconPath());
			if (icon != null)
			{
				iconImageView.setImageBitmap(icon);					
			}
			nameTextView.setTextColor(unselectedColor);						
		}
		
		nameTextView.setText(tag.getName());
		
		return convertView;
	}
	
	public int getCount()
	{
		return tagList.size();
	}

	public Tag getItem(int position)
	{
		return tagList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setSelection(int position)
	{
		check[position] = !check[position];
	}
	
	public boolean[] getCheckedTags()
	{
		return check;
	}
}
