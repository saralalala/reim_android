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
            ViewHolder viewHolder;
            if (convertView == null || convertView.getTag() == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_report, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
                viewHolder.ccTextView = (TextView) convertView.findViewById(R.id.ccTextView);
                viewHolder.confirmTextView = (TextView) convertView.findViewById(R.id.confirmTextView);
                viewHolder.senderTextView = (TextView) convertView.findViewById(R.id.senderTextView);
                viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                viewHolder.amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
                viewHolder.tipImageView = (ImageView) convertView.findViewById(R.id.tipImageView);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Report report = reportList.get(position);

            viewHolder.ccTextView.setVisibility(View.GONE);
            viewHolder.senderTextView.setVisibility(View.GONE);

            viewHolder.statusTextView.setText(report.getStatusString());
            viewHolder.statusTextView.setBackgroundResource(report.getStatusBackground());

            if (report.getStatus() == Report.STATUS_NEED_CONFIRM)
            {
                viewHolder.confirmTextView.setText(R.string.status_need_confirm);
                viewHolder.confirmTextView.setVisibility(View.VISIBLE);
            }
            else if (report.getStatus() == Report.STATUS_CONFIRMED)
            {
                viewHolder.confirmTextView.setText(R.string.status_confirmed);
                viewHolder.confirmTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.confirmTextView.setVisibility(View.GONE);
            }

            String title = report.getTitle().isEmpty() ? context.getString(R.string.report_no_name) : report.getTitle();
            viewHolder.titleTextView.setText(title);

            String date = Utils.secondToStringUpToDay(report.getCreatedDate());
            viewHolder.dateTextView.setText(date.isEmpty() ? context.getString(R.string.not_available) : date);

            double amount = dbManager.getReportAmount(report.getLocalID());
            viewHolder.amountTextView.setText(Utils.formatDouble(amount));
            viewHolder.amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

            int visibility = unreadList.contains(report.getServerID()) ? View.VISIBLE : View.INVISIBLE;
            viewHolder.tipImageView.setVisibility(visibility);

            return convertView;
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
                ViewHolder viewHolder;
                if (convertView == null || convertView.getTag() == null)
                {
                    convertView = layoutInflater.inflate(R.layout.list_report, parent, false);

                    viewHolder = new ViewHolder();
                    viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
                    viewHolder.ccTextView = (TextView) convertView.findViewById(R.id.ccTextView);
                    viewHolder.confirmTextView = (TextView) convertView.findViewById(R.id.confirmTextView);
                    viewHolder.senderTextView = (TextView) convertView.findViewById(R.id.senderTextView);
                    viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
                    viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                    viewHolder.amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
                    viewHolder.tipImageView = (ImageView) convertView.findViewById(R.id.tipImageView);

                    convertView.setTag(viewHolder);
                }
                else
                {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.confirmTextView.setVisibility(View.GONE);

                viewHolder.statusTextView.setText(report.getStatusString());
                viewHolder.statusTextView.setBackgroundResource(report.getStatusBackground());

                int visibility = report.isCC() ? View.VISIBLE : View.GONE;
                viewHolder.ccTextView.setVisibility(visibility);

                String nickname = report.getSender() == null ? "" : report.getSender().getNickname();
                viewHolder.senderTextView.setVisibility(View.VISIBLE);
                viewHolder.senderTextView.setText(context.getString(R.string.prompt_sender) + nickname);

                String title = report.getTitle().isEmpty() ? context.getString(R.string.report_no_name) : report.getTitle();
                viewHolder.titleTextView.setText(title);

                String date = Utils.secondToStringUpToDay(report.getCreatedDate());
                viewHolder.dateTextView.setText(date.isEmpty() ? context.getString(R.string.not_available) : date);

                double amount = Double.valueOf(report.getAmount());
                viewHolder.amountTextView.setText(Utils.formatDouble(amount));
                viewHolder.amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

                visibility = unreadList.contains(report.getServerID()) ? View.VISIBLE : View.INVISIBLE;
                viewHolder.tipImageView.setVisibility(visibility);

                return convertView;
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

    private static class ViewHolder
    {
        TextView statusTextView;
        TextView ccTextView;
        TextView confirmTextView;
        TextView senderTextView;
        TextView titleTextView;
        TextView dateTextView;
        TextView amountTextView;
        ImageView tipImageView;
    }
}