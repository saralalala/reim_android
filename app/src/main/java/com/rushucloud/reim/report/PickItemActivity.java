package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import classes.adapter.ReportItemListViewAdapter;
import classes.model.Category;
import classes.model.Item;
import classes.model.Report;
import classes.model.Tag;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class PickItemActivity extends Activity implements OnClickListener
{
    // Widgets
    private ImageView filterImageView;
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
    private TextView budgetTextView;
    private TextView borrowingTextView;
    private TextView itemCountTextView;
    private TextView warningTextView;
    private ListView itemListView;
    private ReportItemListViewAdapter adapter;

    // Local Data
    private AppPreference appPreference;
    private static DBManager dbManager;

    private Report report;
    private ArrayList<Integer> chosenItemIDList = null;
    private int type;
    private int tabIndex = 0;
    private List<Tag> tagList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private List<Item> consumedItemList;
    private List<Item> consumedShowList = new ArrayList<>();
    private ArrayList<Integer> consumedChosenList = null;

    private int consumedSortType = Constant.SORT_CONSUMED_DATE;
    private boolean consumedSortReverse = false;
    private boolean[] consumedTagCheck;
    private boolean[] consumedCategoryCheck;
    private List<Tag> consumedFilterTagList = new ArrayList<>();
    private List<Category> consumedFilterCategoryList = new ArrayList<>();

    private int consumedTempSortType = Constant.SORT_CONSUMED_DATE;
    private boolean consumedTempSortReverse = false;
    private boolean[] consumedTempTagCheck;
    private boolean[] consumedTempCategoryCheck;

    private List<Item> budgetItemList;
    private List<Item> budgetShowList = new ArrayList<>();
    private ArrayList<Integer> budgetChosenList = null;

    private int budgetSortType = Constant.SORT_CONSUMED_DATE;
    private boolean budgetSortReverse = false;
    private boolean[] budgetTagCheck;
    private boolean[] budgetCategoryCheck;
    private List<Tag> budgetFilterTagList = new ArrayList<>();
    private List<Category> budgetFilterCategoryList = new ArrayList<>();

    private int budgetTempSortType = Constant.SORT_CONSUMED_DATE;
    private boolean budgetTempSortReverse = false;
    private boolean[] budgetTempTagCheck;
    private boolean[] budgetTempCategoryCheck;

    private List<Item> borrowingItemList;
    private List<Item> borrowingShowList = new ArrayList<>();
    private ArrayList<Integer> borrowingChosenList = null;

    private int borrowingSortType = Constant.SORT_CONSUMED_DATE;
    private boolean borrowingSortReverse = false;
    private boolean[] borrowingTagCheck;
    private boolean[] borrowingCategoryCheck;
    private List<Tag> borrowingFilterTagList = new ArrayList<>();
    private List<Category> borrowingFilterCategoryList = new ArrayList<>();

    private int borrowingTempSortType = Constant.SORT_CONSUMED_DATE;
    private boolean borrowingTempSortReverse = false;
    private boolean[] borrowingTempTagCheck;
    private boolean[] borrowingTempCategoryCheck;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_pick_item);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickItemActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshData();
        refreshView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickItemActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            ViewUtils.goBackWithResult(this, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case Constant.ACTIVITY_NEW_ITEM:
                {
                    int itemID = data.getIntExtra("itemID", -1);
                    if (itemID > 0)
                    {
                        type = data.getIntExtra("type", Item.TYPE_REIM);
                        if (type == Item.TYPE_REIM)
                        {
                            tabIndex = 0;
                            if (budgetChosenList.isEmpty() && borrowingChosenList.isEmpty())
                            {
                                consumedChosenList.add(itemID);
                            }
                        }
                        else if (type == Item.TYPE_BUDGET)
                        {
                            tabIndex = 1;
                            if (consumedChosenList.isEmpty() && borrowingChosenList.isEmpty())
                            {
                                budgetChosenList.add(itemID);
                            }
                        }
                        else if (type == Item.TYPE_BORROWING)
                        {
                            tabIndex = 2;
                            if (consumedChosenList.isEmpty() && budgetChosenList.isEmpty())
                            {
                                borrowingChosenList.add(itemID);
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView()
    {
        filterImageView = (ImageView) findViewById(R.id.filterImageView);
        filterImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showFilterWindow();
            }
        });

        consumedTextView = (TextView) findViewById(R.id.consumedTextView);
        consumedTextView.setOnClickListener(this);
        budgetTextView = (TextView) findViewById(R.id.budgetTextView);
        budgetTextView.setOnClickListener(this);
        borrowingTextView = (TextView) findViewById(R.id.borrowingTextView);
        borrowingTextView.setOnClickListener(this);

        itemCountTextView = (TextView) findViewById(R.id.itemCountTextView);

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    if (tabIndex == 0)
                    {
                        chosenItemIDList = consumedChosenList;
                    }
                    else if (tabIndex == 1)
                    {
                        chosenItemIDList = budgetChosenList;
                    }
                    else
                    {
                        chosenItemIDList = borrowingChosenList;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
                    bundle.putInt("type", type);
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    ViewUtils.goBackWithResult(PickItemActivity.this, intent);
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
                    if (appPreference.hasProxyEditPermission())
                    {
                        Intent intent = new Intent(PickItemActivity.this, EditItemActivity.class);
                        intent.putExtra("fromPickItems", true);
                        intent.putExtra("type", tabIndex);
                        ViewUtils.goForwardForResult(PickItemActivity.this, intent, Constant.ACTIVITY_NEW_ITEM);
                    }
                    else
                    {
                        ViewUtils.showToast(PickItemActivity.this, R.string.error_create_item_no_permission);
                    }
                }
                else
                {
                    if (tabIndex == 0)
                    {
                        type = Report.TYPE_REIM;
                        int localID = consumedShowList.get(position - 1).getLocalID();
                        if (consumedChosenList.contains(localID))
                        {
                            consumedChosenList.remove(Integer.valueOf(localID));
                        }
                        else
                        {
                            consumedChosenList.add(localID);
                        }
                        budgetChosenList.clear();
                        borrowingChosenList.clear();
                        itemCountTextView.setText(Integer.toString(consumedChosenList.size()));
                        adapter.setChosenList(consumedChosenList);
                    }
                    else if (tabIndex == 1)
                    {
                        type = Report.TYPE_BUDGET;
                        int localID = budgetShowList.get(position - 1).getLocalID();
                        if (budgetChosenList.contains(localID))
                        {
                            budgetChosenList.remove(Integer.valueOf(localID));
                        }
                        else
                        {
                            budgetChosenList.add(localID);
                        }
                        consumedChosenList.clear();
                        borrowingChosenList.clear();
                        itemCountTextView.setText(Integer.toString(budgetChosenList.size()));
                        adapter.setChosenList(budgetChosenList);
                    }
                    else
                    {
                        type = Report.TYPE_BORROWING;
                        int localID = borrowingShowList.get(position - 1).getLocalID();
                        if (borrowingChosenList.contains(localID))
                        {
                            borrowingChosenList.remove(Integer.valueOf(localID));
                        }
                        else
                        {
                            borrowingChosenList.add(localID);
                        }
                        consumedChosenList.clear();
                        budgetChosenList.clear();
                        itemCountTextView.setText(Integer.toString(borrowingChosenList.size()));
                        adapter.setChosenList(borrowingChosenList);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        initFilterWindow();
    }

    private void initFilterWindow()
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
                MobclickAgent.onEvent(PickItemActivity.this, "UMENG_SHEET_TIME");
                selectSortDateRadio();
                if (tabIndex == 0)
                {
                    if (consumedTempSortType != Constant.SORT_CONSUMED_DATE)
                    {
                        consumedTempSortReverse = false;
                        consumedTempSortType = Constant.SORT_CONSUMED_DATE;
                    }
                    else
                    {
                        reverseSortDateImageView();
                    }
                }
                else if (tabIndex == 1)
                {
                    if (budgetTempSortType != Constant.SORT_CONSUMED_DATE)
                    {
                        budgetTempSortReverse = false;
                        budgetTempSortType = Constant.SORT_CONSUMED_DATE;
                    }
                    else
                    {
                        reverseSortDateImageView();
                    }
                }
                else
                {
                    if (borrowingTempSortType != Constant.SORT_CONSUMED_DATE)
                    {
                        borrowingTempSortReverse = false;
                        borrowingTempSortType = Constant.SORT_CONSUMED_DATE;
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
                MobclickAgent.onEvent(PickItemActivity.this, "UMENG_SHEET_AMOUNT");
                selectSortAmountRadio();
                if (tabIndex == 0)
                {
                    if (consumedTempSortType != Constant.SORT_AMOUNT)
                    {
                        consumedTempSortReverse = false;
                        consumedTempSortType = Constant.SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
                else if (tabIndex == 1)
                {
                    if (budgetTempSortType != Constant.SORT_AMOUNT)
                    {
                        budgetTempSortReverse = false;
                        budgetTempSortType = Constant.SORT_AMOUNT;
                    }
                    else
                    {
                        reverseSortAmountImageView();
                    }
                }
                else
                {
                    if (borrowingTempSortType != Constant.SORT_AMOUNT)
                    {
                        borrowingTempSortReverse = false;
                        borrowingTempSortType = Constant.SORT_AMOUNT;
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

        ImageView confirmImageView = (ImageView) filterView.findViewById(R.id.confirmImageView);
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
                else if (tabIndex == 1)
                {
                    budgetSortReverse = budgetTempSortReverse;
                    budgetSortType = budgetTempSortType;

                    budgetTagCheck = budgetTempTagCheck;
                    budgetCategoryCheck = budgetTempCategoryCheck;

                    budgetFilterTagList.clear();
                    for (int i = 0; i < budgetTagCheck.length; i++)
                    {
                        if (budgetTagCheck[i])
                        {
                            budgetFilterTagList.add(tagList.get(i));
                        }
                    }

                    budgetFilterCategoryList.clear();
                    for (int i = 0; i < budgetCategoryCheck.length; i++)
                    {
                        if (budgetCategoryCheck[i])
                        {
                            budgetFilterCategoryList.add(categoryList.get(i));
                        }
                    }
                }
                else
                {
                    borrowingSortReverse = borrowingTempSortReverse;
                    borrowingSortType = borrowingTempSortType;

                    borrowingTagCheck = borrowingTempTagCheck;
                    borrowingCategoryCheck = borrowingTempCategoryCheck;

                    borrowingFilterTagList.clear();
                    for (int i = 0; i < borrowingTagCheck.length; i++)
                    {
                        if (borrowingTagCheck[i])
                        {
                            borrowingFilterTagList.add(tagList.get(i));
                        }
                    }

                    borrowingFilterCategoryList.clear();
                    for (int i = 0; i < borrowingCategoryCheck.length; i++)
                    {
                        if (borrowingCategoryCheck[i])
                        {
                            borrowingFilterCategoryList.add(categoryList.get(i));
                        }
                    }
                }

                filterPopupWindow.dismiss();
                filterItemList();
                refreshView();
            }
        });

        ImageView cancelImageView = (ImageView) filterView.findViewById(R.id.cancelImageView);
        cancelImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                filterPopupWindow.dismiss();
            }
        });

        filterPopupWindow = ViewUtils.buildTopPopupWindow(this, filterView);
    }

    private void showFilterWindow()
    {
        if (tabIndex == 0)
        {
            consumedTempSortReverse = false;
            consumedTempSortType = consumedSortType;
            switch (consumedSortType)
            {
                case Constant.SORT_CONSUMED_DATE:
                {
                    selectSortDateRadio();
                    if (consumedSortReverse)
                    {
                        reverseSortDateImageView();
                    }
                    break;
                }
                case Constant.SORT_AMOUNT:
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
        else if (tabIndex == 1)
        {
            budgetTempSortReverse = false;
            budgetTempSortType = budgetSortType;
            switch (budgetSortType)
            {
                case Constant.SORT_CONSUMED_DATE:
                {
                    selectSortDateRadio();
                    if (budgetSortReverse)
                    {
                        reverseSortDateImageView();
                    }
                    break;
                }
                case Constant.SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (budgetSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
                default:
                    break;
            }
            budgetTempTagCheck = budgetTagCheck;
            budgetTempCategoryCheck = budgetCategoryCheck;
        }
        else
        {
            borrowingTempSortReverse = false;
            borrowingTempSortType = borrowingSortType;
            switch (borrowingSortType)
            {
                case Constant.SORT_CONSUMED_DATE:
                {
                    selectSortDateRadio();
                    if (borrowingSortReverse)
                    {
                        reverseSortDateImageView();
                    }
                    break;
                }
                case Constant.SORT_AMOUNT:
                {
                    selectSortAmountRadio();
                    if (borrowingSortReverse)
                    {
                        reverseSortAmountImageView();
                    }
                    break;
                }
                default:
                    break;
            }
            borrowingTempTagCheck = borrowingTagCheck;
            borrowingTempCategoryCheck = borrowingCategoryCheck;
        }

        refreshTagView();
        refreshCategoryView();

        filterPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.CENTER, 0, 0);
        filterPopupWindow.update();
    }

    private void refreshView()
    {
        ReimProgressDialog.show();

        if (adapter == null)
        {
            if (type == Item.TYPE_REIM)
            {
                adapter = new ReportItemListViewAdapter(PickItemActivity.this, consumedShowList, consumedChosenList);
            }
            else if (type == Item.TYPE_BUDGET)
            {
                adapter = new ReportItemListViewAdapter(PickItemActivity.this, budgetShowList, budgetChosenList);
            }
            else if (type == Item.TYPE_BORROWING)
            {
                adapter = new ReportItemListViewAdapter(PickItemActivity.this, borrowingShowList, borrowingChosenList);
            }
            itemListView.setAdapter(adapter);
        }

        if (tabIndex == 0)
        {
            itemCountTextView.setText(Integer.toString(consumedChosenList.size()));

            consumedTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
            budgetTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));
            borrowingTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));

            int visibility = consumedItemList.isEmpty() ? View.VISIBLE : View.INVISIBLE;
            warningTextView.setVisibility(visibility);
            adapter.set(consumedShowList, consumedChosenList);
            adapter.notifyDataSetChanged();

            int filterImage = !consumedFilterTagList.isEmpty() || !consumedFilterCategoryList.isEmpty() ? R.drawable.filter_full : R.drawable.filter_empty;
            filterImageView.setImageResource(filterImage);
        }
        else if (tabIndex == 1)
        {
            itemCountTextView.setText(Integer.toString(budgetChosenList.size()));

            consumedTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));
            budgetTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
            borrowingTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));

            int visibility = budgetItemList.isEmpty() ? View.VISIBLE : View.INVISIBLE;
            warningTextView.setVisibility(visibility);
            adapter.set(budgetShowList, budgetChosenList);
            adapter.notifyDataSetChanged();

            int filterImage = !budgetFilterTagList.isEmpty() || !budgetFilterCategoryList.isEmpty() ? R.drawable.filter_full : R.drawable.filter_empty;
            filterImageView.setImageResource(filterImage);
        }
        else
        {
            itemCountTextView.setText(Integer.toString(borrowingChosenList.size()));

            consumedTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));
            budgetTextView.setTextColor(ViewUtils.getColor(R.color.hint_white));
            borrowingTextView.setTextColor(ViewUtils.getColor(R.color.major_light));

            int visibility = borrowingItemList.isEmpty() ? View.VISIBLE : View.INVISIBLE;
            warningTextView.setVisibility(visibility);
            adapter.set(borrowingShowList, borrowingChosenList);
            adapter.notifyDataSetChanged();

            int filterImage = !borrowingFilterTagList.isEmpty() || !borrowingFilterCategoryList.isEmpty() ? R.drawable.filter_full : R.drawable.filter_empty;
            filterImageView.setImageResource(filterImage);
        }

        ReimProgressDialog.dismiss();
    }

    private void refreshTagView()
    {
        tagLayout.removeAllViews();

        int layoutMaxLength = ViewUtils.getPhoneWindowWidth(this) - ViewUtils.dpToPixel(32);
        int tagVerticalInterval = ViewUtils.dpToPixel(16);
        int tagHorizontalInterval = ViewUtils.dpToPixel(10);
        int padding = ViewUtils.dpToPixel(24);
        int textSize = ViewUtils.dpToPixel(16);

        int space = 0;
        LinearLayout layout = new LinearLayout(PickItemActivity.this);
        for (int i = 0; i < tagList.size(); i++)
        {
            String name = tagList.get(i).getName();

            View view;
            if (tabIndex == 0)
            {
                int layoutID = consumedTempTagCheck[i] ? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
                view = View.inflate(PickItemActivity.this, layoutID, null);
            }
            else if (tabIndex == 1)
            {
                int layoutID = budgetTempTagCheck[i] ? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
                view = View.inflate(PickItemActivity.this, layoutID, null);
            }
            else
            {
                int layoutID = borrowingTempTagCheck[i] ? R.layout.grid_item_tag : R.layout.grid_item_tag_unselected;
                view = View.inflate(PickItemActivity.this, layoutID, null);
            }

            final int index = i;
            TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
            nameTextView.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    MobclickAgent.onEvent(PickItemActivity.this, "UMENG_SHEET_TAG");
                    if (tabIndex == 0)
                    {
                        consumedTempTagCheck[index] = !consumedTempTagCheck[index];
                    }
                    else if (tabIndex == 1)
                    {
                        budgetTempTagCheck[index] = !budgetTempTagCheck[index];
                    }
                    else
                    {
                        borrowingTempTagCheck[index] = !borrowingTempTagCheck[index];
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
                layout = new LinearLayout(PickItemActivity.this);
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

        int layoutMaxLength = ViewUtils.getPhoneWindowWidth(this) - ViewUtils.dpToPixel(32);
        int iconWidth = ViewUtils.dpToPixel(50);
        int iconVerticalInterval = ViewUtils.dpToPixel(16);
        int iconHorizontalInterval = ViewUtils.dpToPixel(18);
        int iconMaxCount = (layoutMaxLength + iconHorizontalInterval) / (iconWidth + iconHorizontalInterval);
        iconHorizontalInterval = (layoutMaxLength - iconWidth * iconMaxCount) / (iconMaxCount - 1);

        LinearLayout layout = new LinearLayout(PickItemActivity.this);
        for (int i = 0; i < categoryList.size(); i++)
        {
            if (i % iconMaxCount == 0)
            {
                layout = new LinearLayout(PickItemActivity.this);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.topMargin = iconVerticalInterval;
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                categoryLayout.addView(layout);
            }

            Category category = categoryList.get(i);

            final int index = i;
            View view = View.inflate(PickItemActivity.this, R.layout.grid_category, null);
            view.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    if (tabIndex == 0)
                    {
                        consumedTempCategoryCheck[index] = !consumedTempCategoryCheck[index];
                    }
                    else if (tabIndex == 1)
                    {
                        budgetTempCategoryCheck[index] = !budgetTempCategoryCheck[index];
                    }
                    else
                    {
                        borrowingTempCategoryCheck[index] = !borrowingTempCategoryCheck[index];
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
                    ViewUtils.setImageViewBitmap(category, iconImageView);
                    nameTextView.setTextColor(unselectedColor);
                }
            }
            else if (tabIndex == 1)
            {
                if (budgetTempCategoryCheck[i])
                {
                    iconImageView.setImageResource(R.drawable.icon_chosen);
                    nameTextView.setTextColor(selectedColor);
                }
                else
                {
                    ViewUtils.setImageViewBitmap(category, iconImageView);
                    nameTextView.setTextColor(unselectedColor);
                }
            }
            else
            {
                if (borrowingTempCategoryCheck[i])
                {
                    iconImageView.setImageResource(R.drawable.icon_chosen);
                    nameTextView.setTextColor(selectedColor);
                }
                else
                {
                    ViewUtils.setImageViewBitmap(category, iconImageView);
                    nameTextView.setTextColor(unselectedColor);
                }
            }

            LayoutParams params = new LayoutParams(iconWidth, LayoutParams.WRAP_CONTENT);
            params.rightMargin = iconHorizontalInterval;

            layout.addView(view, params);
        }
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
        else if (tabIndex == 1)
        {
            budgetTempSortReverse = !budgetTempSortReverse;
            if (!budgetTempSortReverse) // status before change
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
            borrowingTempSortReverse = !borrowingTempSortReverse;
            if (!borrowingTempSortReverse) // status before change
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
        else if (tabIndex == 1)
        {
            budgetTempSortReverse = !budgetTempSortReverse;
            if (!budgetTempSortReverse) // status before change
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
            borrowingTempSortReverse = !borrowingTempSortReverse;
            if (!borrowingTempSortReverse) // status before change
            {
                sortAmountImageView.startAnimation(rotateReverseAnimation);
            }
            else
            {
                sortAmountImageView.startAnimation(rotateAnimation);
            }
        }
    }

    public void onClick(View v)
    {
        if (v.equals(consumedTextView))
        {
            tabIndex = 0;
        }
        else if (v.equals(budgetTextView))
        {
            tabIndex = 1;
        }
        else
        {
            tabIndex = 2;
        }
        filterItemList();
        refreshView();
    }

    // Data
    private void initData()
    {
        Bundle bundle = getIntent().getExtras();
        report = (Report) bundle.getSerializable("report");
        chosenItemIDList = bundle.getIntegerArrayList("chosenItemIDList");
        if (chosenItemIDList == null)
        {
            chosenItemIDList = new ArrayList<>();
        }

        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        tabIndex = report.getType();
        if (tabIndex == 0)
        {
            consumedChosenList = new ArrayList<>(chosenItemIDList);
            budgetChosenList = new ArrayList<>();
            borrowingChosenList = new ArrayList<>();
        }
        else if (tabIndex == 1)
        {
            consumedChosenList = new ArrayList<>();
            budgetChosenList = new ArrayList<>(chosenItemIDList);
            borrowingChosenList = new ArrayList<>();
        }
        else
        {
            consumedChosenList = new ArrayList<>();
            budgetChosenList = new ArrayList<>();
            borrowingChosenList = new ArrayList<>(chosenItemIDList);
        }

        tagList = dbManager.getGroupTags(appPreference.getCurrentGroupID());
        consumedTagCheck = new boolean[tagList.size()];
        budgetTagCheck = new boolean[tagList.size()];
        borrowingTagCheck = new boolean[tagList.size()];
        for (int i = 0; i < consumedTagCheck.length; i++)
        {
            consumedTagCheck[i] = false;
            budgetTagCheck[i] = false;
            borrowingTagCheck[i] = false;
        }
        consumedTempTagCheck = consumedTagCheck;
        budgetTempTagCheck = budgetTagCheck;
        borrowingTempTagCheck = borrowingTagCheck;

        categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
        consumedCategoryCheck = new boolean[categoryList.size()];
        budgetCategoryCheck = new boolean[categoryList.size()];
        borrowingCategoryCheck = new boolean[categoryList.size()];
        for (int i = 0; i < consumedCategoryCheck.length; i++)
        {
            consumedCategoryCheck[i] = false;
            budgetCategoryCheck[i] = false;
            borrowingCategoryCheck[i] = false;
        }
        consumedTempCategoryCheck = consumedCategoryCheck;
        budgetTempCategoryCheck = budgetCategoryCheck;
        borrowingTempCategoryCheck = borrowingCategoryCheck;
    }

    private void refreshData()
    {
        consumedItemList = dbManager.getUnarchivedConsumedItems(appPreference.getCurrentUserID());
        budgetItemList = dbManager.getUnarchivedBudgetItems(appPreference.getCurrentUserID());
        borrowingItemList = dbManager.getUnarchivedBorrowingItems(appPreference.getCurrentUserID());

        if (report.getLocalID() != -1)
        {
            List<Item> items = dbManager.getReportItems(report.getLocalID());
            if (!items.isEmpty())
            {
                Item item = items.get(0);
                if (item.getType() == Item.TYPE_BUDGET && !item.isAaApproved())
                {
                    budgetItemList.addAll(items);
                    Item.sortByUpdateDate(budgetItemList);
                }
                else if (item.getType() == Item.TYPE_BORROWING && !item.isAaApproved())
                {
                    borrowingItemList.addAll(items);
                    Item.sortByUpdateDate(borrowingItemList);
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
        List<Item> showList = new ArrayList<>();

        if (tabIndex == 0)
        {
            sortType = consumedSortType;
            sortReverse = consumedSortReverse;
            filterTagList = new ArrayList<>(consumedFilterTagList);
            filterCategoryList = new ArrayList<>(consumedFilterCategoryList);
            itemList = new ArrayList<>(consumedItemList);
        }
        else if (tabIndex == 1)
        {
            sortType = budgetSortType;
            sortReverse = budgetSortReverse;
            filterTagList = new ArrayList<>(budgetFilterTagList);
            filterCategoryList = new ArrayList<>(budgetFilterCategoryList);
            itemList = new ArrayList<>(budgetItemList);
        }
        else
        {
            sortType = borrowingSortType;
            sortReverse = borrowingSortReverse;
            filterTagList = new ArrayList<>(borrowingFilterTagList);
            filterCategoryList = new ArrayList<>(borrowingFilterCategoryList);
            itemList = new ArrayList<>(borrowingItemList);
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
            case Constant.SORT_CONSUMED_DATE:
                Item.sortByConsumedDate(showList);
                break;
            case Constant.SORT_AMOUNT:
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
            consumedShowList.clear();
            consumedShowList.addAll(showList);
        }
        else if (tabIndex == 1)
        {
            budgetShowList.clear();
            budgetShowList.addAll(showList);
        }
        else
        {
            borrowingShowList.clear();
            borrowingShowList.addAll(showList);
        }
    }
}