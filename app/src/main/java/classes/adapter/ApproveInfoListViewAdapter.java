package classes.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.ApproveInfo;
import classes.Report;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.AlertRequest;
import netUtils.Response.Report.AlertResponse;

public class ApproveInfoListViewAdapter extends BaseAdapter
{
	private Activity activity;
	private LayoutInflater layoutInflater;
	private DBManager dbManager;
    private User currentUser;
	private Report report;
	private List<ApproveInfo> infoList;
	private List<Integer> stepStartList;
	
	public ApproveInfoListViewAdapter(Activity activity, Report report, List<ApproveInfo> infos)
	{
		this.activity = activity;
		this.layoutInflater = LayoutInflater.from(activity);
		this.dbManager = DBManager.getDBManager();
        this.currentUser = AppPreference.getAppPreference().getCurrentUser();
		this.report = report;
		this.infoList = new ArrayList<ApproveInfo>(infos);
		this.stepStartList = new ArrayList<Integer>();
		initStepList();
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_approve_info, parent, false);
		}

		CircleImageView pointImageView = (CircleImageView) convertView.findViewById(R.id.pointImageView);
		LinearLayout upperLayout = (LinearLayout) convertView.findViewById(R.id.upperLayout);
		LinearLayout lowerLayout = (LinearLayout) convertView.findViewById(R.id.lowerLayout);
		
		CircleImageView avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
		TextView nicknameTextView = (TextView) convertView.findViewById(R.id.nicknameTextView);
		TextView statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
		LinearLayout timeLayout = (LinearLayout) convertView.findViewById(R.id.timeLayout);
		ImageView alarmImageView = (ImageView) convertView.findViewById(R.id.alarmImageView);
		
		ApproveInfo info = getItem(position);
				
		if (position == 0)
		{
			pointImageView.setVisibility(View.VISIBLE);
			upperLayout.setVisibility(View.GONE);
            lowerLayout.setVisibility(View.VISIBLE);

			int point = info.hasApproved() ? R.drawable.point_approved : R.drawable.point_not_approved;
			pointImageView.setImageResource(point);

            int color = info.hasApproved() ? R.color.status_approved : R.color.background_grey;
            lowerLayout.setBackgroundColor(ViewUtils.getColor(color));
		}
		else
		{
			if (stepStartList.contains(position))
			{
				pointImageView.setVisibility(View.VISIBLE);
				int point = info.hasApproved() ? R.drawable.point_approved : R.drawable.point_not_approved;
				pointImageView.setImageResource(point);			
			}
			else
			{
				pointImageView.setVisibility(View.GONE);				
			}
			upperLayout.setVisibility(View.VISIBLE);
            lowerLayout.setVisibility(View.VISIBLE);
			
			ApproveInfo previousInfo = getItem(position - 1);
			int color = previousInfo.hasApproved() ? R.color.status_approved : R.color.background_grey;
			upperLayout.setBackgroundColor(ViewUtils.getColor(color));
			
			color = info.hasApproved() ? R.color.status_approved : R.color.background_grey;
			lowerLayout.setBackgroundColor(ViewUtils.getColor(color));
		}
		
		if (position == infoList.size() - 1)
		{
			lowerLayout.setVisibility(View.GONE);
		}

		avatarImageView.setImageResource(R.drawable.default_avatar);
		nicknameTextView.setText(R.string.not_available);	

		final User user = dbManager.getUser(info.getUserID());
		if (user != null)
		{
            ViewUtils.setImageViewBitmap(user, avatarImageView);
			nicknameTextView.setText(user.getNickname());
		}
        else
        {
            statusTextView.setText(R.string.not_available);
            alarmImageView.setVisibility(View.GONE);
            timeLayout.setVisibility(View.GONE);

            return convertView;
        }

        if (info.getUserID() == info.getReportSenderID() && info.hasApproved())
        {
            statusTextView.setText(R.string.submitted);
            statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));

            alarmImageView.setVisibility(View.GONE);
            timeLayout.setVisibility(View.VISIBLE);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            timeTextView.setText(info.getApproveTime());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            dateTextView.setText(info.getApproveDate());
        }
        else if (info.getUserID() == info.getReportSenderID())
        {
            statusTextView.setText(R.string.ready_to_submitted);
            statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

            alarmImageView.setVisibility(View.GONE);
            timeLayout.setVisibility(View.GONE);
        }
        else if (info.hasApproved())
        {
            if (info.getRealStatus() == Report.STATUS_APPROVED)
            {
                statusTextView.setText(R.string.approved);
                statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));

                alarmImageView.setVisibility(View.GONE);
                timeLayout.setVisibility(View.VISIBLE);

                TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
                timeTextView.setText(info.getApproveTime());

                TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                dateTextView.setText(info.getApproveDate());
            }
            else if (info.getRealStatus() == Report.STATUS_REJECTED)
            {
                statusTextView.setText(R.string.rejected);
                statusTextView.setTextColor(ViewUtils.getColor(R.color.status_rejected));

                alarmImageView.setVisibility(View.GONE);
                timeLayout.setVisibility(View.VISIBLE);

                TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
                timeTextView.setText(info.getApproveTime());

                TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                dateTextView.setText(info.getApproveDate());
            }
            else
            {
                statusTextView.setText(R.string.ready_to_approved);
                statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

                alarmImageView.setVisibility(View.GONE);
                timeLayout.setVisibility(View.GONE);
            }
        }
        else
        {
            statusTextView.setText(R.string.ready_to_approved);
            statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

            timeLayout.setVisibility(View.GONE);

            int visibility = user.equals(currentUser) ? View.GONE : View.VISIBLE;
            alarmImageView.setVisibility(visibility);
            alarmImageView.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    if (report.getStatus() == Report.STATUS_SUBMITTED)
                    {
                        ReimProgressDialog.show();
                        AlertRequest request = new AlertRequest(user.getServerID(), report.getServerID());
                        request.sendRequest(new HttpConnectionCallback()
                        {
                            public void execute(Object httpResponse)
                            {
                                final AlertResponse response = new AlertResponse(httpResponse);
                                activity.runOnUiThread(new Runnable()
                                {
                                    public void run()
                                    {
                                        ReimProgressDialog.dismiss();
                                        if (response.getStatus())
                                        {
                                            ViewUtils.showToast(activity, R.string.succeed_in_alerting);
                                        }
                                        else
                                        {
                                            ViewUtils.showToast(activity, R.string.failed_to_alert, response.getErrorMessage());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        ViewUtils.showToast(activity, R.string.prompt_no_need_to_alarm);
                    }
                }
            });

            int alarmImage = report.getStatus() == Report.STATUS_SUBMITTED ? R.drawable.alarm_enabled_drawable : R.drawable.alarm_disabled_drawable;
            alarmImageView.setImageResource(alarmImage);
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
		initStepList();
	}	

	private void initStepList()
	{
		stepStartList.clear();
		
		int step = -1;
		for (int i = 0; i < infoList.size(); i++)
		{
			ApproveInfo approveInfo = infoList.get(i);
			if (approveInfo.getStep() != step)
			{
				stepStartList.add(i);
				step = approveInfo.getStep();
			}
		}
	}
}
