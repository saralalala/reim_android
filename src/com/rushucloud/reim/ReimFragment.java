package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
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
import classes.Tag;
import classes.Utils;
import classes.Adapter.ItemListViewAdapter;
import classes.Adapter.TagGridViewAdapter;
import database.DBManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment
{
	private static final int FILTER_TYPE_ALL = 0;
	private static final int FILTER_TYPE_PROVE_AHEAD = 1;
	private static final int FILTER_TYPE_CONSUMED = 2;
	private static final int FILTER_STATUS_ALL = 0;
	private static final int FILTER_STATUS_FREE = 1;
	private static final int FILTER_STATUS_ADDED = 2;	
	private static final int SORT_NULL = 0;	
	private static final int SORT_AMOUNT = 1;	
	private static final int SORT_DATE = 2;	
	
	private View view;
	private View filterView;
	private Button addButton;
	private ListView itemListView;
	private ItemListViewAdapter adapter;

	private WindowManager windowManager;
	private AppPreference appPreference;
	private DBManager dbManager;
	private List<Item> itemList = new ArrayList<Item>();
	private List<Item> showList = new ArrayList<Item>();
	private List<Tag> tagList = new ArrayList<Tag>();
	
	private int filterType = FILTER_TYPE_ALL;
	private int filterStatus = FILTER_STATUS_ALL;
	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	
	private int tempFilterType = FILTER_TYPE_ALL;
	private int tempFilterStatus = FILTER_STATUS_ALL;
	private int tempSortType = SORT_NULL;
	
	private List<Tag> filterTagList = new ArrayList<Tag>();
	private boolean[] tagCheck;
		
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
		setHasOptionsMenu(true);
		return view;
	}

	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReimFragment");
		ReimApplication.pDialog.show();
		dataInitialise();
		viewInitialise();
		refreshItemListView();
		ReimApplication.pDialog.dismiss();
		syncItems();
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReimFragment");
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.reim, menu);
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
		else if (id == R.id.action_filter_item)
		{
			showFilterView();
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
						&& (report.getStatus() != Report.STATUS_DRAFT || report.getStatus() != Report.STATUS_REJECTED))
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
		if (appPreference == null)
		{
			appPreference = AppPreference.getAppPreference();
		}
		
		if (dbManager == null)
		{
			dbManager = DBManager.getDBManager();
//			tags = dbManager.getGroupTags(appPreference.getCurrentGroupID());
			
			for (int i = 0; i < 6; i++)
			{
				Tag tag = new Tag();
				tag.setName("Tag"+i);
				tagList.add(tag);
			}
			
			tagCheck = new boolean[tagList.size()];
			for (int i = 0; i < tagCheck.length; i++)
			{
				tagCheck[i] = false;
			}
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
							|| item.getBelongReport().getStatus() == Report.STATUS_REJECTED)
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
		
		if (filterView == null)
		{
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			
			filterView = getActivity().getLayoutInflater().inflate(R.layout.reim_filter, (ViewGroup) null, false);
			filterView.setBackgroundColor(Color.WHITE);
			filterView.setMinimumHeight(dm.heightPixels);

			final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
			final RadioButton sortDateRadio = (RadioButton)filterView.findViewById(R.id.sortDateRadio);
			final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);			
			RadioGroup sortRadioGroup = (RadioGroup)filterView.findViewById(R.id.sortRadioGroup);
			sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (checkedId == sortNullRadio.getId())
					{
						tempSortType = SORT_NULL;
					}
					else if (checkedId == sortDateRadio.getId())
					{
						tempSortType = SORT_DATE;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						tempSortType = SORT_AMOUNT;
					}
				}
			});

			final RadioButton filterTypeAllRadio = (RadioButton)filterView.findViewById(R.id.filterTypeAllRadio);
			final RadioButton filterProveAheadRadio = (RadioButton)filterView.findViewById(R.id.filterProveAheadRadio);
			final RadioButton filterConsumedRadio = (RadioButton)filterView.findViewById(R.id.filterConsumedRadio);			
			RadioGroup filterTypeRadioGroup = (RadioGroup)filterView.findViewById(R.id.filterTypeRadioGroup);
			filterTypeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (checkedId == filterTypeAllRadio.getId())
					{
						tempFilterType = FILTER_TYPE_ALL;
					}
					else if (checkedId == filterProveAheadRadio.getId())
					{
						tempFilterType = FILTER_TYPE_PROVE_AHEAD;
					}
					else if (checkedId == filterConsumedRadio.getId())
					{
						tempFilterType = FILTER_TYPE_CONSUMED;
					}
				}
			});

			final RadioButton filterStatusAllRadio = (RadioButton)filterView.findViewById(R.id.filterStatusAllRadio);
			final RadioButton filterFreeRadio = (RadioButton)filterView.findViewById(R.id.filterFreeRadio);
			final RadioButton filterAddedRadio = (RadioButton)filterView.findViewById(R.id.filterAddedRadio);			
			RadioGroup filterStatusRadioGroup = (RadioGroup)filterView.findViewById(R.id.filterStatusRadioGroup);
			filterStatusRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					if (checkedId == filterStatusAllRadio.getId())
					{
						tempFilterStatus = FILTER_STATUS_ALL;
					}
					else if (checkedId == filterFreeRadio.getId())
					{
						tempFilterStatus = FILTER_STATUS_FREE;
					}
					else if (checkedId == filterAddedRadio.getId())
					{
						tempFilterStatus = FILTER_STATUS_ADDED;
					}
				}
			});

			final TagGridViewAdapter tagAdapter = new TagGridViewAdapter(getActivity(), tagList);
			
			GridView tagGridView = (GridView)filterView.findViewById(R.id.tagGridView);
			tagGridView.setAdapter(tagAdapter);
			tagGridView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					tagCheck[position] = !tagCheck[position];
					tagAdapter.setSelection(position);
					tagAdapter.notifyDataSetChanged();
				}
			});
			
			Button confirmButton = (Button)filterView.findViewById(R.id.confirmButton);
			confirmButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					sortType = tempSortType;
					filterType = tempFilterType;
					filterStatus = tempFilterStatus;
					filterTagList.clear();
					for (int i = 0; i < tagCheck.length; i++)
					{
						if (tagCheck[i])
						{
							filterTagList.add(tagList.get(i));
						}
					}
					
					sortReverse = !sortReverse;
					
					windowManager.removeView(filterView);
					ReimApplication.pDialog.show();
					refreshItemListView();
					ReimApplication.pDialog.dismiss();
				}
			});
			
			Button cancelButton = (Button)filterView.findViewById(R.id.cancelButton);
			cancelButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					windowManager.removeView(filterView);
				}
			});
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
		itemList.clear();
		itemList.addAll(readItemList());
		filterItemList();
		adapter.set(showList);
		adapter.notifyDataSetChanged();
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
							ReimApplication.pDialog.dismiss();
							Toast.makeText(getActivity(), R.string.deleteFailed, Toast.LENGTH_LONG).show();
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

	private void showFilterView()
	{		
		windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
		LayoutParams params = new LayoutParams();
		windowManager.addView(filterView, params);
	}
	
	private void filterItemList()
	{
		showList.clear();
		for (Item item : itemList)
		{
			if (filterType == FILTER_TYPE_PROVE_AHEAD && !item.isProveAhead())
			{
				continue;
			}
			if (filterType == FILTER_TYPE_CONSUMED && item.isProveAhead())
			{
				continue;
			}
			
			if (filterStatus == FILTER_STATUS_FREE && item.getBelongReport() != null && item.getBelongReport().getLocalID() != -1)
			{
				continue;
			}			
			if (filterStatus == FILTER_STATUS_ADDED && (item.getBelongReport() == null || item.getBelongReport().getLocalID() == -1))
			{
				continue;
			}
			
			if (filterTagList.size() > 0 && filterTagList.size() < tagList.size())
			{
				if (!item.containsSpecificTags(filterTagList))
				{	
					continue;
				}
			}
			showList.add(item);
		}
		
		if (sortType == SORT_AMOUNT)
		{
			Item.sortByAmount(showList);
		}
		if (sortType == SORT_DATE)
		{
			Item.sortByDate(showList);
		}
		
		if (sortReverse)
		{
			Collections.reverse(showList);
		}
	}

	private void syncItems()
	{
		if (Utils.canSyncToServer(getActivity()))
		{
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
}