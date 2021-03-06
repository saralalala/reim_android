package com.rushucloud.reim.statistics;

import android.app.Activity;
import android.content.Intent;
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
import classes.model.StatDepartment;
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
    private RelativeLayout statusTitleLayout;
    private LinearLayout statusLayout;
    private RelativeLayout currencyTitleLayout;
    private LinearLayout currencyLayout;
    private RelativeLayout departmentTitleLayout;
    private LinearLayout departmentLayout;
    private RelativeLayout tagTitleLayout;
    private LinearLayout tagLayout;
    private RelativeLayout memberTitleLayout;
    private LinearLayout memberLayout;

    // Local Data
    private DBManager dbManager;
    private boolean mineData;
    private int year;
    private int month;
    private int categoryID;
    private int tagID;
    private int userID;
    private int status;
    private String currencyCode;
    private int departmentID;
    private String departmentName;
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
        ReimProgressDialog.setContext(this);

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
        if (categoryID != 0)
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
        else if (status != -2)
        {
            int statusType = status == 2 ? R.string.status_approved : R.string.status_finished;
            titleTextView.setText(getString(R.string.stat_status) + ViewUtils.getString(statusType));
        }
        else if (!currencyCode.isEmpty())
        {
            Currency currency = dbManager.getCurrency(currencyCode);
            if (currency != null)
            {
                titleTextView.setText(getString(R.string.stat_currency) + currency.getName());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                goBack();
            }
        }
        else if (departmentID != 0)
        {
            titleTextView.setText(getString(R.string.stat_department) + departmentName);
        }
        else if (year != 0)
        {
            titleTextView.setText(Utils.getMonthString(year, month));
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

        statusTitleLayout = (RelativeLayout) view.findViewById(R.id.statusTitleLayout);
        statusLayout = (LinearLayout) view.findViewById(R.id.statusLayout);

        currencyTitleLayout = (RelativeLayout) view.findViewById(R.id.currencyTitleLayout);
        currencyLayout = (LinearLayout) view.findViewById(R.id.currencyLayout);

        departmentTitleLayout = (RelativeLayout) view.findViewById(R.id.departmentTitleLayout);
        departmentLayout = (LinearLayout) view.findViewById(R.id.departmentLayout);

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
        statusLayout.removeAllViews();
        currencyLayout.removeAllViews();
        departmentLayout.removeAllViews();
        tagLayout.removeAllViews();
        memberLayout.removeAllViews();
    }

    // View - draw mine
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
                    ReimBar monthBar = new ReimBar(this, data / max);

                    View view = View.inflate(this, R.layout.list_month_stat, null);

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

    // View - draw others
    private void drawStatus(HashMap<String, Double> statusData)
    {
        if (status == -2 && !statusData.isEmpty())
        {
            statusTitleLayout.setVisibility(View.VISIBLE);
            statusLayout.setVisibility(View.VISIBLE);

            for (final String status : statusData.keySet())
            {
                View view = View.inflate(this, R.layout.list_status_stat, null);

                TextView statusTextView = (TextView) view.findViewById(R.id.statusTextView);
                statusTextView.setText(status);

                TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
                amountTextView.setText(Utils.formatAmount(statusData.get(status)));

                statusLayout.addView(view);
            }

            View lastView = statusLayout.getChildAt(statusLayout.getChildCount() - 1);
            View divider = lastView.findViewById(R.id.divider);
            divider.setVisibility(View.INVISIBLE);
        }
        else
        {
            statusTitleLayout.setVisibility(View.GONE);
            statusLayout.setVisibility(View.GONE);
        }
    }

    private void drawDepartment(List<StatDepartment> departmentList)
    {
        if (userID == 0 && !departmentList.isEmpty() && StatDepartment.containsDepartment(departmentList))
        {
            departmentTitleLayout.setVisibility(View.VISIBLE);
            departmentLayout.setVisibility(View.VISIBLE);

            for (final StatDepartment department : departmentList)
            {
                View view = View.inflate(this, R.layout.list_department_stat, null);
                if (department.isDepartment())
                {
                    view.setBackgroundResource(R.drawable.list_item_drawable);
                    view.setOnClickListener(new View.OnClickListener()
                    {
                        public void onClick(View v)
                        {
                            Bundle bundle = new Bundle();
                            bundle.putInt("year", year);
                            bundle.putInt("month", month);
                            bundle.putInt("departmentID", department.getDepartmentID());
                            bundle.putString("departmentName", department.getName());
                            Intent intent = new Intent(StatisticsActivity.this, StatisticsActivity.class);
                            intent.putExtras(bundle);
                            ViewUtils.goForward(StatisticsActivity.this, intent);
                        }
                    });
                }

                TextView departmentTextView = (TextView) view.findViewById(R.id.departmentTextView);
                departmentTextView.setText(department.getName());

                TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
                amountTextView.setText(Utils.formatAmount(department.getAmount()));

                departmentLayout.addView(view);
            }

            View lastView = departmentLayout.getChildAt(departmentLayout.getChildCount() - 1);
            View divider = lastView.findViewById(R.id.divider);
            divider.setVisibility(View.INVISIBLE);
        }
        else
        {
            departmentTitleLayout.setVisibility(View.GONE);
            departmentLayout.setVisibility(View.GONE);
        }
    }

    private void drawMember(List<StatUser> userList)
    {
        if (userID == 0 && !userList.isEmpty())
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
                        countTextView.setText(String.format(getString(R.string.item_count), user.getItemCount()));

                        TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
                        amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
                        amountTextView.setText(Utils.formatAmount(user.getAmount()));

                        memberLayout.addView(view);
                    }
                }
            }
        }
    }

    // View - draw both
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
                unitTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                totalTextView.setText(Utils.formatDouble(totalAmount / 100000000));
                unitTextView.setText(R.string.one_hundred_million);
                unitTextView.setVisibility(View.VISIBLE);
            }

            ReimPie reimPie = new ReimPie(this, 0, 360, statContainer.getWidth(), ViewUtils.getColor(R.color.stat_pie), 1);
            statContainer.addView(reimPie);

            float startAngle = -90;

            int legendWidth = ViewUtils.dpToPixel(10);
            int count = 0;
            for (int i = 0; i < categoryArray.size(); i++)
            {
                int iconID = categoryArray.keyAt(i);
                List<StatCategory> categories = categoryArray.get(iconID);
                int rDiff = categories.size() == 1 ? ViewUtils.getCategoryColorRDiff(iconID) :
                                                    ViewUtils.getCategoryColorRDiff(iconID) / (categories.size() - 1);
                int gDiff = categories.size() == 1 ? ViewUtils.getCategoryColorGDiff(iconID) :
                                                    ViewUtils.getCategoryColorGDiff(iconID) / (categories.size() - 1);
                int bDiff = categories.size() == 1 ? ViewUtils.getCategoryColorBDiff(iconID) :
                                                    ViewUtils.getCategoryColorBDiff(iconID) / (categories.size() - 1);
                for (int j = 0; j < categories.size(); j++)
                {
                    StatCategory category = categories.get(j);
                    if (iconID != Constant.DEFAULT_ICON_ID) // light to dark
                    {
                        category.setColor(Color.rgb(ViewUtils.getCategoryColorR(iconID) + j * rDiff,
                                                    ViewUtils.getCategoryColorG(iconID) + j * gDiff,
                                                    ViewUtils.getCategoryColorB(iconID) + j * bDiff));
                    }
                    else // dark to light
                    {
                        category.setColor(Color.rgb(ViewUtils.getCategoryColorR(iconID) +
                                                            ViewUtils.getCategoryColorRDiff(iconID) - j * rDiff,
                                                    ViewUtils.getCategoryColorG(iconID) +
                                                            ViewUtils.getCategoryColorGDiff(iconID) - j * gDiff,
                                                    ViewUtils.getCategoryColorB(iconID) +
                                                            ViewUtils.getCategoryColorBDiff(iconID) - j * bDiff));
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
                    View view = View.inflate(this, R.layout.list_currency_stat, null);

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
        status = bundle.getInt("status", -2);
        currencyCode = bundle.getString("currencyCode", "");
        departmentID = bundle.getInt("departmentID", 0);
        departmentName = bundle.getString("departmentName", "");
    }

    private boolean needToGetData()
    {
        return Utils.getCurrentTime() - lastUpdateTime > Constant.GET_DATA_INTERVAL;
    }

    // Network
    private void sendGetMineDataRequest()
    {
        MineStatDetailRequest request = new MineStatDetailRequest(year, month, tagID, categoryID, currencyCode);
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
        OthersStatRequest request = new OthersStatRequest(year, month, categoryID, tagID, userID, currencyCode, status, departmentID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final OthersStatResponse response = new OthersStatResponse(httpResponse);
                if (response.getStatus() && !isFinishing())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            lastUpdateTime = Utils.getCurrentTime();
                            resetView();
                            drawOverviewLayout(response.getTotalAmount(), -1);
                            drawCategoryPie(response.getStatCategoryList());
                            drawStatus(response.getStatusData());
                            drawCurrency(response.getCurrencyData());
                            drawDepartment(response.getStatDepartmentList());
                            drawTagBar(response.getStatTagList());
                            drawMember(response.getStatUserList());
                            adapter.notifyDataSetChanged();
                            statListView.stopRefresh();
                            statListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
                            ReimProgressDialog.dismiss();
                        }
                    });
                }
                else if (!isFinishing())
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