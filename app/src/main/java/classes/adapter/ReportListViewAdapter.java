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

import classes.model.Report;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.ReimApplication;
import classes.utils.Utils;

public class ReportListViewAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private DBManager dbManager;
    private List<Report> reportList;
    private List<Integer> unreadList;
    private int tabIndex = Constant.TAB_REPORT_MINE;

    public ReportListViewAdapter(Context context, List<Report> reports)
    {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.dbManager = DBManager.getDBManager();
        this.reportList = new ArrayList<>(reports);
        this.unreadList = new ArrayList<>();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (tabIndex == Constant.TAB_REPORT_MINE)
        {
            View view = layoutInflater.inflate(R.layout.list_report, parent, false);

            TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);
            TextView confirmTextView = (TextView) view.findViewById(R.id.confirmTextView);
            TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            ImageView tipImageView = (ImageView) view.findViewById(R.id.tipImageView);

            Report report = reportList.get(position);

            statusTextView.setText(report.getStatusString());
            statusTextView.setBackgroundResource(report.getStatusBackground());

            if (report.getStatus() == Report.STATUS_NEED_CONFIRM)
            {
                confirmTextView.setText(R.string.status_need_confirm);
                confirmTextView.setVisibility(View.VISIBLE);
            }
            else if (report.getStatus() == Report.STATUS_CONFIRMED)
            {
                confirmTextView.setText(R.string.status_confirmed);
                confirmTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                confirmTextView.setVisibility(View.GONE);
            }

            String title = report.getTitle().isEmpty() ? context.getString(R.string.report_no_name) : report.getTitle();
            titleTextView.setText(title);

            String date = Utils.secondToStringUpToDay(report.getCreatedDate());
            dateTextView.setText(date.isEmpty() ? context.getString(R.string.not_available) : date);

            double amount = dbManager.getReportAmount(report.getLocalID());
            amountTextView.setText(Utils.formatDouble(amount));
            amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

            int visibility = unreadList.contains(report.getServerID()) ? View.VISIBLE : View.INVISIBLE;
            tipImageView.setVisibility(visibility);

            return view;
        }
        else
        {
            Report report = reportList.get(position);
            if (!report.getSectionName().isEmpty())
            {
                if (report.getSectionName().equals(context.getString(R.string.pending)))
                {
                    View view = layoutInflater.inflate(R.layout.list_header, parent, false);

                    TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
                    headerTextView.setText(R.string.pending);

                    return view;
                }
                else if (report.getSectionName().equals(context.getString(R.string.no_pending_reports)))
                {
                    return layoutInflater.inflate(R.layout.list_no_pending_report, parent, false);
                }
                else if (report.getSectionName().equals(context.getString(R.string.processed)))
                {
                    View view = layoutInflater.inflate(R.layout.list_header, parent, false);

                    TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
                    headerTextView.setText(R.string.processed);

                    return view;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                View view = layoutInflater.inflate(R.layout.list_report, parent, false);

                TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);
                TextView ccTextView = (TextView) view.findViewById(R.id.ccTextView);
                TextView senderTextView = (TextView) view.findViewById(R.id.senderTextView);
                TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
                TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
                TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                ImageView tipImageView = (ImageView) view.findViewById(R.id.tipImageView);

                statusTextView.setText(report.getStatusString());
                statusTextView.setBackgroundResource(report.getStatusBackground());

                int visibility = report.isCC() ? View.VISIBLE : View.GONE;
                ccTextView.setVisibility(visibility);

                String nickname = report.getSender() == null ? "" : report.getSender().getNickname();
                senderTextView.setText(context.getString(R.string.prompt_sender) + nickname);

                String title = report.getTitle().isEmpty() ? context.getString(R.string.report_no_name) : report.getTitle();
                titleTextView.setText(title);

                String date = Utils.secondToStringUpToDay(report.getCreatedDate());
                dateTextView.setText(date.isEmpty() ? context.getString(R.string.not_available) : date);

                double amount = Double.valueOf(report.getAmount());
                amountTextView.setText(Utils.formatDouble(amount));
                amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

                visibility = unreadList.contains(report.getServerID()) ? View.VISIBLE : View.INVISIBLE;
                tipImageView.setVisibility(visibility);

                return view;
            }
        }
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

    public void setTabIndex(int tabIndex)
    {
        this.tabIndex = tabIndex;
    }
}