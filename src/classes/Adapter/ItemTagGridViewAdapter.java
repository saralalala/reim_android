package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Tag;

import com.rushucloud.reim.R;
import android.content.Context;
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

	public ItemTagGridViewAdapter(Context context, List<Tag> tags)
	{
		layoutInflater = LayoutInflater.from(context);
		tagList = new ArrayList<Tag>(tags);
		check = new boolean[tagList.size()];
		for (int i = 0; i < tags.size(); i++)
		{
			check[i] = false;
		}
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_item_tag, parent, false);
		}

		ImageView iconImageView = (ImageView)convertView.findViewById(R.id.iconImageView);
		if (check[position])
		{
			iconImageView.setBackgroundResource(R.drawable.umeng_socialize_evernote);						
		}
		else
		{
			iconImageView.setBackgroundResource(R.drawable.ic_launcher);							
		}
		
		TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
		nameTextView.setText(tagList.get(position).getName());
		
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
