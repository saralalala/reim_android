package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportTagGridViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private String[] status;
	private int[] colors;
	private boolean[] check;

	public ReportTagGridViewAdapter(Context context)
	{
		layoutInflater = LayoutInflater.from(context);
		status = context.getResources().getStringArray(R.array.filterStatus);
		check = new boolean[5];
		for (int i = 0; i < 5; i++)
		{
			check[i] = false;
		}
		
		colors = new int[5];
		colors[0] = Color.rgb(145,124,107);
		colors[1] = Color.rgb(61,160,228);
		colors[2] = Color.rgb(60,183,154);
		colors[3] = Color.rgb(178,120,160);
		colors[4] = Color.rgb(209,209,209);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_report_tag, parent, false);
		}
		
		TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		statusTextView.setText(status[position]);
		
		if (check[position])
		{
			statusTextView.setTextColor(Color.WHITE);
			statusTextView.setBackgroundColor(colors[position]);
		}
		else
		{
			statusTextView.setTextColor(colors[position]);
			statusTextView.setBackgroundColor(Color.WHITE);
		}
		
		return convertView;
	}
	
	public int getCount()
	{
		return 5;
	}

	public Boolean getItem(int position)
	{
		return check[position];
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setSelection(int position)
	{
		check[position] = !check[position];
	}
	
	public List<Integer> getFilterStatusList()
	{
		List<Integer> filterStatusList = new ArrayList<Integer>();
		for (int i = 0; i < check.length; i++)
		{
			if (check[i])
			{
				filterStatusList.add(i);
			}
		}
		return filterStatusList;
	}
	
	public boolean[] getCheckedTags()
	{
		return check;
	}
}
