package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Report;

import com.rushucloud.reim.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportTagGridViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private Resources resources;
	private List<Integer> fontColors;
	private int[] selectedBackgrounds;
	private int[] unselectedBackgrounds;
	private boolean[] check;

	public ReportTagGridViewAdapter(Context context)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.resources = context.getResources();
		this.check = new boolean[5];
		for (int i = 0; i < 5; i++)
		{
			check[i] = false;
		}
		
		this.fontColors = new ArrayList<Integer>();
		this.fontColors.add(context.getResources().getColor(R.color.report_status_draft));
		this.fontColors.add(context.getResources().getColor(R.color.report_status_submitted));
		this.fontColors.add(context.getResources().getColor(R.color.report_status_approved));
		this.fontColors.add(context.getResources().getColor(R.color.report_status_rejected));
		this.fontColors.add(context.getResources().getColor(R.color.report_status_finished));
		
		selectedBackgrounds = new int[]{ R.drawable.report_tag_draft_selected, R.drawable.report_tag_submitted_selected, R.drawable.report_tag_approved_selected,
										 R.drawable.report_tag_rejected_selected, R.drawable.report_tag_finished_selected };
		
		unselectedBackgrounds = new int[]{ R.drawable.report_tag_draft_unselected, R.drawable.report_tag_submitted_unselected, R.drawable.report_tag_approved_unselected,
				 						   R.drawable.report_tag_rejected_unselected, R.drawable.report_tag_finished_unselected };
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{		
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.grid_report_tag, parent, false);
		}
		
		final TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		statusTextView.setText(Report.getStatusString(position));
		statusTextView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(resources, selectedBackgrounds[position]);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = statusTextView.getLayoutParams();
				params.height = (int)(statusTextView.getWidth() * ratio);;
				statusTextView.setLayoutParams(params);
			}
		});
		
		if (check[position])
		{
			statusTextView.setTextColor(Color.WHITE);
			statusTextView.setBackgroundResource(selectedBackgrounds[position]);
		}
		else
		{
			statusTextView.setTextColor(fontColors.get(position));
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
