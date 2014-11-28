package com.rushucloud.reim;

import java.util.HashMap;
import java.util.Set;

import netUtils.HttpConnectionCallback;
import netUtils.Request.StatisticsRequest;
import netUtils.Response.StatisticsResponse;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;

import org.achartengine.*;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;

import org.achartengine.renderer.XYMultipleSeriesRenderer;

import classes.ReimApplication;
import classes.Utils;
import classes.Widget.ReimPie;

import com.umeng.analytics.MobclickAgent;

public class StatisticsFragment extends Fragment
{
	private boolean hasInit = false;

	private LinearLayout newPieContainer;
	private LinearLayout donePieContainer;
	private LinearLayout processPieContainer;
	private LinearLayout monthBarContainer;
	
	private StatisticsResponse response = null;
	
	private ReimPie doneReimPie;
	private ReimPie newReimPie;
	private ReimPie processReimPie;
	private GraphicalView monthBarChart;
	
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
		newPieContainer = (LinearLayout) getActivity().findViewById(R.id.newPieContainer);
		donePieContainer = (LinearLayout) getActivity().findViewById(R.id.donePieContainer);
		processPieContainer = (LinearLayout) getActivity().findViewById(R.id.processPieContainer);
		monthBarContainer = (LinearLayout) getActivity().findViewById(R.id.monthBarContainer);
	}

	private void resetView()
	{
		newPieContainer.removeAllViews();
		donePieContainer.removeAllViews();
		processPieContainer.removeAllViews();
		monthBarContainer.removeAllViews();
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
		double total = response.getTotal();

		float doneAngle = (float) (response.getDoneAmount() * 360 / total);
		float newAngle = (float) (response.getNewAmount() * 360 / total);
		float processAngle = (float) (response.getProcessAmount() * 360 / total);
		
		// Draw done pie
		if (doneAngle == 0 || Float.isNaN(doneAngle))
		{
			if (doneReimPie == null)
			{
				doneReimPie = new ReimPie(getActivity(), 0, 0, donePieContainer.getWidth(), donePieContainer.getHeight());
			} 
			else
			{
				doneReimPie.setPieRect(0, 0, donePieContainer.getWidth(), donePieContainer.getHeight());
			}
		}
		else 
		{
			if (doneReimPie == null)
			{
				doneReimPie = new ReimPie(getActivity(), doneAngle, 0, donePieContainer.getWidth(), donePieContainer.getHeight());
			} 
			else
			{
				doneReimPie.setPieRect(doneAngle, 0, donePieContainer.getWidth(), donePieContainer.getHeight());
			}		
		}
		donePieContainer.addView(doneReimPie);	

		// Draw new pie
		if (newAngle == 0 || Float.isNaN(newAngle))
		{
			if (newReimPie == null)
			{
				newReimPie = new ReimPie(getActivity(), 0, 0, newPieContainer.getWidth(), newPieContainer.getHeight());
			} 
			else
			{
				newReimPie.setPieRect(0, 0, newPieContainer.getWidth(), newPieContainer.getHeight());
			}
		}
		else 
		{
			if (newReimPie == null)
			{
				newReimPie = new ReimPie(getActivity(), newAngle, doneAngle, newPieContainer.getWidth(), newPieContainer.getHeight());
			} 
			else
			{
				newReimPie.setPieRect(newAngle, doneAngle, newPieContainer.getWidth(), newPieContainer.getHeight());
			}
		}
		newPieContainer.addView(newReimPie);

		// Draw process pie
		if (processAngle == 0 || Float.isNaN(processAngle))
		{
			if (processReimPie == null)
			{
				processReimPie = new ReimPie(getActivity(), 0, 0, processPieContainer.getWidth(), processPieContainer.getHeight());
			} 
			else
			{
				processReimPie.setPieRect(0, 0, processPieContainer.getWidth(), processPieContainer.getHeight());
			}
		}
		else 
		{
			if (processReimPie == null)
			{
				processReimPie = new ReimPie(getActivity(), processAngle, newAngle + doneAngle, processPieContainer.getWidth(), processPieContainer.getHeight());
			} 
			else
			{
				processReimPie.setPieRect(processAngle, newAngle + doneAngle, processPieContainer.getWidth(), processPieContainer.getHeight());
			}
		}
		processPieContainer.addView(processReimPie);	
	}

	private void drawBar()
	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		renderer.setChartTitle("月度详细");
		renderer.setXAxisMin(0);
		renderer.setXAxisMax(12);
		renderer.setXLabels(0);

		renderer.setYAxisMin(0);
		renderer.setYAxisMax(1000);
		renderer.setYLabels(15);

		renderer.setAxisTitleTextSize(18);
		renderer.setDisplayValues(true);
		renderer.setShowGrid(true);
		renderer.setMarginsColor(getResources().getColor(R.color.list_background));
		renderer.setLabelsColor(Color.BLACK);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setYLabelsAlign(Align.CENTER);
		renderer.setBarSpacing(0.5f);
		renderer.setPanEnabled(false, false);
		renderer.setLabelsTextSize(16);

		HashMap<String, String> monthsData = response.getMonthsData();
		
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
		if (monthsData.size() == 0)
		{			
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(Color.rgb(50, 143, 201));
			renderer.addSeriesRenderer(r);
			
			String timeString = Utils.secondToStringUpToDay(Utils.getCurrentTime());
			String currentMonth = timeString.substring(0, timeString.length()-3);
			XYSeries series = new XYSeries(currentMonth);
			series.add(1, 0);
			renderer.addXTextLabel(1, currentMonth);
			dataSet.addSeries(series);
		}
		else
		{
			Set<String> keys = monthsData.keySet();
			int count = keys.size();
			for (int i = 0; i < count; i++)
			{				
				String key = (String) ((keys.toArray())[i]);
				
				SimpleSeriesRenderer r = new SimpleSeriesRenderer();
				r.setColor(Color.rgb(50, 143, 201));
				renderer.addSeriesRenderer(r);
				renderer.addXTextLabel(i + 1, key);
				
				XYSeries series = new XYSeries(key);
				Long value = Long.parseLong(monthsData.get(key));
				series.add(i + 1, value);
				dataSet.addSeries(series);
			}
		}
		
		monthBarChart = ChartFactory.getBarChartView(getActivity(), dataSet, renderer, Type.STACKED);
		monthBarContainer.addView(monthBarChart);
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
							ReimApplication.dismissProgressDialog();
						}
					});
				}
			}
		});
	}
}
