package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Tag;
import classes.utils.ViewUtils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Tag> tagList;
	private boolean[] check;
	private int selectedColor;
	private int unselectedColor;
	
	public TagListViewAdapter(Context context, List<Tag> tags, boolean[] checkList)
	{
		this.layoutInflater = LayoutInflater.from(context);
		
		this.tagList = new ArrayList<Tag>(tags);
		this.check = checkList;
		this.selectedColor = ViewUtils.getColor(R.color.major_dark);
		this.unselectedColor = ViewUtils.getColor(R.color.font_major_dark);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_tag, parent, false);
		}
		
		if (check != null)
		{
			int color = check[position] ? R.color.list_item_selected : R.color.list_item_unselected;
			convertView.setBackgroundResource(color);
		}

		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		
		Tag tag = tagList.get(position);
		
		if (tag.getName().isEmpty())
		{
			nameTextView.setText(R.string.not_available);
		}
		else
		{
			nameTextView.setText(tag.getName());			
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
	
	public void setTag(List<Tag> tags)
	{
		tagList.clear();
		tagList.addAll(tags);
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
}