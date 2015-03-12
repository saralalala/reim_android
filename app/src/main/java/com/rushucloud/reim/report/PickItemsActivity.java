package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.Category;
import classes.Item;
import classes.Report;
import classes.Tag;
import classes.adapter.ReportItemListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class PickItemsActivity extends Activity implements OnClickListener
{
    private static final int SORT_CONSUMED_DATE = 0;
	private static final int SORT_AMOUNT = 1;

	private PopupWindow filterPopupWindow;
    private RadioButton sortAmountRadio;
    private RadioButton sortConsumedDateRadio;
    private ImageView sortDateImageView;
    private ImageView sortAmountImageView;
    private RotateAnimation rotateAnimation;
    private RotateAnimation rotateReverseAnimation;
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

	private int consumedSortType = SORT_CONSUMED_DATE;
	private boolean consumedSortReverse = false;
	private boolean[] consumedTagCheck;
	private boolean[] consumedCategoryCheck;
	private List<Tag> consumedFilterTagList = new ArrayList<Tag>();
	private List<Category> consumedFilterCategoryList = new ArrayList<Category>();
	
	private int consumedTempSortType = SORT_CONSUMED_DATE;
    private boolean consumedTempSortReverse = false;
	private boolean[] consumedTempTagCheck;
	private boolean[] consumedTempCategoryCheck;
	
	private List<Item> proveAheadItemList;
	private List<Item> proveAheadShowList;
	private ArrayList<Integer> proveChosenList = null;

	private int proveSortType = SORT_CONSUMED_DATE;
	private boolean proveSortReverse = false;
	private boolean[] proveTagCheck;
	private boolean[] proveCategoryCheck;
	private List<Tag> proveFilterTagList = new ArrayList<Tag>();
	private List<Category> proveFilterCategoryList = new ArrayList<Category>();
	
	private int proveTempSortType = SORT_CONSUMED_DATE;
    private boolean proveTempSortReverse = false;
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
		MobclickAgent.onPageStart("PickItemsActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshData();
		refreshView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickItemsActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Bundle bundle = new Bundle();
			bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
			Intent intent = new Intent();
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
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

		consumedTextView = (TextView) findViewById(R.id.consumedTextView);
		consumedTextView.setOnClickListener(this);
		proveAheadTextView = (TextView) findViewById(R.id.proveAheadTextView);
		proveAheadTextView.setOnClickListener(this);
		
		itemCountTextView = (TextView) findViewById(R.id.itemCountTextView);
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					report.setIsProveAhead(isProveAhead);
					chosenItemIDList = tabIndex == 0 ? consumedChosenList : proveChosenList;
					Bundle bundle = new Bundle();
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
					Intent intent = new Intent();
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
					finish();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		warningTextView = (TextView) findViewById(R.id.warningTextView);
		
		itemListView = (ListView) findViewById(R.id.itemListView);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (position == 0)
				{
					Intent intent = new Intent(PickItemsActivity.this, EditItemActivity.class);
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
        rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);

        rotateReverseAnimation = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateReverseAnimation.setDuration(200);
        rotateReverseAnimation.setFillAfter(true);

		View filterView = View.inflate(this, R.layout.window_report_items_filter, null);

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

        sortConsumedDateRadio = (RadioButton) filterView.findViewById(R.id.sortConsumedDateRadio);
        sortConsumedDateRadio.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(PickItemsActivity.this, "UMENG_SHEET_TIME");
                selectSortDateRadio();
                if (tabIndex == 0)
                {
                    if (consumedTempSortType != SORT_CONSUMED_DATE)
                    {
                        consumedTempSortReverse = false;
                        consumedTempSortType = SORT_CONSUMED_DATE;
                    }
                    else
                    {
                        reverseSortDateImageView();
                    }
                }
                else
                {
                    if (proveTempSortType != SORT_CONSUMED_DATE)
                    {
                        proveTempSortReverse = false;
                        proveTempSortType = SORT_CONSUMED_DATE;
                    }
                    else
                    {
                        reverseSortDateImageView();
                    }
                }
            }
        });

        sortAmountRadio = (RadioButton) filterView.findViewById(R.id.sortAmountRadio);
        sortAmountRadio.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(PickItemsActivity.this, "UMENG_SHEET_AMOUNT");
                selectSortAmountRadio();
                if (tabIndex == 0)
                {
                    if (consumedTempSortType != SORT_AMOUNT)
                    {
                        consumedTempSortReverse = false;
                        consumedTempSortType = SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
                else
                {
                    if (proveTempSortType != SORT_AMOUNT)
                    {
                        proveTempSortReverse = false;
                        proveTempSortType = SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
            }
        });

		tagLayout = (LinearLayout) filterView.findViewById(R.id.tagLayout);		
		categoryLayout = (LinearLayout) filterView.findViewById(R.id.categoryLayout);

        ImageView confirmImageView = (ImageView)filterView.findViewById(R.id.confirmImageView);
        confirmImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (tabIndex == 0)
                {
                    consumedSortReverse = consumedTempSortReverse;
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
                    proveSortReverse = proveTempSortReverse;
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
                filterPopupWindow.dismiss();
            }
        });

		filterPopupWindow = ViewUtils.constructTopPopupWindow(this, filterView);
	}

	private void refreshView()
	{
		ReimProgressDialog.show();
		
		if (adapter == null)
		{
			if (isProveAhead)
			{
				adapter = new ReportItemListViewAdapter(PickItemsActivity.this, proveAheadShowList, proveChosenList);
			}
			else
			{
				adapter = new ReportItemListViewAdapter(PickItemsActivity.this, consumedShowList, consumedChosenList);
			}
			itemListView.setAdapter(adapter);			
		}
		
		if (tabIndex == 0)
		{
			itemCountTextView.setText(Integer.toString(consumedChosenList.size()));
			
			consumedTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
			proveAheadTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));

            int visibility = consumedItemList.isEmpty() ? View.VISIBLE : View.INVISIBLE;
            warningTextView.setVisibility(visibility);
            adapter.set(consumedShowList, consumedChosenList);
            adapter.notifyDataSetChanged();
		}
		else
		{
			itemCountTextView.setText(Integer.toString(proveChosenList.size()));
			
			consumedTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));
			proveAheadTextView.setTextColor(ViewUtils.getColor(R.color.major_light));

            int visibility = proveAheadItemList.isEmpty() ? View.VISIBLE : View.INVISIBLE;
            warningTextView.setVisibility(visibility);
            adapter.set(proveAheadShowList, proveChosenList);
            adapter.notifyDataSetChanged();
		}

		ReimProgressDialog.dismiss();
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
		LinearLayout layout = new LinearLayout(PickItemsActivity.this);
		for (int i = 0; i < tagList.size(); i++)
		{
			String name = tagList.get(i).getName();
			
			View view;
			if (tabIndex == 0)
			{
				int layoutID = consumedTempTagCheck[i] ? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
				view = View.inflate(PickItemsActivity.this, layoutID, null);
			}
			else
			{
				int layoutID = proveTempTagCheck[i] ? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
				view = View.inflate(PickItemsActivity.this, layoutID, null);
			}

			final int index = i;
			TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			nameTextView.setText(name);
			nameTextView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					MobclickAgent.onEvent(PickItemsActivity.this, "UMENG_SHEET_TAG");
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
				layout = new LinearLayout(PickItemsActivity.this);
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

		LinearLayout layout = new LinearLayout(PickItemsActivity.this);
		for (int i = 0; i < categoryList.size(); i++)
		{
			if (i % iconMaxCount == 0)
			{
				layout = new LinearLayout(PickItemsActivity.this);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = iconVerticalInterval;
				layout.setLayoutParams(params);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				
				categoryLayout.addView(layout);
			}
			
			Category category = categoryList.get(i);

			final int index = i;
			View view = View.inflate(PickItemsActivity.this, R.layout.grid_category, null);
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
					if (!category.getIconPath().isEmpty())
					{
						Bitmap icon = BitmapFactory.decodeFile(category.getIconPath());
						if (icon != null)
						{
							iconImageView.setImageBitmap(icon);		
						}						
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
					if (!category.getIconPath().isEmpty())
					{
						Bitmap icon = BitmapFactory.decodeFile(category.getIconPath());
						if (icon != null)
						{
							iconImageView.setImageBitmap(icon);		
						}				
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
        if (tabIndex == 0)
        {
            consumedTempSortReverse = false;
            consumedTempSortType = consumedSortType;
            switch (consumedSortType)
            {
                case SORT_CONSUMED_DATE:
                {
                    selectSortDateRadio();
                    if (consumedSortReverse)
                    {
                        reverseSortDateImageView();
                    }
                    break;
                }
                case SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (consumedSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
                default:
                    break;
            }
            consumedTempTagCheck = consumedTagCheck;
            consumedTempCategoryCheck = consumedCategoryCheck;
        }
        else
        {
            proveTempSortReverse = false;
            proveTempSortType = proveSortType;
            switch (proveSortType)
            {
                case SORT_CONSUMED_DATE:
                {
                    selectSortDateRadio();
                    if (proveSortReverse)
                    {
                        reverseSortDateImageView();
                    }
                    break;
                }
                case SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (proveSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
                default:
                    break;
            }
            proveTempTagCheck = proveTagCheck;
            proveTempCategoryCheck = proveCategoryCheck;
        }

		refreshTagView();
		refreshCategoryView();
		
		filterPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
		filterPopupWindow.update();
    }

    private void selectSortDateRadio()
    {
        sortConsumedDateRadio.setChecked(true);
        sortAmountRadio.setChecked(false);

        sortDateImageView.setVisibility(View.VISIBLE);
        sortAmountImageView.clearAnimation();
        sortAmountImageView.setVisibility(View.GONE);
    }

    private void selectSortAmountRadio()
    {
        sortConsumedDateRadio.setChecked(false);
        sortAmountRadio.setChecked(true);

        sortDateImageView.clearAnimation();
        sortDateImageView.setVisibility(View.GONE);
        sortAmountImageView.setVisibility(View.VISIBLE);
    }

    private void reverseSortDateImageView()
    {
        if (tabIndex == 0)
        {
            consumedTempSortReverse = !consumedTempSortReverse;
            if (!consumedTempSortReverse) // status before change
            {
                sortDateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortDateImageView.startAnimation(rotateAnimation);
            }
        }
        else
        {
            proveTempSortReverse = !proveTempSortReverse;
            if (!proveTempSortReverse) // status before change
            {
                sortDateImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortDateImageView.startAnimation(rotateAnimation);
            }
        }
    }

    private void reverseSortAmountImageView()
    {
        if (tabIndex == 0)
        {
            consumedTempSortReverse = !consumedTempSortReverse;
            if (!consumedTempSortReverse) // status before change
            {
                sortAmountImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortAmountImageView.startAnimation(rotateAnimation);
            }
        }
        else
        {
            proveTempSortReverse = !proveTempSortReverse;
            if (!proveTempSortReverse) // status before change
            {
                sortAmountImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortAmountImageView.startAnimation(rotateAnimation);
            }
        }
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
		List<Tag> filterTagList;
		List<Category> filterCategoryList;
		List<Item> itemList;
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

        switch (sortType)
        {
            case SORT_CONSUMED_DATE:
                Item.sortByConsumedDate(showList);
                break;
            case SORT_AMOUNT:
                Item.sortByAmount(showList);
                break;
            default:
                break;
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