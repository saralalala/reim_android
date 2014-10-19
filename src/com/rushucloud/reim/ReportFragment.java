package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.DeleteReportRequest;
import netUtils.Response.Report.DeleteReportResponse;


import classes.AppPreference;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;
import classes.Adapter.ReportListViewAdapter;
import database.DBManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.Fragment;

public class ReportFragment extends Fragment
{
	private View view;
	private Button addButton;
	private ListView reportListView;
	private ReportListViewAdapter adapter;
	
	private DBManager dbManager;
	private List<Report> reportList = new ArrayList<Report>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.fragment_report, container, false);
		}
		else
		{
			ViewGroup viewGroup = (ViewGroup)view.getParent();
			viewGroup.removeView(view);
		}
	    return view;  
	}
	   
	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReportFragment");	
        viewInitialise();
        dataInitialise();
		refreshReportListView();
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReportFragment");
	}
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.setHeaderTitle("选项");
    	menu.add(0,0,0,"删除");
    }

    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	int index = (int)reportListView.getAdapter().getItemId(menuInfo.position);
    	final Report report = reportList.get(index);
    	switch (item.getItemId()) 
    	{
			case 0:
				if (!Utils.isDataConnected(getActivity()))
				{
					Toast.makeText(getActivity(), "网络未连接，无法删除", Toast.LENGTH_SHORT).show();
				}
				else if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECT)
				{
					AlertDialog mDialog = new AlertDialog.Builder(getActivity())
														.setTitle("警告")
														.setMessage(R.string.deleteReportWarning)
														.setPositiveButton(R.string.confirm, 
																new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																if (report.getServerID() == -1)
																{
																	deleteReportFromLocal(report.getLocalID());
																}
																else
																{
																	sendDeleteReportRequest(report);
																}
															}
														})
														.setNegativeButton(R.string.cancel, null)
														.create();
					mDialog.show();
				}
				else
				{
					Toast.makeText(getActivity(), "报告已提交，不可删除", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}    		
		
    	return super.onContextItemSelected(item);
    }
    
    private void dataInitialise()
    {
		if (dbManager == null)
		{
			dbManager = DBManager.getDBManager();			
		}
    }
    
	private void viewInitialise()
	{
		if (addButton == null)
		{
			addButton = (Button)getActivity().findViewById(R.id.addButton);
			addButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), EditReportActivity.class);
					startActivity(intent);
				}
			});			
		}

		if (adapter == null)
		{
			adapter = new ReportListViewAdapter(getActivity(), reportList);			
		}
		
		if (reportListView == null)
		{
			reportListView = (ListView)getActivity().findViewById(R.id.reportListView);
			reportListView.setAdapter(adapter);
			reportListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", reportList.get(position));
					Intent intent = new Intent(getActivity(), EditReportActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			registerForContextMenu(reportListView);
		}
	}
	
	private List<Report> readReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private void refreshReportListView()
	{
		reportList.clear();
		reportList.addAll(readReportList());
		adapter.set(reportList);
		adapter.notifyDataSetChanged();
	}

	private void sendDeleteReportRequest(final Report report)
	{
		ReimApplication.pDialog.show();
		DeleteReportRequest request = new DeleteReportRequest(report.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DeleteReportResponse response = new DeleteReportResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							deleteReportFromLocal(report.getLocalID());
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.pDialog.dismiss();
				            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
						}
					});		
				}
			}
		});
	}
	
	private void deleteReportFromLocal(int reportLocalID)
	{
		if (dbManager.deleteReport(reportLocalID))
		{
			refreshReportListView();
			ReimApplication.pDialog.dismiss();
            Toast.makeText(getActivity(), R.string.deleteSucceed, Toast.LENGTH_SHORT).show();														
		}
		else
		{
			ReimApplication.pDialog.dismiss();
            Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_SHORT).show();
		}		
	}
}