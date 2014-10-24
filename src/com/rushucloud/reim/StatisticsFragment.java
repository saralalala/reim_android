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
		dataInitialise();
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

	private void openChart()
	{
		double total = response.get_total();

		LinearLayout newContainer = (LinearLayout) getActivity().findViewById(R.id.new_container);
		LinearLayout doneContainer = (LinearLayout) getActivity().findViewById(R.id.done_container);
		LinearLayout processContainer = (LinearLayout) getActivity().findViewById(R.id.process_container);

		float done_angle = (float) (response.get_done_amount() * 360 / total);
		float new_angle = (float) (response.get_new_amount() * 360 / total);
		float process_angle = (float) (response.get_process_amount() * 360 / total);
		
		if (done_angle == 0 || Float.isNaN(done_angle))
		{
			ReimPie rp_done = new ReimPie(getActivity(), 0, 0, doneContainer.getWidth(), doneContainer.getHeight());
			doneContainer.addView(rp_done);			
		}
		else 
		{
			ReimPie rp_done = new ReimPie(getActivity(), done_angle, 0, doneContainer.getWidth(), doneContainer.getHeight());
			doneContainer.addView(rp_done);			
		}

		if (new_angle == 0 || Float.isNaN(new_angle))
		{
			ReimPie rp_new = new ReimPie(getActivity(), 0, 0, newContainer.getWidth(), newContainer.getHeight());
			newContainer.addView(rp_new);
		}
		else 
		{
			ReimPie rp_done = new ReimPie(getActivity(), new_angle, done_angle,	newContainer.getWidth(), newContainer.getHeight());
			newContainer.addView(rp_done);			
		}

		if (process_angle == 0 || Float.isNaN(process_angle))
		{
			ReimPie rp_new = new ReimPie(getActivity(), 0, 0,	processContainer.getWidth(), processContainer.getHeight());
			processContainer.addView(rp_new);
		}
		else 
		{
			ReimPie rp_process = new ReimPie(getActivity(), process_angle, new_angle + done_angle,
					processContainer.getWidth(), processContainer.getHeight());
			processContainer.addView(rp_process);	
		}
		
		// bar
		draw_bar();
	}

	private void draw_bar()
	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		r.setColor(Color.rgb(50, 143, 201));
		renderer.addSeriesRenderer(r);

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

		HashMap<String, String> _ms = response.get_ms();
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		if (_ms.size() == 0)
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
			Set<String> keys = _ms.keySet();
			int _size = keys.size();
			for (int i = 0; i < _size; i++)
			{
				String _series_key = (String) ((keys.toArray())[i]);
				XYSeries series = new XYSeries(_series_key);
				System.out.println("------------------->" + _series_key + ","
						+ _ms.containsKey(_series_key) + ", " + _ms.get(_series_key));
				String _val = _ms.get(_series_key);
				Long _lval = Long.parseLong(_val);
				series.add(i + 1, _lval);
				renderer.addXTextLabel(i + 1, _series_key);
				dataset.addSeries(series);
			}
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(getActivity(), dataset, renderer,
				Type.STACKED);
		LinearLayout linear = (LinearLayout) getActivity().findViewById(R.id.month_charts);
		linear.addView(mChartView);
	}

	private void dataInitialise()
	{
		ReimApplication.pDialog.show();
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
							ReimApplication.pDialog.dismiss();
							openChart();
						}
					});
				}
			}
		});
	}
}
