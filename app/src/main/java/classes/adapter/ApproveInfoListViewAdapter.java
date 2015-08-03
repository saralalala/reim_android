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

import classes.model.ApproveInfo;
import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.report.AlertRequest;
import netUtils.response.report.AlertResponse;

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
        this.infoList = new ArrayList<>(infos);
        this.stepStartList = new ArrayList<>();
        initStepList();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_approve_info, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.pointImageView = (CircleImageView) convertView.findViewById(R.id.pointImageView);
            viewHolder.upperLayout = (LinearLayout) convertView.findViewById(R.id.upperLayout);
            viewHolder.lowerLayout = (LinearLayout) convertView.findViewById(R.id.lowerLayout);
            viewHolder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
            viewHolder.nicknameTextView = (TextView) convertView.findViewById(R.id.nicknameTextView);
            viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
            viewHolder.timeLayout = (LinearLayout) convertView.findViewById(R.id.timeLayout);
            viewHolder.alarmImageView = (ImageView) convertView.findViewById(R.id.alarmImageView);
            viewHolder.divider = (LinearLayout) convertView.findViewById(R.id.divider);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ApproveInfo info = getItem(position);

        // set point and line
        if (position == 0)
        {
            viewHolder.pointImageView.setVisibility(View.VISIBLE);
            viewHolder.upperLayout.setVisibility(View.GONE);
            viewHolder.lowerLayout.setVisibility(View.VISIBLE);

            int point = info.hasApproved() ? R.drawable.point_approved : R.drawable.point_not_approved;
            viewHolder.pointImageView.setImageResource(point);

            int color = info.hasApproved() ? R.color.status_approved : R.color.background_grey;
            viewHolder.lowerLayout.setBackgroundColor(ViewUtils.getColor(color));
        }
        else
        {
            if (stepStartList.contains(position))
            {
                viewHolder.pointImageView.setVisibility(View.VISIBLE);
                int point = info.hasApproved() ? R.drawable.point_approved : R.drawable.point_not_approved;
                viewHolder.pointImageView.setImageResource(point);
            }
            else
            {
                viewHolder.pointImageView.setVisibility(View.GONE);
            }

            viewHolder.upperLayout.setVisibility(View.VISIBLE);
            viewHolder.lowerLayout.setVisibility(View.VISIBLE);

            ApproveInfo previousInfo = getItem(position - 1);
            int color = previousInfo.hasApproved() ? R.color.status_approved : R.color.background_grey;
            viewHolder.upperLayout.setBackgroundColor(ViewUtils.getColor(color));

            color = info.hasApproved() ? R.color.status_approved : R.color.background_grey;
            viewHolder.lowerLayout.setBackgroundColor(ViewUtils.getColor(color));
        }

        if (position == infoList.size() - 1)
        {
            viewHolder.lowerLayout.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.divider.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
        }
        else
        {
            ApproveInfo nextInfo = infoList.get(position + 1);
            int margin = info.getStep() != nextInfo.getStep() ? 0 : 64;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.divider.getLayoutParams();
            params.setMargins(ViewUtils.dpToPixel(margin), 0, 0, 0);
        }

        viewHolder.nicknameTextView.setText(R.string.not_available);

        // set avatar and nickname
        final User user = dbManager.getUser(info.getUserID());
        if (user != null)
        {
            ViewUtils.setImageViewBitmap(user, viewHolder.avatarImageView);

            String nickname = user.getNickname();
            if (info.getProxyUserID() > 0)
            {
                User proxyUser = dbManager.getUser(info.getProxyUserID());
                if (proxyUser != null)
                {
                    nickname += String.format(ViewUtils.getString(R.string.proxy_approved), proxyUser.getNickname());
                }
            }
            viewHolder.nicknameTextView.setText(nickname);
        }
        else
        {
            viewHolder.statusTextView.setText(R.string.not_available);
            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.GONE);

            return convertView;
        }

        // set status, time and alarm
        if (info.isRevoked()) // revoked report
        {
            viewHolder.statusTextView.setText(R.string.revoked);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.VISIBLE);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            timeTextView.setText(info.getApproveTime());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            dateTextView.setText(info.getApproveDate());
        }
        else if (info.getUserID() == info.getReportSenderID() && info.hasApproved()) // submitted report (step > 100)
        {
            viewHolder.statusTextView.setText(R.string.submitted);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.VISIBLE);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            timeTextView.setText(info.getApproveTime());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            dateTextView.setText(info.getApproveDate());
        }
        else if (info.getUserID() == info.getReportSenderID()) // draft report (step < 100)
        {
            viewHolder.statusTextView.setText(R.string.ready_to_submitted);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.GONE);
        }
        else if (info.hasApproved() && info.getRealStatus() == Report.STATUS_APPROVED) // approved report (step > 100)
        {
            viewHolder.statusTextView.setText(R.string.approved);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.status_approved));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.VISIBLE);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            timeTextView.setText(info.getApproveTime());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            dateTextView.setText(info.getApproveDate());
        }
        else if (info.hasApproved() && info.getRealStatus() == Report.STATUS_REJECTED) // rejected report (step > 100)
        {
            viewHolder.statusTextView.setText(R.string.rejected);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.status_rejected));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.VISIBLE);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            timeTextView.setText(info.getApproveTime());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            dateTextView.setText(info.getApproveDate());
        }
        else if (info.hasApproved()) // report needs to be approved but already not available (step > 100, multiple approve)
        {
            viewHolder.statusTextView.setText(R.string.ready_to_approve);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

            viewHolder.alarmImageView.setVisibility(View.GONE);
            viewHolder.timeLayout.setVisibility(View.GONE);
        }
        else  // report needs to be approved (step < 100)
        {
            viewHolder.statusTextView.setText(R.string.ready_to_approve);
            viewHolder.statusTextView.setTextColor(ViewUtils.getColor(R.color.major_dark));

            viewHolder.timeLayout.setVisibility(View.GONE);

            int visibility = user.equals(currentUser) ? View.GONE : View.VISIBLE;
            viewHolder.alarmImageView.setVisibility(visibility);
            viewHolder.alarmImageView.setOnClickListener(new OnClickListener()
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
            viewHolder.alarmImageView.setImageResource(alarmImage);
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

    private static class ViewHolder
    {
        CircleImageView pointImageView;
        LinearLayout upperLayout;
        LinearLayout lowerLayout;
        CircleImageView avatarImageView;
        TextView nicknameTextView;
        TextView statusTextView;
        LinearLayout timeLayout;
        ImageView alarmImageView;
        LinearLayout divider;
    }
}
