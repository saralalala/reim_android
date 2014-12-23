package com.rushucloud.reim;

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
import android.widget.TextView;
import android.support.v4.app.Fragment;

import classes.ReimApplication;
import classes.Utils.Utils;
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
	
	private StatisticsResponse response = null;
	
	private ReimPie doneReimPie;
	private ReimPie newReimPie;
	private ReimPie ongoingReimPie;
//	private GraphicalView monthBarChart;
	
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
		if (doneReimPie == null)
		{
			doneReimPie = new ReimPie(getActivity(), startAngle, doneAngle, statContainer.getWidth(), R.color.stat_done);
		} 
		else
		{
			doneReimPie.setPieRect(startAngle, doneAngle, statContainer.getWidth());
		}
		statContainer.addView(doneReimPie);	

		// Draw ongoing pie
		if (ongoingReimPie == null)
		{
			ongoingReimPie = new ReimPie(getActivity(), startAngle + doneAngle, ongoingAngle, statContainer.getWidth(), R.color.stat_ongoing);
		} 
		else
		{
			ongoingReimPie.setPieRect(startAngle + doneAngle, ongoingAngle, statContainer.getWidth());
		}
		statContainer.addView(ongoingReimPie);	

		// Draw new pie
		if (newReimPie == null)
		{
			newReimPie = new ReimPie(getActivity(), startAngle + doneAngle + ongoingAngle, newAngle, statContainer.getWidth(), R.color.stat_new);
		} 
		else
		{
			newReimPie.setPieRect(startAngle + doneAngle + ongoingAngle, newAngle, statContainer.getWidth());
		}
		statContainer.addView(newReimPie);
	}

	private void drawBar()
	{
//		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
//
//		renderer.setChartTitle("月度详细");
//		renderer.setXAxisMin(0);
//		renderer.setXAxisMax(12);
//		renderer.setXLabels(0);
//
//		renderer.setYAxisMin(0);
//		renderer.setYAxisMax(1000);
//		renderer.setYLabels(15);
//
//		renderer.setAxisTitleTextSize(18);
//		renderer.setDisplayValues(true);
//		renderer.setShowGrid(true);
//		renderer.setMarginsColor(getResources().getColor(R.color.background));
//		renderer.setLabelsColor(Color.BLACK);
//		renderer.setXLabelsColor(Color.BLACK);
//		renderer.setYLabelsColor(0, Color.BLACK);
//		renderer.setYLabelsAlign(Align.CENTER);
//		renderer.setBarSpacing(0.5f);
//		renderer.setPanEnabled(false, false);
//		renderer.setLabelsTextSize(16);
//
//		HashMap<String, String> monthsData = response.getMonthsData();
//		
//		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
//		if (monthsData.size() == 0)
//		{			
//			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
//			r.setColor(Color.rgb(50, 143, 201));
//			renderer.addSeriesRenderer(r);
//			
//			String timeString = Utils.secondToStringUpToDay(Utils.getCurrentTime());
//			String currentMonth = timeString.substring(0, timeString.length()-3);
//			XYSeries series = new XYSeries(currentMonth);
//			series.add(1, 0);
//			renderer.addXTextLabel(1, currentMonth);
//			dataSet.addSeries(series);
//		}
//		else
//		{
//			Set<String> keys = monthsData.keySet();
//			int count = keys.size();
//			for (int i = 0; i < count; i++)
//			{				
//				String key = (String) ((keys.toArray())[i]);
//				
//				SimpleSeriesRenderer r = new SimpleSeriesRenderer();
//				r.setColor(Color.rgb(50, 143, 201));
//				renderer.addSeriesRenderer(r);
//				renderer.addXTextLabel(i + 1, key);
//				
//				XYSeries series = new XYSeries(key);
//				Long value = Long.parseLong(monthsData.get(key));
//				series.add(i + 1, value);
//				dataSet.addSeries(series);
//			}
//		}
//		
//		monthBarChart = ChartFactory.getBarChartView(getActivity(), dataSet, renderer, Type.STACKED);
//		monthBarContainer.addView(monthBarChart);
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
							drawBar();
							drawCategory();
							ReimApplication.dismissProgressDialog();
						}
					});
				}
			}
		});
	}
}
