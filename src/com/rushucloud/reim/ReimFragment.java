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
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Item.DeleteItemResponse;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Item.DeleteItemRequest;
import classes.Category;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.adapter.ItemListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.SegmentedGroup;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.support.v4.app.Fragment;

public class ReimFragment extends Fragment implements IXListViewListener
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
	private PopupWindow filterPopupWindow;
	private LinearLayout tagLayout;
	private LinearLayout categoryLayout;
	private XListView itemListView;
	private ItemListViewAdapter adapter;
	private PopupWindow deletePopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;

	private List<Item> itemList = new ArrayList<Item>();
	private List<Item> showList = new ArrayList<Item>();
	private List<Tag> tagList = new ArrayList<Tag>();
	private List<Category> categoryList = new ArrayList<Category>();

	private int sortType = SORT_NULL;
	private boolean sortReverse = false;
	private int filterType = FILTER_TYPE_ALL;
	private int filterStatus = FILTER_STATUS_ALL;
	private boolean[] tagCheck;
	private boolean[] categoryCheck;
	private List<Tag> filterTagList = new ArrayList<Tag>();
	private List<Category> filterCategoryList = new ArrayList<Category>();
	
	private int tempFilterType = FILTER_TYPE_ALL;
	private int tempFilterStatus = FILTER_STATUS_ALL;
	private int tempSortType = SORT_NULL;
	private boolean[] tempTagCheck;
	private boolean[] tempCategoryCheck;
	
	private boolean hasInit = false;
	private int itemIndex;
	
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
			ReimProgressDialog.show();
			refreshItemListView();
			ReimProgressDialog.dismiss();
			syncItems();
		}
	}

	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		tagCheck = new boolean[tagList.size()];
		for (int i = 0; i < tagCheck.length; i++)
		{
			tagCheck[i] = false;
		}
		tempTagCheck = tagCheck;
		
		categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
		categoryCheck = new boolean[categoryList.size()];
		for (int i = 0; i < categoryCheck.length; i++)
		{
			categoryCheck[i] = false;
		}
		tempCategoryCheck = categoryCheck;

		itemList.clear();
		itemList.addAll(readItemList());
		filterItemList();
		
		if (PhoneUtils.isNetworkConnected())
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
		initDeleteView();
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
				if (deletePopupWindow == null || !deletePopupWindow.isShowing())
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
			}
		});
		itemListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				itemIndex = position - 1;
				showDeleteWindow();
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
				showFilterWindow();
			}
		});
		
		View filterView = View.inflate(getActivity(), R.layout.window_reim_filter, null);

		final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
		final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);		
		final RadioButton sortConsumedDateRadio = (RadioButton)filterView.findViewById(R.id.sortConsumedDateRadio);	
		final SegmentedGroup sortRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.sortRadioGroup);
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
		final SegmentedGroup filterTypeRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.filterTypeRadioGroup);
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
		final SegmentedGroup filterStatusRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.filterStatusRadioGroup);
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

		tagLayout = (LinearLayout) filterView.findViewById(R.id.tagLayout);
		refreshTagView();
		
		categoryLayout = (LinearLayout) filterView.findViewById(R.id.categoryLayout);
		refreshCategoryView();
		
		ImageView confirmImageView = (ImageView)filterView.findViewById(R.id.confirmImageView);
		confirmImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				sortReverse = sortType == tempSortType ? !sortReverse : false;
				sortType = tempSortType;
				
				filterType = tempFilterType;
				filterStatus = tempFilterStatus;
				
				tagCheck = tempTagCheck;
				categoryCheck = tempCategoryCheck;
				
				filterTagList.clear();
				for (int i = 0; i < tagCheck.length; i++)
				{
					if (tagCheck[i])
					{
						filterTagList.add(tagList.get(i));
					}
				}
				
				filterCategoryList.clear();
				for (int i = 0; i < categoryCheck.length; i++)
				{
					if (categoryCheck[i])
					{
						filterCategoryList.add(categoryList.get(i));
					}
				}

				filterPopupWindow.dismiss();
				ReimProgressDialog.show();
				refreshItemListView();
				ReimProgressDialog.dismiss();
			}
		});

		ImageView cancelImageView = (ImageView)filterView.findViewById(R.id.cancelImageView);
		cancelImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				switch (sortType)
				{
					case SORT_NULL:
						sortRadioGroup.check(sortNullRadio.getId());
						break;
					case SORT_AMOUNT:
						sortRadioGroup.check(sortAmountRadio.getId());
						break;
					case SORT_CONSUMED_DATE:
						sortRadioGroup.check(sortConsumedDateRadio.getId());
						break;
					default:
						break;
				}
				
				switch (filterType)
				{
					case FILTER_TYPE_ALL:
						filterTypeRadioGroup.check(filterTypeAllRadio.getId());
						break;
					case FILTER_TYPE_PROVE_AHEAD:
						filterTypeRadioGroup.check(filterProveAheadRadio.getId());
						break;
					case FILTER_TYPE_CONSUMED:
						filterTypeRadioGroup.check(filterConsumedRadio.getId());
						break;
					default:
						break;
				}
				
				switch (filterStatus)
				{
					case FILTER_STATUS_ALL:
						filterStatusRadioGroup.check(filterStatusAllRadio.getId());
						break;
					case FILTER_STATUS_FREE:
						filterStatusRadioGroup.check(filterFreeRadio.getId());
						break;
					case FILTER_STATUS_ADDED:
						filterStatusRadioGroup.check(filterAddedRadio.getId());
						break;
					default:
						break;
				}
				
				tempTagCheck = tagCheck;
				refreshTagView();
				
				tempCategoryCheck = categoryCheck;
				refreshCategoryView();
				
				filterPopupWindow.dismiss();
			}
		});
		
		filterPopupWindow = ViewUtils.constructTopPopupWindow(getActivity(), filterView);
	}

	private void initDeleteView()
	{
		View deleteView = View.inflate(getActivity(), R.layout.window_delete, null);
		
		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
				
				final Item localItem = showList.get(itemIndex);
				Report report = localItem.getBelongReport();

				if (report != null && !report.isEditable())
				{
					ViewUtils.showToast(getActivity(), R.string.error_delete_item_submitted);
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
															else if (!PhoneUtils.isNetworkConnected())
															{
																ViewUtils.showToast(getActivity(), R.string.error_delete_network_unavailable);
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
		deleteButton = ViewUtils.resizeWindowButton(deleteButton);
		
		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
			}
		});
		cancelButton = ViewUtils.resizeWindowButton(cancelButton);
		
		deletePopupWindow = ViewUtils.constructBottomPopupWindow(getActivity(), deleteView);
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
			
			if (!filterTagList.isEmpty() && !item.containsSpecificTags(filterTagList))
			{
				continue;
			}
			
			if (!filterCategoryList.isEmpty() && !item.containsCategory(filterCategoryList))
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

	private void refreshTagView()
	{
		tagLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 32);
		int tagVerticalInterval = PhoneUtils.dpToPixel(getResources(), 16);
		int tagHorizontalInterval = PhoneUtils.dpToPixel(getResources(), 10);
		int padding = PhoneUtils.dpToPixel(getResources(), 24);
		int textSize = PhoneUtils.dpToPixel(getResources(), 16);

		int space = 0;
		LinearLayout layout = new LinearLayout(getActivity());
		for (int i = 0; i < tagList.size(); i++)
		{
			String name = tagList.get(i).getName();
			
			int layoutID = tempTagCheck[i] ? R.layout.grid_tag : R.layout.grid_tag_unselected;
			View view = View.inflate(getActivity(), layoutID, null);

			final int index = i;
			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(name);
			nameTextView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_TAG");
					tempTagCheck[index] = !tempTagCheck[index];
					refreshTagView();
				}
			});
			
			Paint textPaint = new Paint();
			textPaint.setTextSize(textSize);
			Rect textRect = new Rect();
			textPaint.getTextBounds(name, 0, name.length(), textRect);
			int width = textRect.width() + padding;
			
			if (space - width - tagHorizontalInterval <= 0)
			{
				layout = new LinearLayout(getActivity());
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = tagVerticalInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				tagLayout.addView(layout);
				
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				layout.addView(view, params);
				space = layoutMaxLength - width;
			}
			else
			{
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.leftMargin = tagHorizontalInterval;
				layout.addView(view, params);
				space -= width + tagHorizontalInterval;
			}
		}
	}
	
	private void refreshCategoryView()
	{
		categoryLayout.removeAllViews();
		
		int selectedColor = ViewUtils.getColor(R.color.major_dark);
		int unselectedColor = ViewUtils.getColor(R.color.font_major_dark);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		int layoutMaxLength = metrics.widthPixels - PhoneUtils.dpToPixel(getResources(), 32);
		int iconWidth = PhoneUtils.dpToPixel(getResources(), 50);
		int iconVerticalInterval = PhoneUtils.dpToPixel(getResources(), 16);
		int iconHorizontalInterval = PhoneUtils.dpToPixel(getResources(), 18);
		int iconMaxCount = (layoutMaxLength + iconHorizontalInterval) / (iconWidth + iconHorizontalInterval);
		iconHorizontalInterval = (layoutMaxLength - iconWidth * iconMaxCount) / (iconMaxCount - 1);

		LinearLayout layout = new LinearLayout(getActivity());
		for (int i = 0; i < categoryList.size(); i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(getActivity());
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconVerticalInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				categoryLayout.addView(layout);
			}
			
			Category category = categoryList.get(i);

			final int index = i;
			View view = View.inflate(getActivity(), R.layout.grid_category, null);
			view.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					tempCategoryCheck[index] = !tempCategoryCheck[index];
					refreshCategoryView();
				}
			});
			
			ImageView iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
			
			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(category.getName());
			
			if (tempCategoryCheck[i])
			{
				iconImageView.setImageResource(R.drawable.icon_chosen);
				nameTextView.setTextColor(selectedColor);
			}
			else
			{
				Bitmap icon = BitmapFactory.decodeFile(category.getIconPath());
				if (icon != null)
				{
					iconImageView.setImageBitmap(icon);		
				}
				nameTextView.setTextColor(unselectedColor);
			}			
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconHorizontalInterval;
			
			layout.addView(view, params);
		}
	}
	
    private void showFilterWindow()
    {
		filterPopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		filterPopupWindow.update();
    }
    
    private void showDeleteWindow()
    {    	
		deletePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
		deletePopupWindow.update();
		
		ViewUtils.dimBackground(getActivity());
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
					String iconPath = PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
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
		ReimProgressDialog.show();
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(getActivity(), R.string.prompt_delete_failed);
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
			ReimProgressDialog.dismiss();
			ViewUtils.showToast(getActivity(), R.string.prompt_delete_succeed);
		}
		else
		{
			ReimProgressDialog.dismiss();
			ViewUtils.showToast(getActivity(), R.string.prompt_delete_failed);
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
					int prompt = SyncUtils.isSyncOnGoing ? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
					ViewUtils.showToast(getActivity(), prompt);
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
					int prompt = SyncUtils.isSyncOnGoing ? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
					ViewUtils.showToast(getActivity(), prompt);
				}
			});
		}	
	}
}