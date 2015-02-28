package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Report;
import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ReportTagGridViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private int[] backgrounds;
	private boolean[] check;

	public ReportTagGridViewAdapter(Context context)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.check = new boolean[5];
		for (int i = 0; i < 5; i++)
		{
			check[i] = false;
		}
		
		backgrounds = new int[]{ R.drawable.report_tag_draft, R.drawable.report_tag_submitted, R.drawable.report_tag_approved,
				 				 R.drawable.report_tag_rejected, R.drawable.report_tag_finished };
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{		
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_report_tag, parent, false);
		}
		
		TextView statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
		statusTextView.setText(Report.getStatusString(position));
		statusTextView.setBackgroundResource(backgrounds[position]);
		
		RelativeLayout coverLayout = (RelativeLayout) convertView.findViewById(R.id.coverLayout);
		int visibility = check[position] ? View.VISIBLE : View.INVISIBLE;
		coverLayout.setVisibility(visibility);
		
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
	
	public void setCheck(boolean[] checks)
	{
		for (int i = 0; i < checks.length; i++)
		{
			check[i] = checks[i];
		}
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
