package classes.Adapter;

import java.util.ArrayList;
import java.util.List;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReportDetailListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	
	private Report report;
	private List<Item> itemList;
	
	private int screenWidth;
	private int interval;
	private int padding;
	private int sideLength;
	private int iconCount;

	public ReportDetailListViewAdapter(Context context, Report report, List<Item> items)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		
		this.report = report;
		this.itemList = new ArrayList<Item>(items);
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		this.screenWidth = metrics.widthPixels;
		this.padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		this.interval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
		this.sideLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		switch (position)
		{
			case 0:
			{
				view = layoutInflater.inflate(R.layout.list_report_title, parent, false);
				
				TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
				TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
				ImageView statusImageView = (ImageView) view.findViewById(R.id.statusImageView);

				String title = report.getTitle().equals("") ? context.getString(R.string.not_available) : report.getTitle();
				titleTextView.setText(title);
				
				timeTextView.setText(Utils.secondToStringUpToMinute(report.getCreatedDate()));

				statusImageView.setImageResource(report.getStatusBackground());
				
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_report_user, parent, false);
				
				TextView senderTextView = (TextView) view.findViewById(R.id.senderTextView);
				TextView managerTextView = (TextView) view.findViewById(R.id.managerTextView);
				TextView ccTextView = (TextView) view.findViewById(R.id.ccTextView);

				if (report.getSender() != null)
				{
					senderTextView.setText(report.getSender().getNickname());					
				}
				
				managerTextView.setText(report.getManagersName());
				ccTextView.setText(report.getCCsName());
				
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_report_detail, parent, false);
				
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
				
				break;
			}
			default:
			{
				view = layoutInflater.inflate(R.layout.list_report_item_show, parent, false);
				
				TextView amountTextView = (TextView)view.findViewById(R.id.amountTextView);
				TextView vendorTextView = (TextView)view.findViewById(R.id.vendorTextView);
				LinearLayout iconLayout = (LinearLayout)view.findViewById(R.id.iconLayout);
				ImageView categoryImageView = (ImageView)view.findViewById(R.id.categoryImageView);
				
				Item item = itemList.get(position - 3);

				amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
				amountTextView.setText(Utils.formatDouble(item.getAmount()));

				String vendor = item.getMerchant().equals("") ? context.getString(R.string.not_available) : item.getMerchant();
				vendorTextView.setText(vendor);
				
				// category 和 tag 一共iconCount个
				Bitmap bitmap = BitmapFactory.decodeFile(item.getCategory().getIconPath());
				if (bitmap != null)
				{
					categoryImageView.setImageBitmap(bitmap);				
				}

				iconLayout.removeAllViews();
				
				amountTextView.measure(0,0);

				// category 和 tag 一共iconCount个
				iconCount = (screenWidth - amountTextView.getMeasuredWidth() - padding * 3 + interval) / (sideLength + interval);
				iconCount = 1;
				for (int i = 0; i < iconCount; i++)
				{
					ImageView iconImageView = new ImageView(context);
					iconImageView.setImageResource(R.drawable.food);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
					params.rightMargin = interval;
					iconLayout.addView(iconImageView, params);
				}

				iconLayout.addView(categoryImageView);
				break;
			}
		}
		return view;
	}
	
	public int getCount()
	{
		return itemList.size() + 3;
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
		this.report = report;
	}
	
	public void setItemList(List<Item> items)
	{
		itemList.clear();
		itemList.addAll(items);
	}
}