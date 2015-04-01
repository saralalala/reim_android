package com.rushucloud.reim;

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

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.Category;
import classes.StatCategory;
import classes.StatTag;
import classes.StatUser;
import classes.Tag;
import classes.User;
import classes.adapter.StatisticsListViewAdapter;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimBar;
import classes.widget.ReimCircle;
import classes.widget.ReimPie;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import netUtils.HttpConnectionCallback;
import netUtils.request.statistics.OthersStatRequest;
import netUtils.response.statistics.OthersStatResponse;

public class StatisticsActivity extends Activity
{
    private StatisticsListViewAdapter adapter;
    private XListView statListView;

    private LinearLayout overviewLayout;
    private TextView overviewTextView;
    private RelativeLayout pieLayout;
    private FrameLayout statContainer;
    private TextView totalTextView;
    private TextView unitTextView;
    private LinearLayout categoryLayout;
    private LinearLayout leftCategoryLayout;
    private LinearLayout rightCategoryLayout;
    private RelativeLayout tagTitleLayout;
    private TextView tagTextView;
    private LinearLayout tagLayout;
    private RelativeLayout memberTitleLayout;
    private LinearLayout memberLayout;

    private DBManager dbManager;

    private int colorR[] = {56, 60, 181, 232, 181, 141, 62, 255, 138, 238, 125};
    private int colorG[] = {56, 183, 112, 140, 184, 192, 119, 196, 118, 149, 173};
    private int colorB[] = {56, 152, 178, 191, 69, 219, 219, 0, 203, 50, 165};
    private int colorRDiff[] = {169, 137, 52, 16, 52, 80, 135, 0, 82, 12, 91};
    private int colorGDiff[] = {169, 51, 100, 81, 50, 44, 95, 41, 96, 74, 58};
    private int colorBDiff[] = {169, 72, 54, 45, 131, 25, 25, 179, 37, 144, 63};
    private int year;
    private int month;
    private int categoryID;
    private int tagID;
    private int userID;

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
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

    private void initData()
    {
        dbManager = DBManager.getDBManager();

        Bundle bundle = getIntent().getExtras();
        year = bundle.getInt("year");
        month = bundle.getInt("month");
        categoryID = bundle.getInt("categoryID", -1);
        tagID = bundle.getInt("tagID", -1);
        userID = bundle.getInt("userID", -1);
    }

	private void initView()
	{
		getActionBar().hide();

        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        if (categoryID != -1)
        {
            Category category = dbManager.getCategory(categoryID);
            if (category != null)
            {
                titleTextView.setText(getString(R.string.stat_category) + category.getName());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                finish();
            }
        }
        else if (tagID != -1)
        {
            Tag tag = dbManager.getTag(tagID);
            if (tag != null)
            {
                titleTextView.setText(getString(R.string.stat_tag) + tag.getName());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                finish();
            }
        }
        else if (userID != -1)
        {
            User user = dbManager.getUser(userID);
            if (user != null)
            {
                titleTextView.setText(getString(R.string.stat_user) + user.getNickname());
            }
            else
            {
                ViewUtils.showToast(this, R.string.failed_to_read_data);
                finish();
            }
        }
        else
        {
            ViewUtils.showToast(this, R.string.failed_to_read_data);
            finish();
        }

        View view = View.inflate(this, R.layout.view_stat_second, null);

        overviewLayout = (LinearLayout) view.findViewById(R.id.overviewLayout);
        overviewTextView = (TextView) view.findViewById(R.id.overviewTextView);
        pieLayout = (RelativeLayout) view.findViewById(R.id.pieLayout);
        statContainer = (FrameLayout) view.findViewById(R.id.statContainer);
        totalTextView = (TextView) view.findViewById(R.id.totalTextView);
        unitTextView = (TextView) view.findViewById(R.id.unitTextView);
        categoryLayout = (LinearLayout) view.findViewById(R.id.categoryLayout);
        leftCategoryLayout = (LinearLayout) view.findViewById(R.id.leftCategoryLayout);
        rightCategoryLayout = (LinearLayout) view.findViewById(R.id.rightCategoryLayout);
        tagTitleLayout = (RelativeLayout) view.findViewById(R.id.tagTitleLayout);
        tagTextView = (TextView) view.findViewById(R.id.tagTextView);
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
                if (PhoneUtils.isNetworkConnected())
                {
                    sendGetDataRequest();
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
        tagLayout.removeAllViews();
        memberLayout.removeAllViews();
    }

    private void drawCategoryPie(List<StatCategory> categoryList)
    {
        if (categoryID != -1 && categoryList.size() <= 1)
        {
            overviewLayout.setVisibility(View.VISIBLE);
            pieLayout.setVisibility(View.GONE);
            categoryLayout.setVisibility(View.GONE);

            double totalAmount = 0;
            for (StatCategory category : categoryList)
            {
                totalAmount += category.getAmount();
            }
            overviewTextView.setText(Utils.formatDouble(totalAmount));
        }
        else
        {
            overviewLayout.setVisibility(View.GONE);
            pieLayout.setVisibility(View.VISIBLE);
            categoryLayout.setVisibility(View.VISIBLE);

            SparseArray<List<StatCategory>> categoryArray = new SparseArray<List<StatCategory>>();

            double totalAmount = 0;
            StatCategory deletedCategory = new StatCategory();
            deletedCategory.setName(getString(R.string.deleted_category));
            for (StatCategory category : categoryList)
            {
                Category localCategory = dbManager.getCategory(category.getCategoryID());
                totalAmount += category.getAmount();
                if (localCategory != null)
                {
                    int iconID = localCategory.getIconID() < 1? 0 : localCategory.getIconID();
                    category.setIconID(iconID);
                    category.setName(localCategory.getName());
                    if (categoryArray.indexOfKey(localCategory.getIconID()) < 0)
                    {
                        categoryArray.put(localCategory.getIconID(), new ArrayList<StatCategory>());
                    }
                    List<StatCategory> list = categoryArray.get(localCategory.getIconID());
                    list.add(category);
                }
                else
                {
                    deletedCategory.setAmount(deletedCategory.getAmount() + category.getAmount());
                }
            }
            if (deletedCategory.getAmount() > 0)
            {
                if (categoryArray.indexOfKey(0) < 0)
                {
                    categoryArray.put(0, new ArrayList<StatCategory>());
                }
                List<StatCategory> list = categoryArray.get(0);
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

            int legendWidth = ViewUtils.dpToPixel(this, 10);
            int count = 0;
            for (int i = 0; i < categoryArray.size(); i++)
            {
                int key = categoryArray.keyAt(i);
                List<StatCategory> categories = categoryArray.get(key);
                int rDiff = categories.size() == 1? colorRDiff[key] : colorRDiff[key] / (categories.size() - 1);
                int gDiff = categories.size() == 1? colorGDiff[key] : colorGDiff[key] / (categories.size() - 1);
                int bDiff = categories.size() == 1? colorBDiff[key] : colorBDiff[key] / (categories.size() - 1);
                for (int j = 0; j < categories.size(); j++)
                {
                    StatCategory category = categories.get(j);
                    if (key != 0)
                    {
                        category.setColor(Color.rgb(colorR[key] + j * rDiff, colorG[key] + j * gDiff, colorB[key] + j * bDiff));
                    }
                    else
                    {
                        category.setColor(Color.rgb(colorR[key] + colorRDiff[key] - j * rDiff,
                                                    colorG[key] + colorGDiff[key] - j * gDiff,
                                                    colorB[key] + colorBDiff[key] - j * bDiff));
                    }

                    float angle = i == categoryArray.size() - 1 && j == categories.size() - 1?
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
                    amountTextView.setText(Utils.formatDouble(category.getAmount()));

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

    private void drawTagBar(List<StatTag> tagList)
    {
        if (tagID != -1)
        {
            tagTitleLayout.setVisibility(View.GONE);
            tagTextView.setVisibility(View.GONE);
            tagLayout.setVisibility(View.GONE);
        }
        else if (tagList.isEmpty())
        {
            tagTitleLayout.setVisibility(View.VISIBLE);
            tagTextView.setVisibility(View.VISIBLE);
            tagLayout.setVisibility(View.GONE);
        }
        else
        {
            tagTitleLayout.setVisibility(View.VISIBLE);
            tagTextView.setVisibility(View.GONE);
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
        if (userID != -1)
        {
            memberTitleLayout.setVisibility(View.GONE);
            memberLayout.setVisibility(View.GONE);
        }
        else
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
                        amountTextView.setText(Utils.formatDouble(user.getAmount()));

                        memberLayout.addView(view);
                    }
                }
            }
        }
    }

    private void sendGetDataRequest()
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
                            resetView();
                            drawCategoryPie(response.getStatCategoryList());
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