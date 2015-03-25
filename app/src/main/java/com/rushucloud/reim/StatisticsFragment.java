package com.rushucloud.reim;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;

import classes.Category;
import classes.StatCategory;
import classes.adapter.StatisticsListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimMonthBar;
import classes.widget.ReimPie;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;
import netUtils.HttpConnectionCallback;
import netUtils.request.statistics.MineStatRequest;
import netUtils.response.statistics.MineStatResponse;

public class StatisticsFragment extends Fragment
{
	private static final int GET_DATA_INTERVAL = 600;
	
	private XListView statListView;
	private StatisticsListViewAdapter adapter;
	private View view;
	private FrameLayout statContainer;
	private TextView mainAmountTextView;
	private TextView unitTextView;
	private TextView ongoingPercentTextView;
	private TextView newPercentTextView;
	private TextView totalTextView;
	private TextView totalUnitTextView;
	private LinearLayout monthLayout;
	private LinearLayout categoryLayout;

	private AppPreference appPreference;
	private DBManager dbManager;

	private boolean hasInit = false;
	private boolean hasData = false;
	
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
		view = View.inflate(getActivity(), R.layout.view_statistics, null);
		
		statContainer = (FrameLayout) view.findViewById(R.id.statContainer);
		
		mainAmountTextView = (TextView) view.findViewById(R.id.mainAmountTextView);
		mainAmountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		unitTextView = (TextView) view.findViewById(R.id.unitTextView);

        newPercentTextView = (TextView) view.findViewById(R.id.newPercentTextView);
        newPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
		
		ongoingPercentTextView = (TextView) view.findViewById(R.id.ongoingPercentTextView);
		ongoingPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

		totalTextView = (TextView) view.findViewById(R.id.totalTextView);
		totalUnitTextView = (TextView) view.findViewById(R.id.totalUnitTextView);
		monthLayout = (LinearLayout) view.findViewById(R.id.monthLayout);
		categoryLayout = (LinearLayout) view.findViewById(R.id.categoryLayout);
		
		adapter = new StatisticsListViewAdapter(getActivity(), view);
		statListView = (XListView) getActivity().findViewById(R.id.statListView);
		statListView.setAdapter(adapter);
		statListView.setXListViewListener(new IXListViewListener()
		{
			public void onRefresh()
			{
				getData();
			}
			
			public void onLoadMore()
			{
				
			}
		});
		statListView.setPullRefreshEnable(true);
		statListView.setPullLoadEnable(false);
		statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetStatTime()));
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
			if (needToGetData())
			{
				ReimProgressDialog.show();
			}
			sendGetDataRequest();
		}
		else
		{
			statListView.stopRefresh();
			ViewUtils.showToast(getActivity(), R.string.error_get_data_network_unavailable);
		}		
	}
	
	private void drawPie(double ongoingAmount, double newAmount)
	{
        double totalAmount = ongoingAmount + newAmount;
		double ongoingRatio, newRatio;
		if (totalAmount == 0)
		{
			ongoingRatio = newRatio = 0;
		}
		else
		{
			ongoingRatio = Utils.roundDouble(ongoingAmount * 100 / totalAmount);
			newRatio = 100 - ongoingRatio;
		}
		
		if (totalAmount < 10000)
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount));
			unitTextView.setVisibility(View.GONE);
		}
		else if (totalAmount < 10000000)
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount / 10000));
			unitTextView.setText(R.string.ten_thousand);
		}
		else
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount / 100000000));
			unitTextView.setText(R.string.one_hundred_million);			
		}
		ongoingPercentTextView.setText(Utils.formatDouble(ongoingRatio) + getString(R.string.percent));
		newPercentTextView.setText(Utils.formatDouble(newRatio) + getString(R.string.percent));

		float totalAngle = 262;
		float startAngle = 139;
        float ongoingAngle = (float) ongoingRatio * totalAngle / 100;
		float newAngle = (float) newRatio * totalAngle / 100;

        // Draw new pie
        ReimPie newReimPie = new ReimPie(getActivity(), startAngle, newAngle, statContainer.getWidth(), R.color.stat_new);
        statContainer.addView(newReimPie);

		// Draw ongoing pie
        startAngle += newAngle;
		ReimPie ongoingReimPie = new ReimPie(getActivity(), startAngle, ongoingAngle, statContainer.getWidth(), R.color.stat_ongoing);
		statContainer.addView(ongoingReimPie);
	}

	private void drawMonthBar(HashMap<String, Double> monthsData)
	{
		if (!monthsData.isEmpty())
		{
			double total = 0;
			double max = 0;
			for (Double data : monthsData.values())
			{
				total += data;
				if (data > max)
				{
					max = data;
				}
			}
			totalTextView.setText(Double.toString(total));
			
			for (String month : monthsData.keySet())
			{
				Double data = monthsData.get(month);
				ReimMonthBar monthBar = new ReimMonthBar(getActivity(), data / max);
				
				View view = View.inflate(getActivity(), R.layout.list_month_stat, null);
				
				TextView monthTextView = (TextView) view.findViewById(R.id.monthTextView);
				monthTextView.setText(month);

				TextView dataTextView = (TextView) view.findViewById(R.id.dataTextView);
				TextView unitTextView = (TextView) view.findViewById(R.id.unitTextView);
				
				if (data < 100000)
				{
					dataTextView.setText(Utils.formatDouble(data));
					unitTextView.setVisibility(View.GONE);
				}
				else if (data < 100000000)
				{
					dataTextView.setText(Utils.formatDouble(data / 10000));
					unitTextView.setText(R.string.ten_thousand);
				}
				else
				{
					dataTextView.setText(Utils.formatDouble(data / 100000000));
					unitTextView.setText(R.string.one_hundred_million);			
				}
				
				LinearLayout dataLayout = (LinearLayout) view.findViewById(R.id.dataLayout);
				dataLayout.addView(monthBar);
				
				monthLayout.addView(view);
			}
		}
		else
		{
			totalTextView.setVisibility(View.INVISIBLE);
			totalUnitTextView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void drawCategory(List<StatCategory> categoryList)
	{
		if (!categoryList.isEmpty())
		{			
			for (StatCategory category : categoryList)
			{
				Category localCategory = dbManager.getCategory(category.getCategoryID());
				if (localCategory != null)
				{
					View view = View.inflate(getActivity(), R.layout.list_category_stat, null);

                    ImageView iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
                    ViewUtils.setImageViewBitmap(localCategory, iconImageView);

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
		MineStatRequest request = new MineStatRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final MineStatResponse response = new MineStatResponse(httpResponse);
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
							drawPie(response.getOngoingAmount(), response.getNewAmount());
							drawMonthBar(response.getMonthsData());
							drawCategory(response.getStatCategoryList());
							adapter.notifyDataSetChanged();
							statListView.stopRefresh();
							statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetStatTime()));
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
							statListView.stopRefresh();
							ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
						}
					});					
				}
			}
		});
	}
}