package com.rushucloud.reim;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.item.EditItemActivity;
import com.rushucloud.reim.item.SearchItemActivity;
import com.rushucloud.reim.item.ShowItemActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.base.Category;
import classes.base.Item;
import classes.base.Report;
import classes.base.Tag;
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
import netUtils.HttpConnectionCallback;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.request.DownloadImageRequest;
import netUtils.request.item.DeleteItemRequest;
import netUtils.response.DownloadImageResponse;
import netUtils.response.item.DeleteItemResponse;

public class ReimFragment extends Fragment
{
	private static final int FILTER_TYPE_ALL = 0;
	private static final int FILTER_TYPE_CONSUMED = 1;
    private static final int FILTER_TYPE_BUDGET = 2;
    private static final int FILTER_TYPE_BORROWING = 3;
	private static final int FILTER_STATUS_ALL = 0;
	private static final int FILTER_STATUS_FREE = 1;
	private static final int FILTER_STATUS_ADDED = 2;
	private static final int SORT_CONSUMED_DATE = 0;
	private static final int SORT_AMOUNT = 1;
	
	private View view;
	private PopupWindow filterPopupWindow;
	private ImageView sortDateImageView;
	private ImageView sortAmountImageView;
	private RotateAnimation rotateAnimation;
	private RotateAnimation rotateReverseAnimation;
	private RelativeLayout noResultLayout;
	private LinearLayout tagLayout;
	private LinearLayout categoryLayout;
	private XListView itemListView;
	private ItemListViewAdapter adapter;
	private PopupWindow deletePopupWindow;

	private AppPreference appPreference;
	private DBManager dbManager;

	private List<Item> itemList = new ArrayList<>();
	private List<Item> showList = new ArrayList<>();
	private List<Tag> tagList = new ArrayList<>();
	private List<Category> categoryList = new ArrayList<>();

	private int sortType = SORT_CONSUMED_DATE;
	private boolean sortReverse = false;
	private int filterType = FILTER_TYPE_ALL;
	private int filterStatus = FILTER_STATUS_ALL;
	private boolean[] tagCheck;
	private boolean[] categoryCheck;
	private List<Tag> filterTagList = new ArrayList<>();
	private List<Category> filterCategoryList = new ArrayList<>();
	
	private int tempFilterType = FILTER_TYPE_ALL;
	private boolean tempSortReverse = false;
	private int tempFilterStatus = FILTER_STATUS_ALL;
	private int tempSortType = SORT_CONSUMED_DATE;
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
		itemListView.setXListViewListener(new IXListViewListener()
        {
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
//					String prompt = SyncUtils.isSyncOnGoing? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
                            int prompt = SyncUtils.isSyncOnGoing? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
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
//					String prompt = SyncUtils.isSyncOnGoing? "正在同步中" : "未打开同步开关或未打开Wifi，无法刷新";
                            int prompt = SyncUtils.isSyncOnGoing? R.string.prompt_sync_ongoing : R.string.error_refresh_network_unavailable;
                            ViewUtils.showToast(getActivity(), prompt);
                        }
                    });
                }
            }
        });
		itemListView.setPullRefreshEnable(true);
		itemListView.setPullLoadEnable(false);
		itemListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastSyncTime()));
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (deletePopupWindow == null || !deletePopupWindow.isShowing() && position > 0)
				{
					Item item = showList.get(position - 1);
                    if (item.getConsumedDateGroup().isEmpty())
                    {
                        if (item.getBelongReport() == null || item.getBelongReport().isEditable())
                        {
                            Intent intent = new Intent(getActivity(), EditItemActivity.class);
                            intent.putExtra("itemLocalID", item.getLocalID());
                            intent.putExtra("fromReim", true);
                            ViewUtils.goForward(getActivity(), intent);
                        }
                        else
                        {
                            Intent intent = new Intent(getActivity(), ShowItemActivity.class);
                            intent.putExtra("itemLocalID", item.getLocalID());
                            ViewUtils.goForward(getActivity(), intent);
                        }
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
        final ImageView filterImageView = (ImageView) view.findViewById(R.id.filterImageView);
        filterImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CLICK");
                showFilterWindow();
            }
        });

		noResultLayout = (RelativeLayout) view.findViewById(R.id.noResultLayout);
		
		rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setDuration(200);
		rotateAnimation.setFillAfter(true);
		
		rotateReverseAnimation = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateReverseAnimation.setDuration(200);
		rotateReverseAnimation.setFillAfter(true);
		
		View filterView = View.inflate(getActivity(), R.layout.window_reim_filter, null);

		sortDateImageView = (ImageView) filterView.findViewById(R.id.sortDateImageView);
		sortDateImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				reverseSortDateImageView();
			}
		});
		sortAmountImageView = (ImageView) filterView.findViewById(R.id.sortAmountImageView);
		sortAmountImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				reverseSortAmountImageView();
			}
		});
		
		final RadioButton sortConsumedDateRadio = (RadioButton) filterView.findViewById(R.id.sortConsumedDateRadio);
        final RadioButton sortAmountRadio = (RadioButton) filterView.findViewById(R.id.sortAmountRadio);
		sortConsumedDateRadio.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_TIME");
				sortAmountRadio.setChecked(false);
				sortConsumedDateRadio.setChecked(true);
				if (tempSortType != SORT_CONSUMED_DATE)
				{
					tempSortReverse = false;
					sortAmountImageView.clearAnimation();
					sortAmountImageView.setVisibility(View.GONE);
					
					tempSortType = SORT_CONSUMED_DATE;
					sortDateImageView.setVisibility(View.VISIBLE);
				}
				else
				{
					reverseSortDateImageView();
				}
			}
		});
		sortAmountRadio.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_AMOUNT");
				sortAmountRadio.setChecked(true);
				sortConsumedDateRadio.setChecked(false);
				if (tempSortType != SORT_AMOUNT)
				{
					tempSortReverse = false;
					sortDateImageView.clearAnimation();
					sortDateImageView.setVisibility(View.GONE);
					
					tempSortType = SORT_AMOUNT;
					sortAmountImageView.setVisibility(View.VISIBLE);
				}
				else
				{
					reverseSortAmountImageView();
				}
			}
		});

		final RadioButton filterTypeAllRadio = (RadioButton) filterView.findViewById(R.id.filterTypeAllRadio);
        final RadioButton filterConsumedRadio = (RadioButton) filterView.findViewById(R.id.filterConsumedRadio);
        final RadioButton filterBudgetRadio = (RadioButton) filterView.findViewById(R.id.filterBudgetRadio);
        final RadioButton filterBorrowingRadio = (RadioButton) filterView.findViewById(R.id.filterBorrowingRadio);
		final SegmentedGroup filterTypeRadioGroup = (SegmentedGroup) filterView.findViewById(R.id.filterTypeRadioGroup);
		filterTypeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if (checkedId == filterTypeAllRadio.getId())
				{
					tempFilterType = FILTER_TYPE_ALL;
				}
				else if (checkedId == filterConsumedRadio.getId())
				{
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_PAY");
					tempFilterType = FILTER_TYPE_CONSUMED;
				}
                else if (checkedId == filterBudgetRadio.getId())
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_PROVE_AHEAD");
                    tempFilterType = FILTER_TYPE_BUDGET;
                }
                else if (checkedId == filterBorrowingRadio.getId())
                {
                    MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_PROVE_AHEAD");
                    tempFilterType = FILTER_TYPE_BORROWING;
                }
			}
		});

		final RadioButton filterStatusAllRadio = (RadioButton) filterView.findViewById(R.id.filterStatusAllRadio);
		final RadioButton filterFreeRadio = (RadioButton) filterView.findViewById(R.id.filterFreeRadio);
		final RadioButton filterAddedRadio = (RadioButton) filterView.findViewById(R.id.filterAddedRadio);			
		final SegmentedGroup filterStatusRadioGroup = (SegmentedGroup) filterView.findViewById(R.id.filterStatusRadioGroup);
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
		categoryLayout = (LinearLayout) filterView.findViewById(R.id.categoryLayout);
		
		ImageView confirmImageView = (ImageView) filterView.findViewById(R.id.confirmImageView);
		confirmImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_SUBMIT");
				
				sortType = tempSortType;
				sortReverse = tempSortReverse;
				
				sortDateImageView.clearAnimation();
				sortAmountImageView.clearAnimation();
				
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

                int filterImage = filterType != FILTER_TYPE_ALL || filterStatus != FILTER_STATUS_ALL ||
                        !filterTagList.isEmpty() || !filterCategoryList.isEmpty()? R.drawable.filter_full : R.drawable.filter_empty;
                filterImageView.setImageResource(filterImage);

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
				MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CANCEL");
				
				switch (sortType)
				{
					case SORT_CONSUMED_DATE:
					{
						sortConsumedDateRadio.setChecked(true);
						sortAmountRadio.setChecked(false);
						sortDateImageView.clearAnimation();
						sortDateImageView.setVisibility(View.VISIBLE);
						sortAmountImageView.clearAnimation();
						sortAmountImageView.setVisibility(View.GONE);
						break;
					}
					case SORT_AMOUNT:
					{
						sortConsumedDateRadio.setChecked(false);
						sortAmountRadio.setChecked(true);
						sortDateImageView.clearAnimation();
						sortDateImageView.setVisibility(View.GONE);
						sortAmountImageView.clearAnimation();
						sortAmountImageView.setVisibility(View.VISIBLE);
						break;
					}
					default:
						break;
				}

				tempSortReverse = sortReverse;
				
				switch (filterType)
				{
					case FILTER_TYPE_ALL:
						filterTypeRadioGroup.check(filterTypeAllRadio.getId());
						break;
                    case FILTER_TYPE_CONSUMED:
                        filterTypeRadioGroup.check(filterConsumedRadio.getId());
                        break;
					case FILTER_TYPE_BUDGET:
						filterTypeRadioGroup.check(filterBudgetRadio.getId());
						break;
                    case FILTER_TYPE_BORROWING:
                        filterTypeRadioGroup.check(filterBorrowingRadio.getId());
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
		
		filterPopupWindow = ViewUtils.buildTopPopupWindow(getActivity(), filterView);
	}

	private void initDeleteView()
	{
		View deleteView = View.inflate(getActivity(), R.layout.window_delete, null);
		
		Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_EDIT_ITEM_DELETE");
				
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
																deleteLocalItem(localItem.getLocalID());
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
		
		Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				deletePopupWindow.dismiss();
			}
		});
		
		deletePopupWindow = ViewUtils.buildBottomPopupWindow(getActivity(), deleteView);
	}
	
	private void initSearchView()
	{		
		ImageView searchImageView = (ImageView) getActivity().findViewById(R.id.searchImageView);
		searchImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(getActivity(), "UMENG_SEARCH_LOCAL");
                ViewUtils.goForward(getActivity(), SearchItemActivity.class);
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
			if (filterType == FILTER_TYPE_CONSUMED && item.getType() != Item.TYPE_REIM)
			{
				continue;
			}
            if (filterType == FILTER_TYPE_BUDGET && item.getType() != Item.TYPE_BUDGET)
            {
                continue;
            }
            if (filterType == FILTER_TYPE_BORROWING && item.getType() != Item.TYPE_BORROWING)
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

        switch (sortType)
        {
            case SORT_CONSUMED_DATE:
            {
                Item.sortByConsumedDate(showList);
                if (sortReverse)
                {
                    Collections.reverse(showList);
                }
                buildItemListByConsumedDate();
                break;
            }
            case SORT_AMOUNT:
            {
                Item.sortByAmount(showList);
                if (sortReverse)
                {
                    Collections.reverse(showList);
                }
                break;
            }
            default:
                break;
        }
	}
	
	private void buildItemListByConsumedDate()
	{
		List<String> timeList = new ArrayList<>();
		for (Item item : showList)
		{
			String time = Utils.secondToStringUpToDay(item.getConsumedDate());
			if (!timeList.contains(time))
			{
				timeList.add(time);
			}
		}
		
		List<List<Item>> collectList = new ArrayList<>();
		for (String time : timeList)
		{
			Item item = new Item();
			item.setConsumedDateGroup(time);
			
			List<Item> subList = new ArrayList<>();
			subList.add(item);
			
			collectList.add(subList);
		}
		
		for (Item item : showList)
		{
			String time = Utils.secondToStringUpToDay(item.getConsumedDate());
			int index = timeList.indexOf(time);
			
			List<Item> subList = collectList.get(index);
			subList.add(item);
		}
		
		showList.clear();
		for (List<Item> subList : collectList)
		{
			for (Item item : subList)
			{
				showList.add(item);
			}
		}
	}
	
	private void reverseSortDateImageView()
	{
		tempSortReverse = !tempSortReverse;
		if (!tempSortReverse) // status before change
		{
			sortDateImageView.startAnimation(rotateReverseAnimation);
		}
		else
		{
			sortDateImageView.startAnimation(rotateAnimation);
		}		
	}

	private void reverseSortAmountImageView()
	{
		tempSortReverse = !tempSortReverse;
		if (!tempSortReverse) // status before change
		{
			sortAmountImageView.startAnimation(rotateReverseAnimation);
		}
		else
		{
			sortAmountImageView.startAnimation(rotateAnimation);
		}
	}
	
 	private void refreshItemListView()
	{
        SparseBooleanArray array = new SparseBooleanArray();
        for (int i = 0; i < tagCheck.length; i++)
        {
            array.put(tagList.get(i).getServerID(), tagCheck[i]);
        }
        tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
        tagCheck = new boolean[tagList.size()];
        for (int i = 0; i < tagCheck.length; i++)
        {
            tagCheck[i] = array.get(tagList.get(i).getServerID(), false);
        }
        tempTagCheck = tagCheck;

        array.clear();
        for (int i = 0; i < categoryCheck.length; i++)
        {
            array.put(categoryList.get(i).getServerID(), categoryCheck[i]);
        }
        categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
        categoryCheck = new boolean[categoryList.size()];
        for (int i = 0; i < categoryCheck.length; i++)
        {
            categoryCheck[i] = array.get(categoryList.get(i).getServerID(), false);
        }
        tempCategoryCheck = categoryCheck;

		itemList.clear();
		itemList.addAll(readItemList());
		filterItemList();
		adapter.setItemList(showList);
		adapter.notifyDataSetChanged();

        int visibility = (filterType != FILTER_STATUS_ALL || filterStatus != FILTER_STATUS_ALL || !filterCategoryList.isEmpty() ||
                !filterTagList.isEmpty()) && showList.isEmpty()? View.VISIBLE : View.GONE;
        noResultLayout.setVisibility(visibility);
	}

	private void refreshTagView()
	{
		tagLayout.removeAllViews();

		int layoutMaxLength = ViewUtils.getPhoneWindowWidth(getActivity()) - ViewUtils.dpToPixel(32);
		int tagVerticalInterval = ViewUtils.dpToPixel(16);
		int tagHorizontalInterval = ViewUtils.dpToPixel(10);
		int padding = ViewUtils.dpToPixel(24);
		int textSize = ViewUtils.dpToPixel(16);

		int space = 0;
		LinearLayout layout = new LinearLayout(getActivity());
		for (int i = 0; i < tagList.size(); i++)
		{
			String name = tagList.get(i).getName();
			
			int layoutID = tempTagCheck[i]? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
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

		int layoutMaxLength = ViewUtils.getPhoneWindowWidth(getActivity()) - ViewUtils.dpToPixel(32);
		int iconWidth = ViewUtils.dpToPixel(50);
		int iconVerticalInterval = ViewUtils.dpToPixel(16);
		int iconHorizontalInterval = ViewUtils.dpToPixel(18);
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
					MobclickAgent.onEvent(getActivity(), "UMENG_SHEET_CATEGORY");
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
                ViewUtils.setImageViewBitmap(category, iconImageView);
				nameTextView.setTextColor(unselectedColor);
			}			
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconHorizontalInterval;
			
			layout.addView(view, params);
		}
	}
	
    private void showFilterWindow()
    {
    	if (sortReverse && sortType == SORT_CONSUMED_DATE)
		{
    		sortDateImageView.startAnimation(rotateAnimation);
		}
    	else if (sortReverse && sortType == SORT_AMOUNT)
    	{
    		sortAmountImageView.startAnimation(rotateAnimation);    		
		}

        refreshTagView();
        refreshCategoryView();

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
					PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
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
							deleteLocalItem(item.getLocalID());
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
							ViewUtils.showToast(getActivity(), R.string.failed_to_delete);
						}
					});
				}
			}
		});
	}

	private void deleteLocalItem(int itemLocalID)
	{
		if (dbManager.deleteItem(itemLocalID))
		{
			refreshItemListView();
			ReimProgressDialog.dismiss();
			ViewUtils.showToast(getActivity(), R.string.succeed_in_deleting);
		}
		else
		{
			ReimProgressDialog.dismiss();
			ViewUtils.showToast(getActivity(), R.string.failed_to_delete);
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
}