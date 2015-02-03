package classes.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StatisticsListViewAdapter extends BaseAdapter
{
	private View view;
	
	public StatisticsListViewAdapter(Context context, View view)
	{
		this.view = view;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{		
		return view;
	}
	
	public int getCount()
	{
		return 1;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setView(View view)
	{
		this.view = view;
	}
}