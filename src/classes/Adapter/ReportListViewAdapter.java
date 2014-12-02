package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.ReimApplication;
import classes.Report;
import classes.Utils;

import com.rushucloud.reim.R;

import database.DBManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Report> reportList;
	private DBManager dbManager;
	private int[] statusBackground;

	public ReportListViewAdapter(Context context, List<Report> reports)
	{
		reportList = new ArrayList<Report>(reports);
		layoutInflater = LayoutInflater.from(context);
		dbManager = DBManager.getDBManager();
		statusBackground = new int[] { R.drawable.report_status_draft, R.drawable.report_status_submitted, R.drawable.report_status_approved, 
									   R.drawable.report_status_rejected, R.drawable.report_status_finished };
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_report, parent, false);
		}
		
		TextView titleTextView = (TextView)convertView.findViewById(R.id.titleTextView);
		TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
		final ImageView statusImageView = (ImageView)convertView.findViewById(R.id.statusImageView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);
		
		Report report = reportList.get(position);

		String title = report.getTitle().equals("") ? "N/A" : report.getTitle();
		titleTextView.setText(title);
		
		String date = Utils.secondToStringUpToDay(report.getCreatedDate());
		dateTextView.setText(date.equals("") ? "N/A" : date);

		if (report.getStatus() >= 0 && report.getStatus() <= 4)
		{
			statusImageView.setBackgroundResource(statusBackground[report.getStatus()]);
		}

		double amount = dbManager.getReportAmount(report.getLocalID());
		amountTextView.setText(Utils.formatDouble(amount));
		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
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

	public void set(List<Report> reports)
	{
		reportList.clear();
		reportList.addAll(reports);
	}
}
