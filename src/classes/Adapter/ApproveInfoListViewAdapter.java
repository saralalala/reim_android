package classes.adapter;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.AlertRequest;
import classes.ApproveInfo;
import classes.Report;
import classes.User;
import classes.utils.DBManager;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;

import com.rushucloud.reim.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApproveInfoListViewAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private DBManager dbManager;
	private HttpConnectionCallback callback;
	private List<ApproveInfo> infoList;
	private int reportStatus;
	private int reportServerID;
	
	public ApproveInfoListViewAdapter(Context context, Report report, List<ApproveInfo> infos, HttpConnectionCallback callback)
	{
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		this.callback = callback;
		this.dbManager = DBManager.getDBManager();
		this.reportStatus = report.getStatus();
		this.reportServerID = report.getServerID();
		this.infoList = new ArrayList<ApproveInfo>(infos);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_approve_info, parent, false);
		}

		CircleImageView pointImageView = (CircleImageView)convertView.findViewById(R.id.pointImageView);
		LinearLayout upperLayout = (LinearLayout)convertView.findViewById(R.id.upperLayout);
		LinearLayout lowerLayout = (LinearLayout)convertView.findViewById(R.id.lowerLayout);
		CircleImageView avatarImageView = (CircleImageView)convertView.findViewById(R.id.avatarImageView);
		TextView nicknameTextView = (TextView)convertView.findViewById(R.id.nicknameTextView);
		TextView statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);
		LinearLayout timeLayout = (LinearLayout)convertView.findViewById(R.id.timeLayout);
		ImageView alarmImageView = (ImageView)convertView.findViewById(R.id.alarmImageView);
		
		ApproveInfo info = this.getItem(position);

		final User user = dbManager.getUser(info.getUserID());
		
		if (position < infoList.size() - 1)
		{
			lowerLayout.setVisibility(View.VISIBLE);
			ApproveInfo nextInfo = this.getItem(position + 1);
			if (nextInfo.hasApproved() || (info.hasApproved() && !nextInfo.hasApproved()))
			{
				lowerLayout.setBackgroundColor(ViewUtils.getColor(R.color.status_approved));
			}
			else
			{
				lowerLayout.setBackgroundColor(ViewUtils.getColor(R.color.background_grey));
			}
		}
		
		if (position > 0)
		{			
			upperLayout.setVisibility(View.VISIBLE);
			ApproveInfo previousInfo = this.getItem(position - 1);
			if (previousInfo.hasApproved())
			{
				upperLayout.setBackgroundColor(ViewUtils.getColor(R.color.status_approved));
			}
			else
			{
				upperLayout.setBackgroundColor(ViewUtils.getColor(R.color.background_grey));
			}			
		}
		
		if (position == 0)
		{
			pointImageView.setVisibility(View.VISIBLE);
			upperLayout.setVisibility(View.GONE);
			if (info.hasApproved())
			{
				pointImageView.setImageResource(R.drawable.point_approved);
			}
			else
			{
				pointImageView.setImageResource(R.drawable.point_not_approved);				
			}
		}
		else
		{
			pointImageView.setVisibility(View.GONE);
			upperLayout.setVisibility(View.VISIBLE);
		}
		
		if (position == infoList.size() - 1)
		{
			lowerLayout.setVisibility(View.GONE);
		}
		else
		{
			lowerLayout.setVisibility(View.VISIBLE);			
		}

		avatarImageView.setImageResource(R.drawable.default_avatar);
		nicknameTextView.setText(R.string.not_available);	
		
		if (user != null)
		{
			if (!user.getAvatarPath().isEmpty())
			{
				Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarPath());
				if (bitmap != null)
				{
					avatarImageView.setImageBitmap(bitmap);
				}			
			}
			
			nicknameTextView.setText(user.getNickname());
		}

		if (info.hasApproved())
		{
			if (info.getStatus() == Report.STATUS_APPROVED)
			{
				statusTextView.setText(R.string.approved);
				statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));
			}
			else
			{
				statusTextView.setText(R.string.rejected);
				statusTextView.setTextColor(ViewUtils.getColor(R.color.status_rejected));				
			}

			alarmImageView.setVisibility(View.GONE);
			
			timeLayout.setVisibility(View.VISIBLE);
			
			TextView timeTextView = (TextView)convertView.findViewById(R.id.timeTextView);
			timeTextView.setText(info.getApproveTime());
			
			TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
			dateTextView.setText(info.getApproveDate());
		}
		else
		{
			statusTextView.setText(R.string.ready_to_approved);
			statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

			timeLayout.setVisibility(View.GONE);

			alarmImageView.setVisibility(View.VISIBLE);
			alarmImageView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					if (reportStatus == Report.STATUS_SUBMITTED)
					{
						AlertRequest request = new AlertRequest(user.getServerID(), reportServerID);
						request.sendRequest(callback);						
					}
					else
					{
						ViewUtils.showToast(context, R.string.prompt_no_need_to_alarm);
					}
				}
			});
			
			if (reportStatus != Report.STATUS_SUBMITTED)
			{
				alarmImageView.setImageResource(R.drawable.alarm_disabled_drawable);
			}
		}
		
		return convertView;
	}
	
	public int getCount()
	{
		return infoList.size();
	}

	public ApproveInfo getItem(int position)
	{
		return infoList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
		
	public void setInfoList(List<ApproveInfo> infos)
	{
		infoList.clear();
		infoList.addAll(infos);
	}	
}
