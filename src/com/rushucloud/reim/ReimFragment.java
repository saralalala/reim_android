package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.Item.DeleteItemRequest;
import netUtils.Response.Item.DeleteItemResponse;

import classes.AppPreference;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Adapter.ItemListViewAdapter;
import database.DBManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment
{
	private View view;
	private Button addButton;
	private ListView itemListView;
	private ItemListViewAdapter adapter;

	private DBManager dbManager;
	private List<Item> itemList = new ArrayList<Item>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.fragment_reim, container, false);
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
		MobclickAgent.onPageStart("ReimFragment");
		viewInitialise();
		dataInitialise();
		refreshItemListView();
		setHasOptionsMenu(true);
	}
	
	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReimFragment");
		setHasOptionsMenu(false);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.single_item, menu);
		MenuItem menuItem = menu.findItem(R.id.action_item);
		menuItem.setIcon(R.drawable.ic_action_search);
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_item)
		{
			startActivity(new Intent(getActivity(), SearchItemActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "删除");
	}

	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		final int index = (int) itemListView.getAdapter().getItemId(menuInfo.position);
		final Item localItem = itemList.get(index);
		Report report = localItem.getBelongReport();
		switch (item.getItemId())
		{
			case 0:
				if (report != null && (report.getStatus() != Report.STATUS_DRAFT || 
									   report.getStatus() != Report.STATUS_REJECT))
				{
					Toast.makeText(getActivity(), "条目已提交，不可删除", Toast.LENGTH_SHORT).show();
					
				}
				else
				{
					AlertDialog mDialog = new AlertDialog.Builder(getActivity()).setTitle("警告")
											.setMessage(R.string.deleteItemWarning)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													sendDeleteItemRequest(localItem);
												}
											}).setNegativeButton(R.string.cancel, null).create();
					mDialog.show();
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
		
		if (ReimApplication.needToSync)
		{
			ReimApplication.needToSync = false;
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							adapter.notifyDataSetChanged();
						}
					});
				}
			});
		}
	}

	private void viewInitialise()
	{
		if (addButton == null)
		{
			addButton = (Button) getActivity().findViewById(R.id.addButton);
			addButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), EditItemActivity.class);
					startActivity(intent);
				}
			});
		}

		if (adapter == null)
		{
			adapter = new ItemListViewAdapter(getActivity(), itemList);
		}
		
		if (itemListView == null)
		{
			itemListView = (ListView) getActivity().findViewById(R.id.itemListView);
			itemListView.setAdapter(adapter);
			itemListView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Item item = itemList.get(position);
					if (item.getBelongReport() == null || 
							item.getBelongReport().getStatus() == Report.STATUS_DRAFT || 
							item.getBelongReport().getStatus() == Report.STATUS_REJECT)
					{
						Intent intent = new Intent(getActivity(), EditItemActivity.class);
						intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
						startActivity(intent);					
					}
					else
					{
						Intent intent = new Intent(getActivity(), ShowItemActivity.class);
						intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
						startActivity(intent);					
					}
				}
			});
			registerForContextMenu(itemListView);
		}
	}

	private List<Item> readItemList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getUserItems(appPreference.getCurrentUserID());
	}

	private void refreshItemListView()
	{
		ReimApplication.pDialog.show();
		itemList.clear();
		itemList.addAll(readItemList());
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
		ReimApplication.pDialog.dismiss();
	}
	
	private void sendDeleteItemRequest(final Item item)
	{
		DeleteItemRequest request = new DeleteItemRequest(item.getServerID());
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final DeleteItemResponse response = new DeleteItemResponse(httpResponse);
				if (response.getStatus())
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							if (dbManager.deleteItem(item.getLocalID()))
							{								
								refreshItemListView();
								Toast.makeText(getActivity(), R.string.deleteSucceed,
										Toast.LENGTH_LONG).show();
							}
							else
							{
								Toast.makeText(getActivity(), R.string.deleteFailed,
										Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog mDialog = new AlertDialog.Builder(getActivity())
													.setTitle("条目删除失败")
													.setMessage(response.getErrorMessage())
													.setNegativeButton(R.string.confirm, null)
													.create();
							mDialog.show();
						}
					});					
				}
			}
		});
	}
}