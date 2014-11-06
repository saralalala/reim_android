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
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;

import org.achartengine.renderer.XYMultipleSeriesRenderer;

import classes.ReimApplication;
import classes.Utils;

import com.rushucloud.graphics.ReimPie;
import com.umeng.analytics.MobclickAgent;

public class StatisticsFragment extends Fragment
{
	private StatisticsResponse response = null;
	
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("StatisticsFragment");
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("StatisticsFragment");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_statistics, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		initData();
	}

	protected CategorySeries buildCategoryDataset(String[] titles, double[] values)
	{
		CategorySeries series = new CategorySeries("test");
		int k = -1;
		for (double value : values)
		{
			series.add(titles[++k], value);
		}
		return series;
	}

	private void drawPie()
	{
		double total = response.getTotal();

		LinearLayout newContainer = (LinearLayout) getActivity().findViewById(R.id.new_container);
		LinearLayout doneContainer = (LinearLayout) getActivity().findViewById(R.id.done_container);
		LinearLayout processContainer = (LinearLayout) getActivity().findViewById(R.id.process_container);

		float doneAngle = (float) (response.getDoneAmount() * 360 / total);
		float newAngle = (float) (response.getNewAmount() * 360 / total);
		float processAngle = (float) (response.getProcessAmount() * 360 / total);
		
		if (doneAngle == 0 || Float.isNaN(doneAngle))
		{
			ReimPie doneReimPie = new ReimPie(getActivity(), 0, 0, doneContainer.getWidth(), doneContainer.getHeight());
			doneContainer.addView(doneReimPie);			
		}
		else 
		{
			ReimPie doneReimPie = new ReimPie(getActivity(), doneAngle, 0, doneContainer.getWidth(), doneContainer.getHeight());
			doneContainer.addView(doneReimPie);			
		}

		if (newAngle == 0 || Float.isNaN(newAngle))
		{
			ReimPie newReimPie = new ReimPie(getActivity(), 0, 0, newContainer.getWidth(), newContainer.getHeight());
			newContainer.addView(newReimPie);
		}
		else 
		{
			ReimPie newReimPie = new ReimPie(getActivity(), newAngle, doneAngle, newContainer.getWidth(), newContainer.getHeight());
			newContainer.addView(newReimPie);			
		}

		if (processAngle == 0 || Float.isNaN(processAngle))
		{
			ReimPie processReimPie = new ReimPie(getActivity(), 0, 0,	processContainer.getWidth(), processContainer.getHeight());
			processContainer.addView(processReimPie);
		}
		else 
		{
			ReimPie processReimPie = new ReimPie(getActivity(), processAngle, newAngle + doneAngle,
					processContainer.getWidth(), processContainer.getHeight());
			processContainer.addView(processReimPie);	
		}
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
		renderer.setMarginsColor(Color.WHITE);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setYLabelsAlign(Align.CENTER);
		renderer.setBarSpacing(0.5f);
		renderer.setPanEnabled(false, false);
		renderer.setLabelsTextSize(16);

		HashMap<String, String> monthsData = response.getMonthsData();
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		if (monthsData.size() == 0)
		{
			String timeString = Utils.secondToStringUpToDay(Utils.getCurrentTime());
			String currentMonth = timeString.substring(0, timeString.length()-3);
			XYSeries series = new XYSeries(currentMonth);
			series.add(1, 0);
			renderer.addXTextLabel(1, currentMonth);
			dataset.addSeries(series);
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
				dataset.addSeries(series);
			}
		}
		
		GraphicalView mChartView = ChartFactory.getBarChartView(getActivity(), dataset, renderer, Type.STACKED);
		LinearLayout linear = (LinearLayout) getActivity().findViewById(R.id.month_charts);
		linear.addView(mChartView);
	}

	private void initData()
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
							ReimApplication.dismissProgressDialog();
							drawPie();
							drawBar();
						}
					});
				}
			}
		});
	}
}
