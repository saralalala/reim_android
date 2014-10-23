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
import classes.Utils;
import classes.Adapter.ItemListViewAdapter;
import database.DBManager;
import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
			ViewGroup viewGroup = (ViewGroup) view.getParent();
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

	@SuppressLint("NewApi") 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.reim, menu);
		Spinner spinner = (Spinner)menu.findItem(R.id.action_spinner_item).getActionView();
		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.itemSpinner, android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_search_item)
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
				if (!Utils.isNetworkConnected(getActivity()))
				{
					Toast.makeText(getActivity(), "网络未连接，无法删除", Toast.LENGTH_SHORT).show();
				}
				else if (report != null
						&& (report.getStatus() != Report.STATUS_DRAFT || report.getStatus() != Report.STATUS_REJECT))
				{
					Toast.makeText(getActivity(), "条目已提交，不可删除", Toast.LENGTH_SHORT).show();

				}
				else
				{
					AlertDialog mDialog = new AlertDialog.Builder(getActivity())
							.setTitle("警告")
							.setMessage(R.string.deleteItemWarning)
							.setPositiveButton(R.string.confirm,
									new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which)
										{
											if (localItem.getServerID() == -1)
											{
												deleteItemFromLocal(localItem.getLocalID());
											}
											else
											{
												sendDeleteItemRequest(localItem);
											}
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

		if (ReimApplication.needToSync && Utils.canSyncToServer(getActivity()))
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
							refreshItemListView();
						}
					});

					SyncUtils.syncAllToServer(null);
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
					if (item.getBelongReport() == null
							|| item.getBelongReport().getStatus() == Report.STATUS_DRAFT
							|| item.getBelongReport().getStatus() == Report.STATUS_REJECT)
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
		ReimApplication.pDialog.show();
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
							deleteItemFromLocal(item.getLocalID());
						}
					});
				}
				else
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.pDialog.show();
							Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			}
		});
	}

	private void deleteItemFromLocal(int itemLocalID)
	{
		if (dbManager.deleteItem(itemLocalID))
		{
			refreshItemListView();
			ReimApplication.pDialog.dismiss();
			Toast.makeText(getActivity(), R.string.deleteSucceed, Toast.LENGTH_LONG).show();
		}
		else
		{
			ReimApplication.pDialog.dismiss();
			Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_LONG).show();
		}
	}
}