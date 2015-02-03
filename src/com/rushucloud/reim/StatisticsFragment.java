package com.rushucloud.reim;

import java.util.HashMap;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Response.StatisticsResponse;
import netUtils.Request.StatisticsRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import classes.Category;
import classes.ReimApplication;
import classes.StatisticsCategory;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimMonthBar;
import classes.widget.ReimPie;
import classes.widget.ReimProgressDialog;

import com.umeng.analytics.MobclickAgent;


public class StatisticsFragment extends Fragment
{
	private static final int GET_DATA_INTERVAL = 600;
	
	private FrameLayout statContainer;
	private TextView mainPercentTextView;
	private TextView donePercentTextView;
	private TextView ongoingPercentTextView;
	private TextView newPercentTextView;
	private TextView monthCostTextView;
	private LinearLayout monthLayout;
	private RelativeLayout categoryTitleLayout;
	private LinearLayout categoryLayout;

	private AppPreference appPreference;
	private DBManager dbManager;

	private boolean hasInit = false;
	private boolean hasData = false;
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
			appPreference = AppPreference.getAppPreference();
			dbManager = DBManager.getDBManager();
			initView();
			hasInit = true;
		}
		
		if (getUserVisibleHint() && needToGetData())
		{
			getData();			
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
		if (isVisibleToUser && hasInit && needToGetData())
		{
			getData();
		}
	}
	
	private void initView()
	{		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arc);
		double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int margin = PhoneUtils.dpToPixel(getResources(), 36);
		
		diameter = metrics.widthPixels - margin * 2;
		ImageView arcImageView = (ImageView) getActivity().findViewById(R.id.arcImageView);
		ViewGroup.LayoutParams params = arcImageView.getLayoutParams();
		params.width = diameter;
		params.height = (int)(params.width * ratio);
		arcImageView.setLayoutParams(params);
		
		ImageView arcCoverImageView = (ImageView) getActivity().findViewById(R.id.arcCoverImageView);
		arcCoverImageView.setLayoutParams(params);
		
		statContainer = (FrameLayout) getActivity().findViewById(R.id.statContainer);
		statContainer.setLayoutParams(params);
		
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

		categoryTitleLayout = (RelativeLayout) getActivity().findViewById(R.id.categoryTitleLayout);
		categoryLayout = (LinearLayout) getActivity().findViewById(R.id.categoryLayout);
	}

	private void resetView()
	{
		statContainer.removeAllViews();
		monthLayout.removeAllViews();
		categoryLayout.removeAllViews();
	}

	private boolean needToGetData()
	{
		return !hasData || Utils.getCurrentTime() - appPreference.getLastGetStatTime() > GET_DATA_INTERVAL;
	}
	
	private void getData()
	{		
		if (PhoneUtils.isNetworkConnected())
		{
			sendGetDataRequest();			
		}
		else
		{
			ViewUtils.showToast(getActivity(), R.string.error_get_data_network_unavailable);
		}		
	}
	
	private void drawPie(double totalAmount, double doneAmount, double ongoingAmount, double newAmount)
	{
		double doneRatio, ongoingRatio, newRatio, mainRatio;
		if (totalAmount == 0)
		{
			doneRatio = ongoingRatio = newRatio = mainRatio = 0;
		}
		else
		{			
			doneRatio = Utils.roundDouble(doneAmount * 100 / totalAmount);
			ongoingRatio = Utils.roundDouble(ongoingAmount * 100 / totalAmount);
			newRatio = Utils.roundDouble(newAmount * 100 / totalAmount);
			
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
		startAngle += doneAngle;
		ReimPie ongoingReimPie = new ReimPie(getActivity(), startAngle, ongoingAngle, statContainer.getWidth(), R.color.stat_ongoing);
		statContainer.addView(ongoingReimPie);	

		// Draw new pie
		startAngle += ongoingAngle;
		ReimPie newReimPie = new ReimPie(getActivity(), startAngle, newAngle, statContainer.getWidth(), R.color.stat_new);
		statContainer.addView(newReimPie);
	}

	private void drawMonthBar(HashMap<String, Double> monthsData)
	{
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
	
	private void drawCategory(List<StatisticsCategory> categoryList)
	{
		if (categoryList.isEmpty())
		{
			categoryTitleLayout.setVisibility(View.GONE);
			categoryLayout.setVisibility(View.GONE);
		}
		else
		{
			categoryTitleLayout.setVisibility(View.VISIBLE);
			categoryLayout.setVisibility(View.VISIBLE);
			
			for (StatisticsCategory category : categoryList)
			{				
				Category localCategory = dbManager.getCategory(category.getCategoryID());
				if (localCategory != null)
				{
					View view = View.inflate(getActivity(), R.layout.list_category_stat, null);
					
					TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
					titleTextView.setText(localCategory.getName());
					
					TextView countTextView = (TextView) view.findViewById(R.id.countTextView);
					countTextView.setText(Integer.toString(category.getItems().size()));
					
					TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
					amountTextView.setText(Utils.formatDouble(category.getAmount()));
					
					categoryLayout.addView(view);					
				}
			}
		}		
	}

	private void sendGetDataRequest()
	{
		ReimProgressDialog.show();
		StatisticsRequest request = new StatisticsRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final StatisticsResponse response = new StatisticsResponse(httpResponse);
				if (response.getStatus())
				{
					hasData = true;

					appPreference.setLastGetStatTime(Utils.getCurrentTime());
					appPreference.saveAppPreference();
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							resetView();
							drawPie(response.getTotal(), response.getDoneAmount(), response.getOngoingAmount(), response.getNewAmount());
							drawMonthBar(response.getMonthsData());
							drawCategory(response.getStatCategoryList());
							ReimProgressDialog.dismiss();
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
						}
					});					
				}
			}
		});
	}
}