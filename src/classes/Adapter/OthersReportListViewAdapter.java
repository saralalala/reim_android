package classes.Adapter;

import java.util.ArrayList;
import java.util.List;

import classes.ReimApplication;
import classes.Report;
import classes.Utils.Utils;

import com.rushucloud.reim.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OthersReportListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private List<Report> reportList;

	public OthersReportListViewAdapter(Context context, List<Report> reports)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.reportList = new ArrayList<Report>(reports);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_report, parent, false);
		}
		
		TextView titleTextView = (TextView)convertView.findViewById(R.id.titleTextView);
		TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
		ImageView statusImageView = (ImageView)convertView.findViewById(R.id.statusImageView);
		TextView amountTextView = (TextView)convertView.findViewById(R.id.amountTextView);

		Report report = reportList.get(position);

		String title = report.getTitle().equals("") ? context.getString(R.string.not_available) : report.getTitle();
		titleTextView.setText(title);
		
		String date = Utils.secondToStringUpToDay(report.getCreatedDate());
		dateTextView.setText(date.equals("") ? context.getString(R.string.not_available) : date);

		if (report.getStatus() >= 0 && report.getStatus() <= 4)
		{
			statusImageView.setImageResource(report.getStatusBackground());		
		}

		double amount = Double.valueOf(report.getAmount());
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