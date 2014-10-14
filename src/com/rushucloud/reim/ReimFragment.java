package com.rushucloud.reim;

import java.util.List;

import classes.AppPreference;
import classes.Item;
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
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment
{

	private ListView itemListView;
	private ItemListViewAdapter adapter;

	private DBManager dbManager;
	private List<Item> itemList;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_reimbursement, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		dataInitialise();
		viewInitialise();
	}

	public void onResume()
	{
		super.onResume();
		refreshItemListView();
//		setHasOptionsMenu(true);
	}

	public void onPause()
	{
		super.onPause();
//		setHasOptionsMenu(false);
	}

//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
//	{
//		super.onCreateOptionsMenu(menu, inflater);
//		inflater.inflate(R.menu.searchview, menu);
//		SearchView searchView = (SearchView)menu.findItem(R.id.search_item).getActionView();
//		searchView.setQueryHint(getActivity().getString(R.string.inputKeyword));
//		searchView.setOnQueryTextListener(new OnQueryTextListener()
//		{
//			public boolean onQueryTextSubmit(String query)
//			{
//				// TODO get from server
//				return false;
//			}
//			
//			public boolean onQueryTextChange(String newText)
//			{
//				// TODO filter local
//				return false;
//			}
//		});
//	}
	
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
		switch (item.getItemId())
		{
			case 0:
				AlertDialog mDialog = new AlertDialog.Builder(getActivity()).setTitle("警告")
						.setMessage(R.string.deleteItemWarning)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								int itemLocalID = itemList.get(index).getLocalID();
								if (dbManager.deleteItem(itemLocalID))
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
						}).setNegativeButton(R.string.cancel, null).create();
				mDialog.show();
				break;
			default:
				break;
		}

		return super.onContextItemSelected(item);
	}

	private void dataInitialise()
	{
		dbManager = DBManager.getDBManager();
		itemList = readItemList();
	}

	private void viewInitialise()
	{
		Button addButton = (Button) getActivity().findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
//				Intent intent = new Intent(getActivity(), EditItemActivity.class);
				Intent intent = new Intent(getActivity(), SearchItemActivity.class);
				startActivity(intent);
			}
		});

		adapter = new ItemListViewAdapter(getActivity(), itemList);
		itemListView = (ListView) getActivity().findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(getActivity(), EditItemActivity.class);
				intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
				startActivity(intent);
			}
		});
		registerForContextMenu(itemListView);
	}

	private List<Item> readItemList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDBManager();
		return dbManager.getUserItems(appPreference.getCurrentUserID());
	}

	private void refreshItemListView()
	{
		itemList.clear();
		itemList.addAll(readItemList());
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
	}
}