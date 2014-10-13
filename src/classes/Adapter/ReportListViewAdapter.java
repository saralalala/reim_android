package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Report;
import classes.Utils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Report> reportList;

	public ReportListViewAdapter(Context context, List<Report> reports)
	{
		reportList = new ArrayList<Report>(reports);
		layoutInflater = LayoutInflater.from(context);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_item_report, parent, false);
		}
		
		TextView titleTextView = (TextView)convertView.findViewById(R.id.titleTextView);
		TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
		TextView countTextView = (TextView)convertView.findViewById(R.id.countTextView);
		TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Report report = this.getItem(position);

		String title = report.getTitle().equals("") ? "N/A" : report.getTitle();
		titleTextView.setText(title);
		
		String date = Utils.secondToStringUpToDay(report.getCreatedDate());
		dateTextView.setText(date.equals("") ? "N/A" : date);
		
		countTextView.setText("#" + Integer.toString(report.getItemCount()));

		String status = report.getStatusString().equals("") ? "N/A" : report.getStatusString();
		statusTextView.setText(status);
		
		amountTextView.setText("￥" + Double.toString(report.getTotalAmount()));
		
		return convertView;
	}
	
	public int getCount()
	{
		return reportList.size();
	}

	public Report getItem(int position)
	{
		return reportList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void clear()
	{
		reportList.clear();
	}
	
	public void set(List<Report> reports)
	{
		reportList = new ArrayList<Report>(reports);
	}
}
