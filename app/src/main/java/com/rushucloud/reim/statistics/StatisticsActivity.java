package com.rushucloud.reim.statistics;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import classes.adapter.StatisticsListViewAdapter;
import classes.model.Category;
import classes.model.Currency;
import classes.model.StatCategory;
import classes.model.StatTag;
import classes.model.StatUser;
import classes.model.Tag;
import classes.model.User;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimBar;
import classes.widget.ReimCircle;
import classes.widget.ReimPie;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.statistics.MineStatDetailRequest;
import netUtils.request.statistics.OthersStatRequest;
import netUtils.response.statistics.MineStatDetailResponse;
import netUtils.response.statistics.OthersStatResponse;

public class StatisticsActivity extends Activity
{
    // Widgets
    private StatisticsListViewAdapter adapter;
    private XListView statListView;

    private LinearLayout newLayout;
    private TextView newTextView;
    private TextView overviewTextView;
    private RelativeLayout categoryTitleLayout;
    private RelativeLayout pieLayout;
    private FrameLayout statContainer;
    private TextView totalTextView;
    private TextView unitTextView;
    private LinearLayout categoryLayout;
    private LinearLayout leftCategoryLayout;
    private LinearLayout rightCategoryLayout;
    private TextView monthTotalTextView;
    private TextView totalUnitTextView;
    private RelativeLayout monthTitleLayout;
    private LinearLayout monthLayout;
    private RelativeLayout currencyTitleLayout;
    private LinearLayout currencyLayout;
    private RelativeLayout tagTitleLayout;
    private LinearLayout tagLayout;
    private RelativeLayout memberTitleLayout;
    private LinearLayout memberLayout;

    // Local Data
    private DBManager dbManager;

    private int colorR[] = {60, 181, 232, 181, 141, 62, 255, 138, 238, 125, 56};
    private int colorG[] = {183, 112, 140, 184, 192, 119, 196, 118, 149, 173, 56};
    private int colorB[] = {152, 178, 191, 69, 219, 219, 0, 203, 50, 165, 56};
    private int colorRDiff[] = {137, 52, 16, 52, 80, 135, 0, 82, 12, 91, 169};
    private int colorGDiff[] = {51, 100, 81, 50, 44, 95, 41, 96, 74, 58, 169};
    private int colorBDiff[] = {72, 54, 45, 131, 25, 25, 179, 37, 144, 63, 169};
    private boolean mineData;
    private int year;
    private int month;
    private int categoryID;
    private int tagID;
    private int userID;
    private int lastUpdateTime = 0;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("StatisticsActivity");
        MobclickAgent.onResume(this);

        if (PhoneUtils.isNetworkConnected() && needToGetData())
        {
            ReimProgressDialog.show();
            if (mineData)
            {
                sendGetMineDataRequest();
            }
            else
            {
                sendGetOthersDataRequest();
            }
        }
        else if (!PhoneUtils.isNetworkConnected() && needToGetData())
        {
            ViewUtils.showToast(this, R.string.error_get_data_network_unavailable);
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("StatisticsActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        if (year != 0)
        {
            titleTextView.setText(Utils.getMonthString(year, month));
        }
        else if (categoryID != 0)
        {
            Category category = dbManager.getCategory(categoryID);
            if (category != null)
            {
                titleTextView.setText(getString(R.string.stat_category) + category.getName());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                goBack();
            }
        }
        else if (tagID != 0)
        {
            Tag tag = dbManager.getTag(tagID);
            if (tag != null)
            {
                titleTextView.setText(getString(R.string.stat_tag) + tag.getName());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                goBack();
            }
        }
        else if (userID != 0)
        {
            User user = dbManager.getUser(userID);
            if (user != null)
            {
                titleTextView.setText(getString(R.string.stat_user) + user.getNickname());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                goBack();
            }
        }
        else
        {
            ViewUtils.showToast(this, R.string.failed_to_read_data);
            goBack();
        }

        View view = View.inflate(this, R.layout.view_stat_second, null);

        newLayout = (LinearLayout) view.findViewById(R.id.newLayout);
        newTextView = (TextView) view.findViewById(R.id.newTextView);
        overviewTextView = (TextView) view.findViewById(R.id.overviewTextView);
        overviewTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        categoryTitleLayout = (RelativeLayout) view.findViewById(R.id.categoryTitleLayout);
        pieLayout = (RelativeLayout) view.findViewById(R.id.pieLayout);
        statContainer = (FrameLayout) view.findViewById(R.id.statContainer);
        totalTextView = (TextView) view.findViewById(R.id.totalTextView);
        totalTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
        unitTextView = (TextView) view.findViewById(R.id.unitTextView);
        categoryLayout = (LinearLayout) view.findViewById(R.id.categoryLayout);
        leftCategoryLayout = (LinearLayout) view.findViewById(R.id.leftCategoryLayout);
        rightCategoryLayout = (LinearLayout) view.findViewById(R.id.rightCategoryLayout);

        monthTotalTextView = (TextView) view.findViewById(R.id.monthTotalTextView);
        totalUnitTextView = (TextView) view.findViewById(R.id.totalUnitTextView);
        monthTitleLayout = (RelativeLayout) view.findViewById(R.id.monthTitleLayout);
        monthLayout = (LinearLayout) view.findViewById(R.id.monthLayout);

        currencyTitleLayout = (RelativeLayout) view.findViewById(R.id.currencyTitleLayout);
        currencyLayout = (LinearLayout) view.findViewById(R.id.currencyLayout);

        tagTitleLayout = (RelativeLayout) view.findViewById(R.id.tagTitleLayout);
        tagLayout = (LinearLayout) view.findViewById(R.id.tagLayout);

        memberTitleLayout = (RelativeLayout) view.findViewById(R.id.memberTitleLayout);
        memberLayout = (LinearLayout) view.findViewById(R.id.memberLayout);

        adapter = new StatisticsListViewAdapter(view);
        statListView = (XListView) findViewById(R.id.statListView);
        statListView.setAdapter(adapter);
        statListView.setXListViewListener(new XListView.IXListViewListener()
        {
            public void onRefresh()
            {
                boolean isNetworkConnected = PhoneUtils.isNetworkConnected();
                if (isNetworkConnected && mineData)
                {
                    sendGetMineDataRequest();
                }
                else if (isNetworkConnected)
                {
                    sendGetOthersDataRequest();
                }
                else
                {
                    statListView.stopRefresh();
                    ViewUtils.showToast(StatisticsActivity.this, R.string.error_get_data_network_unavailable);
                }
            }

            public void onLoadMore()
            {

            }
        });
        statListView.setPullRefreshEnable(true);
        statListView.setPullLoadEnable(false);
        statListView.setRefreshTime(getString(R.string.dash));
    }

    private void resetView()
    {
        statContainer.removeAllViews();
        leftCategoryLayout.removeAllViews();
        rightCategoryLayout.removeAllViews();
        monthLayout.removeAllViews();
        currencyLayout.removeAllViews();
        tagLayout.removeAllViews();
        memberLayout.removeAllViews();
    }

    private void drawOverviewLayout(double totalAmount, double newAmount)
    {
        if (mineData)
        {
            newLayout.setVisibility(View.VISIBLE);
            newTextView.setText(Utils.formatAmount(newAmount));
        }
        else
        {
            newLayout.setVisibility(View.GONE);
        }
        overviewTextView.setText(Utils.formatAmount(totalAmount));
    }

    private void drawCategoryPie(List<StatCategory> categoryList)
    {
        if (categoryID != 0 && categoryList.size() <= 1)
        {
            categoryTitleLayout.setVisibility(View.GONE);
            pieLayout.setVisibility(View.GONE);
            categoryLayout.setVisibility(View.GONE);
        }
        else
        {
            categoryTitleLayout.setVisibility(View.VISIBLE);
            pieLayout.setVisibility(View.VISIBLE);
            categoryLayout.setVisibility(View.VISIBLE);

            SparseArray<List<StatCategory>> categoryArray = new SparseArray<>();

            double totalAmount = 0;
            StatCategory deletedCategory = new StatCategory();
            deletedCategory.setName(getString(R.string.deleted_category));
            for (StatCategory category : categoryList)
            {
                Category localCategory = dbManager.getCategory(category.getCategoryID());
                totalAmount += category.getAmount();
                if (localCategory != null)
                {
                    int iconID = localCategory.getIconID() < 1 ? Constant.DEFAULT_ICON_ID : localCategory.getIconID();
                    category.setIconID(iconID);
                    category.setName(localCategory.getName());
                    if (categoryArray.indexOfKey(iconID) < 0)
                    {
                        categoryArray.put(iconID, new ArrayList<StatCategory>());
                    }
                    List<StatCategory> list = categoryArray.get(iconID);
                    list.add(category);
                }
                else
                {
                    deletedCategory.setAmount(deletedCategory.getAmount() + category.getAmount());
                }
            }
            if (deletedCategory.getAmount() > 0)
            {
                if (categoryArray.indexOfKey(Constant.DEFAULT_ICON_ID) < 0)
                {
                    categoryArray.put(Constant.DEFAULT_ICON_ID, new ArrayList<StatCategory>());
                }
                List<StatCategory> list = categoryArray.get(Constant.DEFAULT_ICON_ID);
                list.add(deletedCategory);
            }

            if (totalAmount < 10000)
            {
                totalTextView.setText(Utils.formatDouble(totalAmount));
                unitTextView.setVisibility(View.GONE);
            }
            else if (totalAmount < 10000000)
            {
                totalTextView.setText(Utils.formatDouble(totalAmount / 10000));
                unitTextView.setText(R.string.ten_thousand);
            }
            else
            {
                totalTextView.setText(Utils.formatDouble(totalAmount / 100000000));
                unitTextView.setText(R.string.one_hundred_million);
            }

            ReimPie reimPie = new ReimPie(this, 0, 360, statContainer.getWidth(), ViewUtils.getColor(R.color.stat_pie), 1);
            statContainer.addView(reimPie);

            float startAngle = -90;

            int legendWidth = ViewUtils.dpToPixel(10);
            int count = 0;
            for (int i = 0; i < categoryArray.size(); i++)
            {
                int key = categoryArray.keyAt(i);
                int colorIndex = key - 1;
                List<StatCategory> categories = categoryArray.get(key);
                int rDiff = categories.size() == 1 ? colorRDiff[colorIndex] : colorRDiff[colorIndex] / (categories.size() - 1);
                int gDiff = categories.size() == 1 ? colorGDiff[colorIndex] : colorGDiff[colorIndex] / (categories.size() - 1);
                int bDiff = categories.size() == 1 ? colorBDiff[colorIndex] : colorBDiff[colorIndex] / (categories.size() - 1);
                for (int j = 0; j < categories.size(); j++)
                {
                    StatCategory category = categories.get(j);
                    if (key != Constant.DEFAULT_ICON_ID)
                    {
                        category.setColor(Color.rgb(colorR[colorIndex] + j * rDiff,
                                                    colorG[colorIndex] + j * gDiff,
                                                    colorB[colorIndex] + j * bDiff));
                    }
                    else
                    {
                        category.setColor(Color.rgb(colorR[colorIndex] + colorRDiff[colorIndex] - j * rDiff,
                                                    colorG[colorIndex] + colorGDiff[colorIndex] - j * gDiff,
                                                    colorB[colorIndex] + colorBDiff[colorIndex] - j * bDiff));
                    }

                    float angle = i == categoryArray.size() - 1 && j == categories.size() - 1 ?
                            270 - startAngle : (float) (360 * category.getAmount() / totalAmount);

                    reimPie = new ReimPie(this, startAngle, angle, statContainer.getWidth(), category.getColor(), 1);
                    statContainer.addView(reimPie);

                    startAngle += angle;

                    View categoryView = View.inflate(this, R.layout.list_category_stat_others, null);
                    categoryView.setOnClickListener(new View.OnClickListener()
                    {
                        public void onClick(View v)
                        {

                        }
                    });

                    ImageView iconImageView = (ImageView) categoryView.findViewById(R.id.iconImageView);
                    ViewUtils.setImageViewBitmap(category, iconImageView);

                    TextView amountTextView = (TextView) categoryView.findViewById(R.id.amountTextView);
                    amountTextView.setText(Utils.formatAmount(category.getAmount()));

                    FrameLayout legendLayout = (FrameLayout) categoryView.findViewById(R.id.legendLayout);
                    ReimPie legendPie = new ReimPie(this, 0, 360, legendWidth, category.getColor(), 0);
                    legendLayout.addView(legendPie);

                    TextView nameTextView = (TextView) categoryView.findViewById(R.id.nameTextView);
                    nameTextView.setText(category.getName());

                    if (count % 2 == 0)
                    {
                        leftCategoryLayout.addView(categoryView);
                    }
                    else
                    {
                        rightCategoryLayout.addView(categoryView);
                    }
                    count++;
                }
            }

            ReimCircle reimCircle = new ReimCircle(this, 12, statContainer.getWidth(), ViewUtils.getColor(R.color.stat_pie_border), 1);
            statContainer.addView(reimCircle);

            reimPie = new ReimPie(this, 0, 360, statContainer.getWidth(), ViewUtils.getColor(R.color.background), 40);
            statContainer.addView(reimPie);
        }
    }

    private void drawMonthBar(HashMap<String, Double> monthsData)
    {
        if (year == 0 || month == 0)
        {
            monthTitleLayout.setVisibility(View.VISIBLE);
            monthLayout.setVisibility(View.VISIBLE);
            if (!monthsData.isEmpty())
            {
                double total = 0;
                double max = 0;
                for (Double data : monthsData.values())
                {
                    total += data;
                    if (data > max)
                    {
                        max = data;
                    }
                }
                monthTotalTextView.setText(Utils.formatAmount(total));

                for (final String month : monthsData.keySet())
                {
                    Double data = monthsData.get(month);
                    ReimBar monthBar = new ReimBar(StatisticsActivity.this, data / max);

                    View view = View.inflate(StatisticsActivity.this, R.layout.list_month_stat, null);

                    TextView monthTextView = (TextView) view.findViewById(R.id.monthTextView);
                    monthTextView.setText(month);

                    TextView dataTextView = (TextView) view.findViewById(R.id.dataTextView);
                    TextView unitTextView = (TextView) view.findViewById(R.id.unitTextView);

                    if (data < 100000)
                    {
                        dataTextView.setText(Utils.formatDouble(data));
                        unitTextView.setVisibility(View.GONE);
                    }
                    else if (data < 100000000)
                    {
                        dataTextView.setText(Utils.formatDouble(data / 10000));
                        unitTextView.setText(R.string.ten_thousand);
                    }
                    else
                    {
                        dataTextView.setText(Utils.formatDouble(data / 100000000));
                        unitTextView.setText(R.string.one_hundred_million);
                    }

                    LinearLayout dataLayout = (LinearLayout) view.findViewById(R.id.dataLayout);
                    dataLayout.addView(monthBar);

                    monthLayout.addView(view);
                }
            }
            else
            {
                monthTotalTextView.setVisibility(View.INVISIBLE);
                totalUnitTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void drawCurrency(HashMap<String, Double> currencyData)
    {
        if (currencyData.size() > 1)
        {
            currencyTitleLayout.setVisibility(View.VISIBLE);
            currencyLayout.setVisibility(View.VISIBLE);

            for (String code : currencyData.keySet())
            {
                final Currency currency = dbManager.getCurrency(code);
                if (currency != null)
                {
                    View view = View.inflate(StatisticsActivity.this, R.layout.list_currency_stat, null);

                    TextView currencyTextView = (TextView) view.findViewById(R.id.currencyTextView);
                    currencyTextView.setText(currency.getName());

                    TextView symbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
                    symbolTextView.setText(currency.getSymbol());

                    TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                    amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
                    amountTextView.setText(Utils.formatAmount(currencyData.get(code)));

                    if (mineData)
                    {
                        currencyLayout.addView(view);
                    }
                    else
                    {
                        currencyLayout.addView(view);
                    }
                }
            }

            if (mineData)
            {
                View lastView = currencyLayout.getChildAt(currencyLayout.getChildCount() - 1);
                View divider = lastView.findViewById(R.id.divider);
                divider.setVisibility(View.INVISIBLE);
            }
            else
            {
                View lastView = currencyLayout.getChildAt(currencyLayout.getChildCount() - 1);
                View divider = lastView.findViewById(R.id.divider);
                divider.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            currencyTitleLayout.setVisibility(View.GONE);
            currencyLayout.setVisibility(View.GONE);
        }
    }

    private void drawTagBar(List<StatTag> tagList)
    {
        if (tagID == 0 && !tagList.isEmpty())
        {
            tagTitleLayout.setVisibility(View.VISIBLE);
            tagLayout.setVisibility(View.VISIBLE);

            double max = 0;
            for (StatTag tag : tagList)
            {
                if (tag.getAmount() > max)
                {
                    max = tag.getAmount();
                }
            }

            for (StatTag tag : tagList)
            {
                Tag localTag = dbManager.getTag(tag.getTagID());
                if (localTag != null)
                {
                    double amount = tag.getAmount();
                    ReimBar tagBar = new ReimBar(this, amount / max);

                    View view = View.inflate(this, R.layout.list_tag_stat, null);

                    TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
                    nameTextView.setText(localTag.getName());

                    TextView countTextView = (TextView) view.findViewById(R.id.countTextView);
                    countTextView.setText(String.format(getString(R.string.item_count), tag.getItemCount()));

                    TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                    TextView unitTextView = (TextView) view.findViewById(R.id.unitTextView);

                    if (amount < 100000)
                    {
                        amountTextView.setText(Utils.formatDouble(amount));
                        unitTextView.setVisibility(View.GONE);
                    }
                    else if (amount < 100000000)
                    {
                        amountTextView.setText(Utils.formatDouble(amount / 10000));
                        unitTextView.setText(R.string.ten_thousand);
                    }
                    else
                    {
                        amountTextView.setText(Utils.formatDouble(amount / 100000000));
                        unitTextView.setText(R.string.one_hundred_million);
                    }

                    LinearLayout dataLayout = (LinearLayout) view.findViewById(R.id.dataLayout);
                    dataLayout.addView(tagBar);

                    tagLayout.addView(view);
                }
            }
        }
    }

    private void drawMember(List<StatUser> userList)
    {
        if (userID == 0)
        {
            memberTitleLayout.setVisibility(View.VISIBLE);
            memberLayout.setVisibility(View.VISIBLE);

            if (!userList.isEmpty())
            {
                for (StatUser user : userList)
                {
                    User localUser = dbManager.getUser(user.getUserID());
                    if (localUser != null)
                    {
                        View view = View.inflate(this, R.layout.list_member_stat, null);

                        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
                        nameTextView.setText(localUser.getNickname());

                        TextView countTextView = (TextView) view.findViewById(R.id.countTextView);
                        countTextView.setText(Integer.toString(user.getItemCount()));

                        TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                        amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
                        amountTextView.setText(Utils.formatAmount(user.getAmount()));

                        memberLayout.addView(view);
                    }
                }
            }
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        dbManager = DBManager.getDBManager();

        Bundle bundle = getIntent().getExtras();
        mineData = bundle.getBoolean("mineData", false);
        year = bundle.getInt("year", 0);
        month = bundle.getInt("month", 0);
        categoryID = bundle.getInt("categoryID", 0);
        tagID = bundle.getInt("tagID", 0);
        userID = bundle.getInt("userID", 0);
    }

    private boolean needToGetData()
    {
        return Utils.getCurrentTime() - lastUpdateTime > Constant.GET_DATA_INTERVAL;
    }

    // Network
    private void sendGetMineDataRequest()
    {
        MineStatDetailRequest request = new MineStatDetailRequest(year, month, tagID, categoryID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final MineStatDetailResponse response = new MineStatDetailResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            lastUpdateTime = Utils.getCurrentTime();
                            resetView();
                            drawOverviewLayout(response.getTotalAmount(), response.getNewAmount());
                            drawCategoryPie(response.getStatCategoryList());
                            drawMonthBar(response.getMonthsData());
                            drawCurrency(response.getCurrencyData());
                            drawTagBar(response.getStatTagList());
                            adapter.notifyDataSetChanged();
                            statListView.stopRefresh();
                            statListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
                            ReimProgressDialog.dismiss();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            statListView.stopRefresh();
                            ViewUtils.showToast(StatisticsActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendGetOthersDataRequest()
    {
        OthersStatRequest request = new OthersStatRequest(year, month, categoryID, tagID, userID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final OthersStatResponse response = new OthersStatResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            lastUpdateTime = Utils.getCurrentTime();
                            resetView();
                            drawOverviewLayout(response.getTotalAmount(), -1);
                            drawCategoryPie(response.getStatCategoryList());
                            drawCurrency(response.getCurrencyData());
                            drawTagBar(response.getStatTagList());
                            drawMember(response.getStatUserList());
                            adapter.notifyDataSetChanged();
                            statListView.stopRefresh();
                            statListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
                            ReimProgressDialog.dismiss();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            statListView.stopRefresh();
                            ViewUtils.showToast(StatisticsActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}