package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rushucloud.reim.item.EditItemActivity;
import com.rushucloud.reim.item.SearchItemActivity;
import com.rushucloud.reim.item.ShowItemActivity;
import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Item.DeleteItemRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Item.DeleteItemResponse;
import classes.Category;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Tag;
import classes.Utils.AppPreference;
import classes.Utils.Utils;
import classes.Widget.SegmentedGroup;
import classes.Widget.XListView;
import classes.Widget.XListView.IXListViewListener;
import classes.Adapter.ItemListViewAdapter;
import classes.Adapter.ItemTagGridViewAdapter;
import database.DBManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment implements OnKeyListener, IXListViewListener
{
	private static final int FILTER_TYPE_ALL = 0;
	private static final int FILTER_TYPE_PROVE_AHEAD = 1;
	private static final int FILTER_TYPE_CONSUMED = 2;
	private static final int FILTER_STATUS_ALL = 0;
	private static final int FILTER_STATUS_FREE = 1;
	private static final int FILTER_STATUS_ADDED = 2;	
	private static final int SORT_NULL = 0;	
	private static final int SORT_AMOUNT = 1;	
	private static final int SORT_CONSUMED_DATE = 2;	
	
	private View view;
	private View filterView;
	private XListView itemListView;
	private ItemListViewAdapter adapter;
	private PopupWindow deletePopupWindow;

	private WindowManager windowManager;
	private LayoutParams params = new LayoutParams();
	private AppPreference appPreference;
	private DBManager dbManager;
	private List<Item> itemList = new ArrayList<Item>();
	private List<Item> showList = new ArrayList<Item>();
	private List<Tag> tagList = new ArrayList<Tag>();
	
	private int filterType = FILTER_TYPE_ALL;
	private int filterStatus = FILTER_STATUS_ALL;
	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	private List<Tag> filterTagList = new ArrayList<Tag>();
	
	private int tempFilterType = FILTER_TYPE_ALL;
	private int tempFilterStatus = FILTER_STATUS_ALL;
	private int tempSortType = SORT_NULL;	
	
	private boolean hasInit = false;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (view == null)
		{
			view = inflater.inflate(R.layout.fragment_reim, container, false);
		}
		else
		{
			ViewGroup viewGroup = (ViewGroup) view.getParent();
			if (viewGroup != null)
			{
				viewGroup.removeView(view);
			}
		}
		return view;
	}

	public void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ReimFragment");
		if (!hasInit)
		{
 			initData();
   			initView();
			setHasOptionsMenu(true);
			hasInit = true;
			refreshItemListView();
			syncItems();		
		}	
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ReimFragment");
	}

	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && hasInit)
		{
			ReimApplication.showProgressDialog();
			refreshItemListView();
			ReimApplication.dismissProgressDialog();
			syncItems();
		}
	}

	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		dbManager.executeTempCommand();

		itemList.clear();
		itemList.addAll(readItemList());
		filterItemList();
		
		if (Utils.isNetworkConnected())
		{
			for (Item item : showList)
			{
				Category category = item.getCategory();
				if (category != null && category.hasUndownloadedIcon())
				{
					sendDownloadCategoryIconRequest(category);
				}
			}
		}
	}

	private void initView()
	{
		initListView();
		initFilterView();
		initSearchView();
	}
	
	private void initListView()
	{
		adapter = new ItemListViewAdapter(getActivity(), itemList);
		itemListView = (XListView) getActivity().findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setXListViewListener(this);
		itemListView.setPullRefreshEnable(true);
		itemListView.setPullLoadEnable(false);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Item item = showList.get(position-1);
				if (item.getBelongReport() == null || item.getBelongReport().isEditable())
				{
					Intent intent = new Intent(getActivity(), EditItemActivity.class);
					intent.putExtra("itemLocalID", item.getLocalID());
					intent.putExtra("fromReim", true);
					startActivity(intent);
				}
				else
				{
					Intent intent = new Intent(getActivity(), ShowItemActivity.class);
					intent.putExtra("itemLocalID", item.getLocalID());
					startActivity(intent);
				}
			}
		});
		itemListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				showDeleteWindow(position - 1);
				return false;
			}
		});
	}
	
	private void initFilterView()
	{
		ImageView filterImageView = (ImageView) view.findViewById(R.id.filterImageView);
		filterImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CLICK");
				windowManager.addView(filterView, params);
			}
		});		
		
		windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		filterView = View.inflate(getActivity(), R.layout.window_reim_filter, null);
		filterView.setBackgroundColor(Color.WHITE);
		filterView.setMinimumHeight(metrics.heightPixels);
		
		filterView.setFocusable(true);
		filterView.setFocusableInTouchMode(true);
		filterView.setOnKeyListener(this);

		final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
		final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);		
		final RadioButton sortConsumedDateRadio = (RadioButton)filterView.findViewById(R.id.sortConsumedDateRadio);	
		SegmentedGroup sortRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.sortRadioGroup);
		sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if (checkedId == sortNullRadio.getId())
				{
					tempSortType = SORT_NULL;
				}
				else if (checkedId == sortAmountRadio.getId())
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_AMOUNT");
					tempSortType = SORT_AMOUNT;
				}
				else if (checkedId == sortConsumedDateRadio.getId())
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_TIME");
					tempSortType = SORT_CONSUMED_DATE;
				}
			}
		});

		final RadioButton filterTypeAllRadio = (RadioButton)filterView.findViewById(R.id.filterTypeAllRadio);
		final RadioButton filterProveAheadRadio = (RadioButton)filterView.findViewById(R.id.filterProveAheadRadio);
		final RadioButton filterConsumedRadio = (RadioButton)filterView.findViewById(R.id.filterConsumedRadio);			
		SegmentedGroup filterTypeRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.filterTypeRadioGroup);
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
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_PROVE_AHEAD");
					tempFilterType = FILTER_TYPE_PROVE_AHEAD;
				}
				else if (checkedId == filterConsumedRadio.getId())
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_REIMBURSED");
					tempFilterType = FILTER_TYPE_CONSUMED;
				}
			}
		});

		final RadioButton filterStatusAllRadio = (RadioButton)filterView.findViewById(R.id.filterStatusAllRadio);
		final RadioButton filterFreeRadio = (RadioButton)filterView.findViewById(R.id.filterFreeRadio);
		final RadioButton filterAddedRadio = (RadioButton)filterView.findViewById(R.id.filterAddedRadio);			
		SegmentedGroup filterStatusRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.filterStatusRadioGroup);
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
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_NO_IN_REPORT");
					tempFilterStatus = FILTER_STATUS_FREE;
				}
				else if (checkedId == filterAddedRadio.getId())
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_IN_REPORT");
					tempFilterStatus = FILTER_STATUS_ADDED;
				}
			}
		});

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		int interval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		int tagWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);	
		int tagMaxCount = (metrics.widthPixels - padding * 2 + interval) / (tagWidth + interval);
		
		final ItemTagGridViewAdapter tagAdapter = new ItemTagGridViewAdapter(getActivity(), tagList);
		
		GridView tagGridView = (GridView)filterView.findViewById(R.id.tagGridView);
		tagGridView.setAdapter(tagAdapter);
		tagGridView.setNumColumns(tagMaxCount);
		tagGridView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_TAG");
				tagAdapter.setSelection(position);
				tagAdapter.notifyDataSetChanged();
			}
		});

		ImageView confirmImageView = (ImageView)filterView.findViewById(R.id.confirmImageView);
		confirmImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				sortReverse = sortType == tempSortType ? !sortReverse : false;
				sortType = tempSortType;
				filterType = tempFilterType;
				filterStatus = tempFilterStatus;
				filterTagList.clear();
				boolean[] check = tagAdapter.getCheckedTags();
				for (int i = 0; i < check.length; i++)
				{
					if (check[i])
					{
						filterTagList.add(tagList.get(i));
					}
				}					
				
				windowManager.removeView(filterView);
				ReimApplication.showProgressDialog();
				refreshItemListView();
				ReimApplication.dismissProgressDialog();
			}
		});

		ImageView cancelImageView = (ImageView)filterView.findViewById(R.id.cancelImageView);
		cancelImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				windowManager.removeView(filterView);
			}
		});
	}
	
	private void initSearchView()
	{		
		ImageView searchImageView = (ImageView) getActivity().findViewById(R.id.searchImageView);
		searchImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SEARCH_LOCAL");
				Intent intent = new Intent(getActivity(), SearchItemActivity.class);
				startActivity(intent);
			}
		});		
	}

	private List<Item> readItemList()
	{
		return dbManager.getUserItems(appPreference.getCurrentUserID());
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
			if (filterType == FILTER_TYPE_CONSUMED && !item.needReimbursed())
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
			
			if (filterTagList.size() > 0 && !item.containsSpecificTags(filterTagList))
			{
				continue;
			}
			showList.add(item);
		}

		if (sortType == SORT_NULL)
		{
			Item.sortByUpdateDate(showList);
		}
		if (sortType == SORT_AMOUNT)
		{
			Item.sortByAmount(showList);
		}
		if (sortType == SORT_CONSUMED_DATE)
		{
			Item.sortByConsumedDate(showList);
		}
		
		if (sortReverse)
		{
			Collections.reverse(showList);
		}
	}
	
	private void refreshItemListView()
	{
		itemList.clear();
		itemList.addAll(readItemList());
		filterItemList();
		adapter.set(showList);
		adapter.notifyDataSetChanged();
	}

    private void showDeleteWindow(final int index)
    {    
    	if (deletePopupWindow == null)
		{
    		View deleteView = View.inflate(getActivity(), R.layout.window_delete, null);
    		
    		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
    		deleteButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				deletePopupWindow.dismiss();
    				
    				final Item localItem = showList.get(index);
    				Report report = localItem.getBelongReport();

    				if (report != null && !report.isEditable())
    				{
    					Utils.showToast(getActivity(), "条目已提交，不可删除");

    				}
    				else
    				{
    					Builder builder = new Builder(getActivity());
    					builder.setTitle(R.string.warning);
    					builder.setMessage(R.string.prompt_delete_item);
    					builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
    													{
    														public void onClick(DialogInterface dialog, int which)
    														{
    															if (localItem.getServerID() == -1)
    															{
    																deleteItemFromLocal(localItem.getLocalID());
    															}
    															else if (!Utils.isNetworkConnected())
    															{
    																Utils.showToast(getActivity(), "网络未连接，无法删除");
    															}
    															else
    															{
    																sendDeleteItemRequest(localItem);
    															}
    														}
    													});
    					builder.setNegativeButton(R.string.cancel, null);
    					builder.create().show();
    				}
    			}
    		});
    		deleteButton = Utils.resizeWindowButton(deleteButton);
    		
    		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
    		cancelButton.setOnClickListener(new View.OnClickListener()
    		{
    			public void onClick(View v)
    			{
    				deletePopupWindow.dismiss();
    			}
    		});
    		cancelButton = Utils.resizeWindowButton(cancelButton);
    		
    		deletePopupWindow = Utils.constructPopupWindow(getActivity(), deleteView);    	
		}
    	
		deletePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		Utils.dimBackground(getActivity());
    }
    
    private void sendDownloadCategoryIconRequest(final Category category)
    {
    	DownloadImageRequest request = new DownloadImageRequest(category.getIconID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String iconPath = Utils.saveIconToFile(response.getBitmap(), category.getIconID());
					category.setIconPath(iconPath);
					category.setLocalUpdatedDate(Utils.getCurrentTime());
					category.setServerUpdatedDate(category.getLocalUpdatedDate());
					dbManager.updateCategory(category);
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							refreshItemListView();
						}
					});
				}
			}
		});
    }
    
	private void sendDeleteItemRequest(final Item item)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(getActivity(), R.string.prompt_delete_failed);
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
			ReimApplication.dismissProgressDialog();
			Utils.showToast(getActivity(), R.string.prompt_delete_succeed);
		}
		else
		{
			ReimApplication.dismissProgressDialog();
			Utils.showToast(getActivity(), R.string.prompt_delete_failed);
		}
	}
	
	private void syncItems()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
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

					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
	}
	
	public boolean onKey(View v, int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			windowManager.removeView(filterView);
		}
		return false;
	}
	
	public void onRefresh()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							itemListView.stopRefresh();
							itemListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							refreshItemListView();
						}
					});

					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					itemListView.stopRefresh();
//					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "网络未连接，无法刷新";
					Utils.showToast(getActivity(), prompt);
				}
			});
		}		
	}

	public void onLoadMore()
	{
		if (SyncUtils.canSyncToServer())
		{
			SyncUtils.isSyncOnGoing = true;
			SyncUtils.syncFromServer(new SyncDataCallback()
			{
				public void execute()
				{
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
							itemListView.stopLoadMore();
							itemListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
							refreshItemListView();
						}
					});

					SyncUtils.syncAllToServer(new SyncDataCallback()
					{
						public void execute()
						{
							SyncUtils.isSyncOnGoing = false;
						}
					});
				}
			});
		}
		else
		{
			getActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					itemListView.stopLoadMore();
//					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
					String prompt = SyncUtils.isSyncOnGoing ? "正在同步中" : "网络未连接，无法刷新";
					Utils.showToast(getActivity(), prompt);
				}
			});
		}	
	}
}