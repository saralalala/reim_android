package com.rushucloud.reim;

import java.util.List;


import classes.AppPreference;
import classes.Report;
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

public class ReportFragment extends Fragment {

	private ListView reportListView;
	private ReportListViewAdapter adapter;
	
	private DBManager dbManager;
	private List<Report> reportList;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.fragment_report, container, false);  
	}
	
	public void onActivityCreated(Bundle savedInstanceState)
	{  
        super.onActivityCreated(savedInstanceState);
        dataInitialise();
        viewInitialise();
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
    	final int index = (int)reportListView.getAdapter().getItemId(menuInfo.position);
    	switch (item.getItemId()) 
    	{
			case 0:
				AlertDialog mDialog = new AlertDialog.Builder(getActivity())
													.setTitle("警告")
													.setMessage(R.string.deleteItemWarning)
													.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															int reportLocalID = reportList.get(index).getLocalID();
															if (dbManager.deleteItem(reportLocalID))
															{
																refreshReportListView();
													            Toast.makeText(getActivity(),
													            		R.string.deleteSucceed, Toast.LENGTH_LONG).show();																
															}
															else
															{
													            Toast.makeText(getActivity(),
													            		R.string.deleteFailed, Toast.LENGTH_LONG).show();
															}
															
														}
													})
													.setNegativeButton(R.string.cancel, null)
													.create();
				mDialog.show();
				break;
			default:
				break;
		}    		
		
    	return super.onContextItemSelected(item);
    }
    
    private void dataInitialise()
    {
    	dbManager = DBManager.getDataBaseManager(getActivity());
    	reportList = readReportList();
    }
    
	private void viewInitialise()
	{
		Button addButton = (Button)getActivity().findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(getActivity(), EditItemActivity.class);
				startActivity(intent);
				getActivity().finish();
			}
		});

		adapter = new ReportListViewAdapter(getActivity(), reportList);
		reportListView = (ListView)getActivity().findViewById(R.id.reportListView);
		reportListView.setAdapter(adapter);
		reportListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(getActivity(), EditItemActivity.class);
				intent.putExtra("reportLocalID", reportList.get(position).getLocalID());
				startActivity(intent);
				getActivity().finish();
			}
		});
		registerForContextMenu(reportListView);
	}
	
	private List<Report> readReportList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDataBaseManager(getActivity());
		return dbManager.getUserReports(appPreference.getCurrentUserID());
	}
	
	private void refreshReportListView()
	{
		reportList.clear();
		reportList.addAll(readReportList());
		adapter.set(reportList);
		adapter.notifyDataSetChanged();
	}
}
