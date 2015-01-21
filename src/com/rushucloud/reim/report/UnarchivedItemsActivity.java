package com.rushucloud.reim.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import classes.Category;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.adapter.ReportItemListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.SegmentedGroup;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UnarchivedItemsActivity extends Activity implements OnClickListener
{
	private static final int SORT_NULL = 0;	
	private static final int SORT_AMOUNT = 1;	
	private static final int SORT_CONSUMED_DATE = 2;	

	private PopupWindow filterPopupWindow;
	private LinearLayout tagLayout;
	private LinearLayout categoryLayout;
	private TextView consumedTextView;
	private TextView proveAheadTextView;
	private TextView itemCountTextView;
	private TextView warningTextView;
	private ListView itemListView;
	private ReportItemListViewAdapter adapter;
	
	private AppPreference appPreference;
	private static DBManager dbManager;
	
	private Report report;
	private ArrayList<Integer> chosenItemIDList = null;
	private boolean isProveAhead;
	private int tabIndex = 0;
	private List<Tag> tagList = new ArrayList<Tag>();
	private List<Category> categoryList = new ArrayList<Category>();
	
	private List<Item> consumedItemList;
	private List<Item> consumedShowList;
	private ArrayList<Integer> consumedChosenList = null;

	private int consumedSortType = SORT_NULL;
	private boolean consumedSortReverse = false;
	private boolean[] consumedTagCheck;
	private boolean[] consumedCategoryCheck;
	private List<Tag> consumedFilterTagList = new ArrayList<Tag>();
	private List<Category> consumedFilterCategoryList = new ArrayList<Category>();
	
	private int consumedTempSortType = SORT_NULL;
	private boolean[] consumedTempTagCheck;
	private boolean[] consumedTempCategoryCheck;
	
	private List<Item> proveAheadItemList;
	private List<Item> proveAheadShowList;
	private ArrayList<Integer> proveChosenList = null;

	private int proveSortType = SORT_NULL;
	private boolean proveSortReverse = false;
	private boolean[] proveTagCheck;
	private boolean[] proveCategoryCheck;
	private List<Tag> proveFilterTagList = new ArrayList<Tag>();
	private List<Category> proveFilterCategoryList = new ArrayList<Category>();
	
	private int proveTempSortType = SORT_NULL;
	private boolean[] proveTempTagCheck;
	private boolean[] proveTempCategoryCheck;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_unarchived_items);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("UnarchivedItemsActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshData();
		refreshView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("UnarchivedItemsActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Bundle bundle = new Bundle();
			bundle.putSerializable("report", report);
			bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
			Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{		
		Bundle bundle = this.getIntent().getExtras();
		report = (Report)bundle.getSerializable("report");
		chosenItemIDList = bundle.getIntegerArrayList("chosenItemIDList");
		if (chosenItemIDList == null)
		{
			chosenItemIDList = new ArrayList<Integer>();
		}

		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();

		isProveAhead = report.isProveAhead();
		tabIndex = Utils.booleanToInt(isProveAhead);
		if (tabIndex == 0)
		{
			consumedChosenList = new ArrayList<Integer>(chosenItemIDList);
			proveChosenList = new ArrayList<Integer>();
		}
		else
		{
			proveChosenList = new ArrayList<Integer>(chosenItemIDList);
			consumedChosenList = new ArrayList<Integer>();
		}
		
		tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
		consumedTagCheck = new boolean[tagList.size()];
		proveTagCheck = new boolean[tagList.size()];
		for (int i = 0; i < consumedTagCheck.length; i++)
		{
			consumedTagCheck[i] = false;
			proveTagCheck[i] = false;
		}
		consumedTempTagCheck = consumedTagCheck;
		proveTempTagCheck = proveTagCheck;
		
		categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
		consumedCategoryCheck = new boolean[categoryList.size()];
		proveCategoryCheck = new boolean[categoryList.size()];
		for (int i = 0; i < consumedCategoryCheck.length; i++)
		{
			consumedCategoryCheck[i] = false;
			proveCategoryCheck[i] = false;
		}
		consumedTempCategoryCheck = consumedCategoryCheck;
		proveTempCategoryCheck = proveCategoryCheck;
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView filterImageView = (ImageView) findViewById(R.id.filterImageView);
		filterImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showFilterWindow();
			}
		});

		consumedTextView = (TextView)findViewById(R.id.consumedTextView);
		consumedTextView.setOnClickListener(this);
		proveAheadTextView = (TextView)findViewById(R.id.proveAheadTextView);
		proveAheadTextView.setOnClickListener(this);
		
		itemCountTextView = (TextView)findViewById(R.id.itemCountTextView);
		
		TextView confirmTextView = (TextView)findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					report.setIsProveAhead(isProveAhead);
					chosenItemIDList = tabIndex == 0 ? consumedChosenList : proveChosenList;
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
					Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		warningTextView = (TextView)findViewById(R.id.warningTextView);
		
		itemListView = (ListView) findViewById(R.id.itemListView);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (position == 0)
				{
					Intent intent = new Intent(UnarchivedItemsActivity.this, EditItemActivity.class);
					intent.putExtra("fromReim", true);
					startActivity(intent);
				}
				else
				{
					if (tabIndex == 0)
					{
						int localID = consumedShowList.get(position - 1).getLocalID();
						if (consumedChosenList.contains(localID))
						{
							consumedChosenList.remove(Integer.valueOf(localID));
						}
						else
						{
							consumedChosenList.add(localID);
						}
						isProveAhead = false;
						proveChosenList.clear();
						itemCountTextView.setText(Integer.toString(consumedChosenList.size()));
						adapter.setChosenList(consumedChosenList);
					}
					else
					{
						int localID = proveAheadShowList.get(position - 1).getLocalID();
						if (proveChosenList.contains(localID))
						{
							proveChosenList.remove(Integer.valueOf(localID));
						}
						else
						{
							proveChosenList.add(localID);
						}
						isProveAhead = true;
						consumedChosenList.clear();
						itemCountTextView.setText(Integer.toString(proveChosenList.size()));
						adapter.setChosenList(proveChosenList);
					}
					adapter.notifyDataSetChanged();					
				}
			}
		});
	
		initFilterView();
	}

	private void initFilterView()
	{
		View filterView = View.inflate(this, R.layout.window_report_items_filter, null);
		tagLayout = (LinearLayout) filterView.findViewById(R.id.tagLayout);		
		categoryLayout = (LinearLayout) filterView.findViewById(R.id.categoryLayout);
		
		filterPopupWindow = ViewUtils.constructTopPopupWindow(this, filterView);
	}

	private void refreshView()
	{
		ReimProgressDialog.show();
		
		if (adapter == null)
		{
			if (isProveAhead)
			{
				adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, proveAheadShowList, proveChosenList);		
			}
			else
			{
				adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, consumedShowList, consumedChosenList);
			}
			itemListView.setAdapter(adapter);			
		}
		
		if (tabIndex == 0)
		{
			itemCountTextView.setText(Integer.toString(consumedChosenList.size()));
			
			consumedTextView.setTextColor(getResources().getColor(R.color.major_light));
			proveAheadTextView.setTextColor(getResources().getColor(R.color.hint_white));
			
			if (consumedItemList.isEmpty())
			{
				itemListView.setVisibility(View.INVISIBLE);
				warningTextView.setVisibility(View.VISIBLE);
			}
			else
			{
				itemListView.setVisibility(View.VISIBLE);
				warningTextView.setVisibility(View.INVISIBLE);
				adapter.set(consumedShowList, consumedChosenList);
				adapter.notifyDataSetChanged();		
			}
		}
		else
		{
			itemCountTextView.setText(Integer.toString(proveChosenList.size()));
			
			consumedTextView.setTextColor(getResources().getColor(R.color.hint_white));
			proveAheadTextView.setTextColor(getResources().getColor(R.color.major_light));
			
			if (proveAheadItemList.isEmpty())
			{
				itemListView.setVisibility(View.INVISIBLE);
				warningTextView.setVisibility(View.VISIBLE);
			}
			else
			{
				itemListView.setVisibility(View.VISIBLE);
				warningTextView.setVisibility(View.INVISIBLE);
				adapter.set(proveAheadShowList, proveChosenList);
				adapter.notifyDataSetChanged();		
			}
		}

		ReimProgressDialog.dismiss();
	}

	private void refreshTagView()
	{
		tagLayout.removeAllViews();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics);
		int tagVerticalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		int tagHorizontalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, metrics);
		int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics);

		int space = 0;
		LinearLayout layout = new LinearLayout(UnarchivedItemsActivity.this);
		for (int i = 0; i < tagList.size(); i++)
		{
			String name = tagList.get(i).getName();
			
			View view;
			if (tabIndex == 0)
			{
				int layoutID = consumedTempTagCheck[i] ? R.layout.grid_tag : R.layout.grid_tag_unselected;
				view = View.inflate(UnarchivedItemsActivity.this, layoutID, null);				
			}
			else
			{
				int layoutID = proveTempTagCheck[i] ? R.layout.grid_tag : R.layout.grid_tag_unselected;
				view = View.inflate(UnarchivedItemsActivity.this, layoutID, null);
			}

			final int index = i;
			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(name);
			nameTextView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					MobclickAgent.onEvent(UnarchivedItemsActivity.this, "UMENG_SHEET_TAG");
					if (tabIndex == 0)
					{
						consumedTempTagCheck[index] = !consumedTempTagCheck[index];			
					}
					else
					{
						proveTempTagCheck[index] = !proveTempTagCheck[index];
					}
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
				layout = new LinearLayout(UnarchivedItemsActivity.this);
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
		
		int selectedColor = getResources().getColor(R.color.major_dark);
		int unselectedColor = getResources().getColor(R.color.font_major_dark);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		int layoutMaxLength = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics);
		int iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
		int iconVerticalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		int iconHorizontalInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		int iconMaxCount = (layoutMaxLength + iconHorizontalInterval) / (iconWidth + iconHorizontalInterval);
		iconHorizontalInterval = (layoutMaxLength - iconWidth * iconMaxCount) / (iconMaxCount - 1);

		LinearLayout layout = new LinearLayout(UnarchivedItemsActivity.this);
		for (int i = 0; i < categoryList.size(); i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(UnarchivedItemsActivity.this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconVerticalInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				categoryLayout.addView(layout);
			}
			
			Category category = categoryList.get(i);

			final int index = i;
			View view = View.inflate(UnarchivedItemsActivity.this, R.layout.grid_category, null);
			view.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					if (tabIndex == 0)
					{
						consumedTempCategoryCheck[index] = !consumedTempCategoryCheck[index];			
					}
					else
					{
						proveTempCategoryCheck[index] = !proveTempCategoryCheck[index];
					}
					refreshCategoryView();
				}
			});
			
			ImageView iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
			
			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(category.getName());
			
			if (tabIndex == 0)
			{
				if (consumedTempCategoryCheck[i])
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
			}
			else
			{
				if (proveTempCategoryCheck[i])
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
			}
			
			LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
			params.rightMargin = iconHorizontalInterval;
			
			layout.addView(view, params);
		}
	}

    private void showFilterWindow()
    {
    	View filterView = filterPopupWindow.getContentView();

		final RadioButton sortNullRadio = (RadioButton)filterView.findViewById(R.id.sortNullRadio);
		final RadioButton sortAmountRadio = (RadioButton)filterView.findViewById(R.id.sortAmountRadio);		
		final RadioButton sortConsumedDateRadio = (RadioButton)filterView.findViewById(R.id.sortConsumedDateRadio);	
		final SegmentedGroup sortRadioGroup = (SegmentedGroup)filterView.findViewById(R.id.sortRadioGroup);
		sortRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if (tabIndex == 0)
				{
					if (checkedId == sortNullRadio.getId())
					{
						consumedTempSortType = SORT_NULL;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						MobclickAgent.onEvent(UnarchivedItemsActivity.this, "UMENG_SHEET_AMOUNT");
						consumedTempSortType = SORT_AMOUNT;
					}
					else if (checkedId == sortConsumedDateRadio.getId())
					{
						MobclickAgent.onEvent(UnarchivedItemsActivity.this, "UMENG_SHEET_TIME");
						consumedTempSortType = SORT_CONSUMED_DATE;
					}					
				}
				else
				{
					if (checkedId == sortNullRadio.getId())
					{
						proveTempSortType = SORT_NULL;
					}
					else if (checkedId == sortAmountRadio.getId())
					{
						MobclickAgent.onEvent(UnarchivedItemsActivity.this, "UMENG_SHEET_AMOUNT");
						proveTempSortType = SORT_AMOUNT;
					}
					else if (checkedId == sortConsumedDateRadio.getId())
					{
						MobclickAgent.onEvent(UnarchivedItemsActivity.this, "UMENG_SHEET_TIME");
						proveTempSortType = SORT_CONSUMED_DATE;
					}					
				}
			}
		});
		
		ImageView confirmImageView = (ImageView)filterView.findViewById(R.id.confirmImageView);
		confirmImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (tabIndex == 0)
				{
					consumedSortReverse = consumedSortType == consumedTempSortType ? !consumedSortReverse : false;
					consumedSortType = consumedTempSortType;
					
					consumedTagCheck = consumedTempTagCheck;
					consumedCategoryCheck = consumedTempCategoryCheck;
					
					consumedFilterTagList.clear();
					for (int i = 0; i < consumedTagCheck.length; i++)
					{
						if (consumedTagCheck[i])
						{
							consumedFilterTagList.add(tagList.get(i));
						}
					}
					
					consumedFilterCategoryList.clear();
					for (int i = 0; i < consumedCategoryCheck.length; i++)
					{
						if (consumedCategoryCheck[i])
						{
							consumedFilterCategoryList.add(categoryList.get(i));
						}
					}		
				}
				else
				{
					proveSortReverse = proveSortType == proveTempSortType ? !proveSortReverse : false;
					proveSortType = proveTempSortType;
					
					proveTagCheck = proveTempTagCheck;
					proveCategoryCheck = proveTempCategoryCheck;
					
					proveFilterTagList.clear();
					for (int i = 0; i < proveTagCheck.length; i++)
					{
						if (proveTagCheck[i])
						{
							proveFilterTagList.add(tagList.get(i));
						}
					}
					
					proveFilterCategoryList.clear();
					for (int i = 0; i < proveCategoryCheck.length; i++)
					{
						if (proveCategoryCheck[i])
						{
							proveFilterCategoryList.add(categoryList.get(i));
						}
					}					
				}

				filterPopupWindow.dismiss();
				filterItemList();
				refreshView();
			}
		});

		ImageView cancelImageView = (ImageView)filterView.findViewById(R.id.cancelImageView);
		cancelImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (tabIndex == 0)
				{
					switch (consumedSortType)
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
					
					consumedTempTagCheck = consumedTagCheck;
					consumedTempCategoryCheck = consumedCategoryCheck;
				}
				else
				{
					switch (proveSortType)
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
					
					proveTempTagCheck = proveTagCheck;
					proveTempCategoryCheck = proveCategoryCheck;					
				}
				
				refreshTagView();				
				refreshCategoryView();				
				filterPopupWindow.dismiss();
			}
		});

		// init filter view
		int sortType = tabIndex == 0 ? consumedSortType : proveSortType;
		
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
		
		refreshTagView();
		refreshCategoryView();
		
		filterPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		filterPopupWindow.update();
    }
    
	private void refreshData()
	{
		consumedItemList = dbManager.getUnarchivedConsumedItems(appPreference.getCurrentUserID());
		proveAheadItemList = dbManager.getUnarchivedProveAheadItems(appPreference.getCurrentUserID());
		
		if (report.getLocalID() != -1)
		{
			List<Item> items = dbManager.getReportItems(report.getLocalID());
			if (!items.isEmpty())
			{
				Item item = items.get(0);
				if (item.isProveAhead() && !item.isPaApproved())
				{
					proveAheadItemList.addAll(items);
					Item.sortByUpdateDate(proveAheadItemList);
				}
				else
				{
					consumedItemList.addAll(items);
					Item.sortByUpdateDate(consumedItemList);
				}
			}			
		}
		
		filterItemList();
	}
	
	private void filterItemList()
	{
		int sortType;
		boolean sortReverse;
		List<Tag> filterTagList = new ArrayList<Tag>();
		List<Category> filterCategoryList = new ArrayList<Category>();
		List<Item> itemList = new ArrayList<Item>();
		List<Item> showList = new ArrayList<Item>();
		
		if (tabIndex == 0)
		{
			sortType = consumedSortType;
			sortReverse = consumedSortReverse;
			filterTagList = new ArrayList<Tag>(consumedFilterTagList);
			filterCategoryList = new ArrayList<Category>(consumedFilterCategoryList);
			itemList = new ArrayList<Item>(consumedItemList);
		}
		else
		{
			sortType = proveSortType;
			sortReverse = proveSortReverse;
			filterTagList = new ArrayList<Tag>(proveFilterTagList);
			filterCategoryList = new ArrayList<Category>(proveFilterCategoryList);
			itemList = new ArrayList<Item>(proveAheadItemList);
		}

		for (Item item : itemList)
		{			
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
		
		if (tabIndex == 0)
		{
			consumedShowList = new ArrayList<Item>(showList);
		}
		else
		{
			proveAheadShowList = new ArrayList<Item>(showList);		
		}
	}

	public void onClick(View v)
	{
		if (v.equals(consumedTextView))
		{
			tabIndex = 0;		
		}
		else
		{
			tabIndex = 1;
		}
		filterItemList();
		refreshView();
	}
}