package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.utils.DBManager;
import classes.utils.Utils;

import com.rushucloud.reim.R;
import com.rushucloud.reim.report.ApproveInfoActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class ReportDetailListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	
	private DBManager dbManager;
	private Report report;
	private List<Item> itemList;
	
	public ReportDetailListViewAdapter(Context context, Report report, List<Item> items)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.report = report;
		this.itemList = new ArrayList<Item>(items);
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

			String title = report.getTitle().equals("") ? context.getString(R.string.report_no_name) : report.getTitle();
			titleTextView.setText(title);
			
			timeTextView.setText(Utils.secondToStringUpToMinute(report.getCreatedDate()));

			statusTextView.setText(report.getStatusString());
			statusTextView.setBackgroundResource(report.getStatusBackground());
			
			LayoutParams params = (LayoutParams) statusTextView.getLayoutParams();
			params.width = report.getStatusWidth(context);
			statusTextView.setLayoutParams(params);
			
			TextView approveInfoTextView = (TextView) view.findViewById(R.id.approveInfoTextView);
			approveInfoTextView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(context, ApproveInfoActivity.class);
					intent.putExtra("reportServerID", report.getServerID());
					context.startActivity(intent);
				}
			});

			// init sender, manager and cc			
			TextView senderTextView = (TextView) view.findViewById(R.id.senderTextView);
			TextView managerTextView = (TextView) view.findViewById(R.id.managerTextView);
			TextView ccTextView = (TextView) view.findViewById(R.id.ccTextView);

			if (report.getSender() != null)
			{
				User user = dbManager.getUser(report.getSender().getServerID());
				senderTextView.setText(user.getNickname());
			}

			managerTextView.setText(report.getManagersName());
			ccTextView.setText(report.getCCsName());
			
			// init amount and item count
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView itemCountTextView = (TextView) view.findViewById(R.id.itemCountTextView);
			
			double amount = 0;
			int itemCount = itemList.size();
			
			for (Item item : itemList)
			{
				amount += item.getAmount();
			}

			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(amount));
			itemCountTextView.setText(itemCount + context.getString(R.string.item_count));
			
			return view;
		}
		else 
		{
			View view = layoutInflater.inflate(R.layout.list_report_item_show, parent, false);
			
			TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
			TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
			ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
			
			Item item = itemList.get(position - 1);

			amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
			amountTextView.setText(Utils.formatDouble(item.getAmount()));

			String vendor = item.getVendor().equals("") ? context.getString(R.string.vendor_not_available) : item.getVendor();
			vendorTextView.setText(vendor);
			
			Category category = item.getCategory();
			if (category != null)
			{
				Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
				if (bitmap != null)
				{
					categoryImageView.setImageBitmap(bitmap);				
				}
			}
			
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