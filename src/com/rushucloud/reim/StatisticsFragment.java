package com.rushucloud.reim;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import netUtils.Request.CommonRequest;
import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.StatisticsRequest;
import netUtils.Response.CommonResponse;
import netUtils.Response.StatisticsResponse;

import classes.Item;
import database.DBManager;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.rushucloud.graphics.ReimPie;

public class StatisticsFragment extends Fragment
{

	private GraphicalView mChart;
	private static final int COMPLETED = 0;
	private StatisticsResponse response = null;

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == COMPLETED)
			{
				openChart();
				//stateText.setText("completed");
			}
		}
	};

//	protected DefaultRenderer buildCategoryRenderer(int[] colors)
//	{
//		DefaultRenderer renderer = new DefaultRenderer();
//		renderer.setLabelsTextSize(15);
//		renderer.setLegendTextSize(15);
//		renderer.setMargins(new int[] { 20, 30, 15, 0 });
//		for (int color : colors)
//		{
//			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
//			r.setColor(color);
//			renderer.addSeriesRenderer(r);
//		}
//		return renderer;
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// container.removeAllViews();

		return inflater.inflate(R.layout.fragment_statistics, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		dataInitialise();
		//openChart();
	}

	protected CategorySeries buildCategoryDataset(String[] titles,
			double[] values)
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

		LinearLayout newContainer = (LinearLayout) getActivity()
				.findViewById(R.id.new_container);
		LinearLayout doneContainer = (LinearLayout) getActivity()
				.findViewById(R.id.done_container);
		LinearLayout progressContainer = (LinearLayout) getActivity()
				.findViewById(R.id.process_container);

		float done_angle = (float) (response.get_done_amount() * 360 / total);
		ReimPie rp_done = new ReimPie(getActivity()
				.getBaseContext(), done_angle, 0);
		doneContainer.addView(rp_done);

		float new_angle = (float) (response.get_new_amount() * 360 / total);
		ReimPie rp_new = new ReimPie(
				getActivity().getBaseContext(), new_angle,
				done_angle);
		newContainer.addView(rp_new);

		float process_angle = (float) (response
				.get_process_amount() * 360 / total);
		ReimPie rp_process = new ReimPie(getActivity()
				.getBaseContext(), process_angle, new_angle
				+ done_angle);
		progressContainer.addView(rp_process);
		
		// bar
		draw_bar();
		
	}
	
	private void draw_bar(){
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        SimpleSeriesRenderer r = new SimpleSeriesRenderer();        
        r.setColor(Color.rgb(50, 143, 201));           
        renderer.addSeriesRenderer(r);
       
       
        renderer.setChartTitle( "月度详细" );       
        renderer.setXAxisMin(0);     
        renderer.setXAxisMax(12);
        renderer.setXLabels(0);
       
        renderer.setYAxisMin(0);     
        renderer.setYAxisMax(1000);   
        renderer.setYLabels(15);
       
        renderer.setAxisTitleTextSize(18);
        renderer.setDisplayValues(true);
        //renderer.setDisplayChartValues(true);      
        renderer.setShowGrid(true);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);                      
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setBarSpacing(0.5f);


        renderer.setPanEnabled(false, true);
        renderer.setLabelsTextSize(16);
        
        HashMap<String, String> _ms = response.get_ms();

    	XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    	Set<String> keys = _ms.keySet();
    	int _size = keys.size();
        for(int i = 0; i < keys.size(); i++){
        	String _series_key = (String)((keys.toArray())[i]);
        	 XYSeries series = new XYSeries(_series_key);
        	 System.out.println("------------------->" + _series_key + "," + _ms.containsKey(_series_key) + ", " + _ms.get(_series_key));
        	 String _val =  _ms.get(_series_key);
        	 Long _lval = Long.parseLong(_val); 
        	 series.add(i + 1,  _lval);
        	 renderer.addXTextLabel(i + 1, _series_key);      
        	 dataset.addSeries(series);
        }
		GraphicalView mChartView = ChartFactory.getBarChartView(getActivity(), dataset, renderer, Type.STACKED);
        LinearLayout linear = (LinearLayout) getActivity().findViewById(R.id.month_charts);
        linear.addView(mChartView);
	}

	private void dataInitialise()
	{

		StatisticsRequest request = new StatisticsRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{

				response = new StatisticsResponse(
						httpResponse);
				if (response.getStatus())
				{
					Message msg = new Message();  
		            msg.what = COMPLETED;  
		            handler.sendMessage(msg);  
		            
				}
				else
				{
					/*
					 * getActivity().runOnUiThread(new Runnable() { public void
					 * run() {
					 * 
					 * } });
					 */
				}

			}
		});
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// refreshItemListView();
	}
}
