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
	private int[] fontColors;
	private int[] selectedBackgrounds;
	private int[] unselectedBackgrounds;
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
		
		fontColors = new int[]{ R.color.report_status_draft, R.color.report_status_submitted, R.color.report_status_approved, 
							R.color.report_status_rejected, R.color.report_status_finished };
		
		selectedBackgrounds = new int[]{ R.drawable.report_tag_draft_selected, R.drawable.report_tag_submitted_selected, R.drawable.report_tag_approved_selected,
										 R.drawable.report_tag_rejected_selected, R.drawable.report_tag_finished_selected };
		
		unselectedBackgrounds = new int[]{ R.drawable.report_tag_draft_unselected, R.drawable.report_tag_submitted_unselected, R.drawable.report_tag_approved_unselected,
				 						   R.drawable.report_tag_rejected_unselected, R.drawable.report_tag_finished_unselected };
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (position == 0)
		{

			View view = layoutInflater.inflate(R.layout.grid_report_tag_draft, parent, false);
			
			TextView statusTextView = (TextView)view.findViewById(R.id.statusTextView);
			statusTextView.setText(status[position]);
			
			if (check[position])
			{
				statusTextView.setTextColor(Color.WHITE);
				statusTextView.setBackgroundResource(selectedBackgrounds[position]);
			}
			else
			{
				statusTextView.setTextColor(fontColors[position]);
				statusTextView.setBackgroundResource(unselectedBackgrounds[position]);
			}
			
			return view;
		}
		
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_report_tag, parent, false);
		}
		
		TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		statusTextView.setText(status[position]);
		
		if (check[position])
		{
			statusTextView.setTextColor(Color.WHITE);
			statusTextView.setBackgroundResource(selectedBackgrounds[position]);
		}
		else
		{
			statusTextView.setTextColor(fontColors[position]);
			statusTextView.setBackgroundResource(unselectedBackgrounds[position]);
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
