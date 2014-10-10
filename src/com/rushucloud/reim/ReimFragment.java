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

public class ReimFragment extends Fragment {

	private ListView itemListView;
	private ItemListViewAdapter adapter;

	private DBManager dbManager;
	private List<Item> itemList;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_reimbursement, container, false);
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
    	final int index = (int)itemListView.getAdapter().getItemId(menuInfo.position);
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
															int itemLocalID = itemList.get(index).getLocalID();
															if (dbManager.deleteItem(itemLocalID))
															{
																refreshItemListView();
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
		itemList = readItemList();
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

		adapter = new ItemListViewAdapter(getActivity(), itemList);
		itemListView = (ListView)getActivity().findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(getActivity(), EditItemActivity.class);
				intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
				startActivity(intent);
				getActivity().finish();
			}
		});
		registerForContextMenu(itemListView);
	}
	
	private List<Item> readItemList()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		DBManager dbManager = DBManager.getDataBaseManager(getActivity());
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
