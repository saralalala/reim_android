package com.rushucloud.reim;

import java.util.HashMap;
import netUtils.HttpConnectionCallback;
import netUtils.Request.StatisticsRequest;
import netUtils.Response.StatisticsResponse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import classes.ReimApplication;
import classes.Utils.Utils;
import classes.Widget.ReimMonthBar;
import classes.Widget.ReimPie;

import com.umeng.analytics.MobclickAgent;

public class StatisticsFragment extends Fragment
{
	private boolean hasInit = false;

	private FrameLayout statContainer;
	private TextView mainPercentTextView;
	private TextView donePercentTextView;
	private TextView ongoingPercentTextView;
	private TextView newPercentTextView;
	private TextView monthCostTextView;
	private LinearLayout monthLayout;
	
	private StatisticsResponse response = null;
	
	private int diameter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_statistics, container, false);
	}
	
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("StatisticsFragment");
		if (!hasInit)
		{
			initView();
			hasInit = true;
			initData();
		}
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("StatisticsFragment");
	}

	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && hasInit)
		{
			initData();
		}
	}
	
	private void initView()
	{
		statContainer = (FrameLayout) getActivity().findViewById(R.id.statContainer);
		
		mainPercentTextView = (TextView) getActivity().findViewById(R.id.mainPercentTextView);
		mainPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		donePercentTextView = (TextView) getActivity().findViewById(R.id.donePercentTextView);
		donePercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		ongoingPercentTextView = (TextView) getActivity().findViewById(R.id.ongoingPercentTextView);
		ongoingPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		newPercentTextView = (TextView) getActivity().findViewById(R.id.newPercentTextView);
		newPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

		monthCostTextView = (TextView) getActivity().findViewById(R.id.monthCostTextView);
		monthLayout = (LinearLayout) getActivity().findViewById(R.id.monthLayout);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arc);
		double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, metrics);
		
		diameter = metrics.widthPixels - margin * 2;
		ImageView arcImageView = (ImageView) getActivity().findViewById(R.id.arcImageView);
		ViewGroup.LayoutParams params = arcImageView.getLayoutParams();
		params.width = diameter;
		params.height = (int)(params.width * ratio);
		arcImageView.setLayoutParams(params);
		
		ImageView arcCoverImageView = (ImageView) getActivity().findViewById(R.id.arcCoverImageView);
		arcCoverImageView.setLayoutParams(params);
		
		FrameLayout statContainer = (FrameLayout) getActivity().findViewById(R.id.statContainer);
		statContainer.setLayoutParams(params);
	}

	private void resetView()
	{
		statContainer.removeAllViews();
		monthLayout.removeAllViews();
	}
	
	private void initData()
	{
		if (Utils.isNetworkConnected())
		{
			sendGetDataRequest();			
		}
		else
		{
			Utils.showToast(getActivity(), "网络未连接，无法获取数据");
		}		
	}
	
	private void drawPie()
	{
		double doneRatio, ongoingRatio, newRatio, mainRatio;
		double total = response.getTotal();
		if (total == 0)
		{
			doneRatio = ongoingRatio = newRatio = mainRatio = 0;
		}
		else
		{			
			doneRatio = Utils.roundDouble(response.getDoneAmount() * 100 / total);
			ongoingRatio = Utils.roundDouble(response.getOngoingAmount() * 100 / total);
			newRatio = Utils.roundDouble(response.getNewAmount() * 100 / total);
			
			if (doneRatio >= ongoingRatio && doneRatio >= newRatio)
			{
				doneRatio = 100 - ongoingRatio - newRatio;
				doneRatio = Utils.roundDouble(doneRatio);
			}
			else if (ongoingRatio >= doneRatio && ongoingRatio >= newRatio)
			{
				ongoingRatio = 100 - doneRatio - newRatio;
				ongoingRatio = Utils.roundDouble(ongoingRatio);
			}
			else if (newRatio >= ongoingRatio && newRatio >= doneRatio)
			{
				newRatio = 100 - doneRatio - ongoingRatio;
				newRatio = Utils.roundDouble(newRatio);
			}
			
			mainRatio = ongoingRatio + newRatio;
		}
		
		mainPercentTextView.setText(Double.toString(mainRatio));
		donePercentTextView.setText(Double.toString(doneRatio) + getString(R.string.percent));
		ongoingPercentTextView.setText(Double.toString(ongoingRatio) + getString(R.string.percent));
		newPercentTextView.setText(Double.toString(newRatio) + getString(R.string.percent));

		float totalAngle = 262;
		float startAngle = (float) 139;
		float doneAngle = (float) doneRatio * totalAngle / 100;
		float newAngle = (float) newRatio * totalAngle / 100;
		float ongoingAngle = (float) ongoingRatio * totalAngle / 100;
		
		// Draw done pie
		ReimPie doneReimPie = new ReimPie(getActivity(), startAngle, doneAngle, statContainer.getWidth(), R.color.stat_done);
		statContainer.addView(doneReimPie);	

		// Draw ongoing pie
		ReimPie ongoingReimPie = new ReimPie(getActivity(), startAngle + doneAngle, ongoingAngle, statContainer.getWidth(), R.color.stat_ongoing);
		statContainer.addView(ongoingReimPie);	

		// Draw new pie
		ReimPie newReimPie = new ReimPie(getActivity(), startAngle + doneAngle + ongoingAngle, newAngle, statContainer.getWidth(), R.color.stat_new);
		statContainer.addView(newReimPie);
	}

	private void drawMonthBar()
	{
		HashMap<String, Double> monthsData = response.getMonthsData();
		if (monthsData.isEmpty())
		{
			monthCostTextView.setVisibility(View.GONE);
			monthLayout.setVisibility(View.GONE);
		}
		else
		{
			monthCostTextView.setVisibility(View.VISIBLE);
			monthLayout.setVisibility(View.VISIBLE);
			
			double max = 0;
			for (Double data : monthsData.values())
			{
				if (data > max)
				{
					max = data;
				}
			}
			
			for (String month : monthsData.keySet())
			{
				Double data = monthsData.get(month);
				ReimMonthBar monthBar = new ReimMonthBar(getActivity(), data / max);
				
				View view = View.inflate(getActivity(), R.layout.list_month_stat, null);
				
				TextView monthTextView = (TextView) view.findViewById(R.id.monthTextView);
				monthTextView.setText(month);
				
				TextView dataTextView = (TextView) view.findViewById(R.id.dataTextView);
				dataTextView.setText(Utils.formatDouble(data));
				
				LinearLayout dataLayout = (LinearLayout) view.findViewById(R.id.dataLayout);
				dataLayout.addView(monthBar);
				
				monthLayout.addView(view);
			}
		}
	}
	
	private void drawCategory()
	{
		
	}

	private void sendGetDataRequest()
	{
		ReimApplication.showProgressDialog();
		StatisticsRequest request = new StatisticsRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				response = new StatisticsResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							resetView();
							drawPie();
							drawMonthBar();
							drawCategory();
							ReimApplication.dismissProgressDialog();
						}
					});
				}
			}
		});
	}
}
