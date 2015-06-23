package classes.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.report.ApproveInfoActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Currency;
import classes.model.Item;
import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class ReportDetailListViewAdapter extends BaseAdapter
{
    private Activity activity;
    private LayoutInflater layoutInflater;

    private DBManager dbManager;
    private Report report;
    private List<Item> itemList;

    public ReportDetailListViewAdapter(Activity activity, Report report, List<Item> items)
    {
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
        this.report = report;
        this.itemList = new ArrayList<>(items);
        this.dbManager = DBManager.getDBManager();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (position == 0)
        {
            View view = layoutInflater.inflate(R.layout.list_report_detail, parent, false);

            // init title, time and status
            TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
            TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);

            String title = report.getTitle().isEmpty() ? activity.getString(R.string.report_no_name) : report.getTitle();
            titleTextView.setText(title);

            timeTextView.setText(Utils.secondToStringUpToMinute(report.getCreatedDate()));

            statusTextView.setText(report.getStatusString());
            statusTextView.setBackgroundResource(report.getStatusBackground());

            TextView approveInfoTextView = (TextView) view.findViewById(R.id.approveInfoTextView);
            approveInfoTextView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    if (report.getSender() != null && report.getSender().getServerID() == AppPreference.getAppPreference().getCurrentUserID())
                    {
                        MobclickAgent.onEvent(activity, "UMENG_REPORT_MINE_STATUS");
                    }
                    else
                    {
                        MobclickAgent.onEvent(activity, "UMENG_REPORT_OTHER_STATUS");
                    }

                    Intent intent = new Intent(activity, ApproveInfoActivity.class);
                    intent.putExtra("reportServerID", report.getServerID());
                    ViewUtils.goForward(activity, intent);
                }
            });

            // init sender
            TextView senderTextView = (TextView) view.findViewById(R.id.senderTextView);

            if (report.getSender() != null)
            {
                User user = dbManager.getUser(report.getSender().getServerID());
                if (user != null)
                {
                    senderTextView.setText(user.getNickname());
                }
            }

            // init manager and cc
            LinearLayout managerLayout = (LinearLayout) view.findViewById(R.id.managerLayout);
            LinearLayout ccLayout = (LinearLayout) view.findViewById(R.id.ccLayout);
            TextView managerTextView = (TextView) view.findViewById(R.id.managerTextView);
            TextView ccTextView = (TextView) view.findViewById(R.id.ccTextView);

            managerLayout.setVisibility(View.VISIBLE);
            ccLayout.setVisibility(View.VISIBLE);

            if (report.getStatus() == Report.STATUS_SUBMITTED)
            {
                managerTextView.setText(report.getManagersName());
                if (report.getCCList() == null || report.getCCList().isEmpty())
                {
                    ccLayout.setVisibility(View.GONE);
                }
                else
                {
                    ccTextView.setText(report.getCCsName());
                }
            }
            else if (report.getStatus() == Report.STATUS_APPROVED)
            {
                managerTextView.setText(R.string.admin);
                ccLayout.setVisibility(View.GONE);
            }
            else
            {
                managerLayout.setVisibility(View.GONE);
                ccLayout.setVisibility(View.GONE);
            }

            // init amount and item count
            TextView totalTextView = (TextView) view.findViewById(R.id.totalTextView);
            TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            TextView itemCountTextView = (TextView) view.findViewById(R.id.itemCountTextView);

            double amount = 0;
            boolean containsForeignCurrency = false;

            for (Item item : itemList)
            {
                if (item.getCurrency().isCNY())
                {
                    amount += item.getAmount();
                }
                else if (item.getRate() != 0)
                {
                    containsForeignCurrency = true;
                    amount += item.getAmount() * item.getRate() / 100;
                }
                else
                {
                    containsForeignCurrency = true;
                    Currency currency = dbManager.getCurrency(item.getCurrency().getCode());
                    amount += item.getAmount() * currency.getRate() / 100;
                }
            }

            int prompt = containsForeignCurrency? R.string.equivalent_amount : R.string.total_amount;
            totalTextView.setText(prompt);
            amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            amountTextView.setText(Utils.formatDouble(amount));
            itemCountTextView.setText(String.format(ViewUtils.getString(R.string.item_count), itemList.size()));

            return view;
        }
        else
        {
            View view = layoutInflater.inflate(R.layout.list_report_item_show, parent, false);

            TextView symbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
            TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
            ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);

            Item item = itemList.get(position - 1);

            symbolTextView.setText(item.getCurrency().getSymbol());

            amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            amountTextView.setText(Utils.formatDouble(item.getAmount()));

            String vendor = item.getVendor().isEmpty() ? activity.getString(R.string.vendor_not_available) : item.getVendor();
            vendorTextView.setText(vendor);

            Category category = item.getCategory();
            ViewUtils.setImageViewBitmap(category, categoryImageView);

            return view;
        }
    }

    public int getCount()
    {
        return itemList.size() + 1;
    }

    public Item getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setReport(Report report)
    {
        this.report = new Report(report);
    }

    public void setItemList(List<Item> items)
    {
        itemList.clear();
        itemList.addAll(items);
    }
}