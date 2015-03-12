package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.ReimApplication;
import classes.Report;
import classes.utils.DBManager;
import classes.utils.Utils;

public class ReportListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
    private DBManager dbManager;
	private List<Report> reportList;
    private List<Integer> unreadList;

	public ReportListViewAdapter(Context context, List<Report> reports)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
        this.dbManager = DBManager.getDBManager();
		this.reportList = new ArrayList<Report>(reports);
        this.unreadList = new ArrayList<Integer>();
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_report, parent, false);
		}

		TextView statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
		TextView titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
		TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
		TextView amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
        ImageView tipImageView = (ImageView) convertView.findViewById(R.id.tipImageView);
		
		Report report = reportList.get(position);

		statusTextView.setText(report.getStatusString());
		statusTextView.setBackgroundResource(report.getStatusBackground());
		
		String title = report.getTitle().isEmpty() ? context.getString(R.string.report_no_name) : report.getTitle();
		titleTextView.setText(title);

		String date = Utils.secondToStringUpToDay(report.getCreatedDate());
		dateTextView.setText(date.isEmpty() ? context.getString(R.string.not_available) : date);
		
		double amount = dbManager.getReportAmount(report.getLocalID());
		amountTextView.setText(Utils.formatDouble(amount));
		amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        int visibility = unreadList.contains(report.getServerID()) ? View.VISIBLE : View.INVISIBLE;
        tipImageView.setVisibility(visibility);

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

	public void setReportList(List<Report> reports)
	{
		reportList.clear();
		reportList.addAll(reports);
	}

    public void setUnreadList(List<Integer> unreads)
    {
        unreadList.clear();
        unreadList.addAll(unreads);
    }
}