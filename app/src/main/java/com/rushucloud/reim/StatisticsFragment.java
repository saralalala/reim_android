package com.rushucloud.reim;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;

import classes.Category;
import classes.StatCategory;
import classes.StatTag;
import classes.StatUser;
import classes.Tag;
import classes.User;
import classes.adapter.StatisticsListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimBar;
import classes.widget.ReimPie;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import classes.widget.XListView.IXListViewListener;
import netUtils.HttpConnectionCallback;
import netUtils.request.statistics.MineStatRequest;
import netUtils.request.statistics.OthersStatRequest;
import netUtils.response.statistics.MineStatResponse;
import netUtils.response.statistics.OthersStatResponse;

public class StatisticsFragment extends Fragment
{
	private static final int GET_DATA_INTERVAL = 600;

    private View view;
    private TextView statTitleTextView;
    private RelativeLayout titleLayout;
    private TextView myTitleTextView;
    private TextView othersTitleTextView;
	private StatisticsListViewAdapter mineAdapter;
    private StatisticsListViewAdapter othersAdapter;
    private XListView statListView;

	private FrameLayout mineStatContainer;
	private TextView mainAmountTextView;
	private TextView mineUnitTextView;
	private TextView ongoingPercentTextView;
	private TextView newPercentTextView;
	private TextView monthTotalTextView;
	private TextView totalUnitTextView;
	private LinearLayout monthLayout;
	private LinearLayout categoryLayout;

    private FrameLayout othersStatContainer;
    private TextView othersTotalTextView;
    private TextView othersUnitTextView;
    private LinearLayout othersCategoryLayout;
    private LinearLayout tagLayout;
    private LinearLayout memberLayout;

	private AppPreference appPreference;
	private DBManager dbManager;

    private int year;
    private int month;
	private boolean hasInit = false;
	private boolean hasMineData = false;
    private boolean hasOthersData = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_statistics, container, false);
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
		MobclickAgent.onPageStart("StatisticsFragment");
		if (!hasInit)
		{
			appPreference = AppPreference.getAppPreference();
			dbManager = DBManager.getDBManager();
			initView();
			hasInit = true;
		}

        if (getUserVisibleHint() && needToGetMineData())
        {
            ReimProgressDialog.show();
            sendGetMineDataRequest();
        }
	}

	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("StatisticsFragment");
	}

	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && hasInit)
		{
            setListView(ReimApplication.getStatTabIndex());
            if (ReimApplication.getStatTabIndex() == 0 && needToGetMineData())
            {
                ReimProgressDialog.show();
                sendGetMineDataRequest();
            }
            else if (ReimApplication.getStatTabIndex() == 1 && needToGetOthersData())
            {
                ReimProgressDialog.show();
                sendGetOthersDataRequest();
            }
		}
	}
	
	private void initView()
	{
        statTitleTextView = (TextView) view.findViewById(R.id.statTitleTextView);
        titleLayout = (RelativeLayout) view.findViewById(R.id.titleLayout);

        myTitleTextView = (TextView) view.findViewById(R.id.myTitleTextView);
        myTitleTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setListView(0);
            }
        });

        othersTitleTextView = (TextView) view.findViewById(R.id.othersTitleTextView);
        othersTitleTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setListView(1);
            }
        });

        initMineView();

		statListView = (XListView) getActivity().findViewById(R.id.statListView);
		statListView.setAdapter(mineAdapter);
		statListView.setXListViewListener(new IXListViewListener()
		{
			public void onRefresh()
			{
                if (PhoneUtils.isNetworkConnected())
                {
                    if (ReimApplication.getStatTabIndex() == 0)
                    {
                        sendGetMineDataRequest();
                    }
                    else
                    {
                        sendGetOthersDataRequest();
                    }
                }
                else
                {
                    statListView.stopRefresh();
                    ViewUtils.showToast(getActivity(), R.string.error_get_data_network_unavailable);
                }
			}
			
			public void onLoadMore()
			{
				
			}
		});
		statListView.setPullRefreshEnable(true);
		statListView.setPullLoadEnable(false);
		statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetMineStatTime()));
	}

    private void initMineView()
    {
        View mineView = View.inflate(getActivity(), R.layout.view_stat_mine, null);

        mineStatContainer = (FrameLayout) mineView.findViewById(R.id.mineStatContainer);

        mainAmountTextView = (TextView) mineView.findViewById(R.id.mainAmountTextView);
        mainAmountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        mineUnitTextView = (TextView) mineView.findViewById(R.id.mineUnitTextView);

        newPercentTextView = (TextView) mineView.findViewById(R.id.newPercentTextView);
        newPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        ongoingPercentTextView = (TextView) mineView.findViewById(R.id.ongoingPercentTextView);
        ongoingPercentTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        monthTotalTextView = (TextView) mineView.findViewById(R.id.monthTotalTextView);
        totalUnitTextView = (TextView) mineView.findViewById(R.id.totalUnitTextView);
        monthLayout = (LinearLayout) mineView.findViewById(R.id.monthLayout);
        categoryLayout = (LinearLayout) mineView.findViewById(R.id.categoryLayout);

        mineAdapter = new StatisticsListViewAdapter(mineView);
    }

    private void initOthersView()
    {
        year = Utils.getCurrentYear();
        month = Utils.getCurrentMonth();

        View othersView = View.inflate(getActivity(), R.layout.view_stat_others, null);

        final TextView monthTextView = (TextView) othersView.findViewById(R.id.monthTextView);
        monthTextView.setText(Utils.getMonthString(year, month));

        ImageView leftArrowImageView = (ImageView) othersView.findViewById(R.id.leftArrowImageView);
        leftArrowImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                month = month == 1? 12 : month - 1;
                if (month == 12)
                {
                    year--;
                }
                monthTextView.setText(Utils.getMonthString(year, month));
                ReimProgressDialog.show();
                sendGetOthersDataRequest();
            }
        });

        ImageView rightArrowImageView = (ImageView) othersView.findViewById(R.id.rightArrowImageView);
        rightArrowImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (year == Utils.getCurrentYear() && month == Utils.getCurrentMonth())
                {
                    ViewUtils.showToast(getActivity(), R.string.prompt_no_lastest_data);
                }
                else
                {
                    month = month == 12? 1 : month + 1;
                    if (month == 1)
                    {
                        year++;
                    }
                    monthTextView.setText(Utils.getMonthString(year, month));
                    ReimProgressDialog.show();
                    sendGetOthersDataRequest();
                }
            }
        });

        othersStatContainer = (FrameLayout) othersView.findViewById(R.id.othersStatContainer);

        othersTotalTextView = (TextView) othersView.findViewById(R.id.othersTotalTextView);
        othersTotalTextView.setTypeface(ReimApplication.TypeFaceAleoLight);

        othersUnitTextView = (TextView) othersView.findViewById(R.id.othersUnitTextView);

        othersCategoryLayout = (LinearLayout) othersView.findViewById(R.id.categoryLayout);
        tagLayout = (LinearLayout) othersView.findViewById(R.id.tagLayout);
        memberLayout = (LinearLayout) othersView.findViewById(R.id.memberLayout);

        othersAdapter = new StatisticsListViewAdapter(othersView);
    }

	private void resetMineView()
	{
        mineStatContainer.removeAllViews();
		monthLayout.removeAllViews();
		categoryLayout.removeAllViews();
	}

    private void resetOthersView()
    {
        othersStatContainer.removeAllViews();
        othersCategoryLayout.removeAllViews();
        tagLayout.removeAllViews();
        memberLayout.removeAllViews();
    }

    private void setListView(int index)
    {
        ReimApplication.setStatTabIndex(index);
        if (index == 0)
        {
            myTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));
            othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
            statListView.setAdapter(mineAdapter);
            statListView.stopRefresh();
            statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetMineStatTime()));
        }
        else
        {
            myTitleTextView.setTextColor(ViewUtils.getColor(R.color.hint_light));
            othersTitleTextView.setTextColor(ViewUtils.getColor(R.color.major_light));

            if (othersAdapter == null)
            {
                initOthersView();
            }
            statListView.setAdapter(othersAdapter);
            statListView.stopRefresh();
            statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetOthersStatTime()));
        }
        refreshData();
    }

	private boolean needToGetMineData()
	{
		return !hasMineData || Utils.getCurrentTime() - appPreference.getLastGetMineStatTime() > GET_DATA_INTERVAL;
	}

    private boolean needToGetOthersData()
    {
        return !hasOthersData || Utils.getCurrentTime() - appPreference.getLastGetOthersStatTime() > GET_DATA_INTERVAL;
    }

	private void refreshData()
	{
		if (PhoneUtils.isNetworkConnected())
		{
            if (ReimApplication.getStatTabIndex() == 0 && needToGetMineData())
            {
                ReimProgressDialog.show();
                sendGetMineDataRequest();
            }
            else if (ReimApplication.getStatTabIndex() == 1 && needToGetOthersData())
            {
                ReimProgressDialog.show();
                sendGetOthersDataRequest();
            }
		}
		else
		{
			statListView.stopRefresh();
			ViewUtils.showToast(getActivity(), R.string.error_get_data_network_unavailable);
		}
	}
	
	private void drawCostPie(double ongoingAmount, double newAmount)
	{
        double totalAmount = ongoingAmount + newAmount;
		double ongoingRatio, newRatio;
		if (totalAmount == 0)
		{
			ongoingRatio = newRatio = 0;
		}
		else
		{
			ongoingRatio = Utils.roundDouble(ongoingAmount * 100 / totalAmount);
			newRatio = 100 - ongoingRatio;
		}
		
		if (totalAmount < 10000)
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount));
            mineUnitTextView.setVisibility(View.GONE);
		}
		else if (totalAmount < 10000000)
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount / 10000));
            mineUnitTextView.setText(R.string.ten_thousand);
		}
		else
		{
			mainAmountTextView.setText(Utils.formatDouble(totalAmount / 100000000));
            mineUnitTextView.setText(R.string.one_hundred_million);
		}
		ongoingPercentTextView.setText(Utils.formatDouble(ongoingRatio) + getString(R.string.percent));
		newPercentTextView.setText(Utils.formatDouble(newRatio) + getString(R.string.percent));

		float totalAngle = 262;
		float startAngle = 139;
        float ongoingAngle = (float) ongoingRatio * totalAngle / 100;
		float newAngle = (float) newRatio * totalAngle / 100;

        // Draw new pie
        ReimPie newReimPie = new ReimPie(getActivity(), startAngle, newAngle, mineStatContainer.getWidth(), ViewUtils.getColor(R.color.stat_new));
        mineStatContainer.addView(newReimPie);

		// Draw ongoing pie
        startAngle += newAngle;
		ReimPie ongoingReimPie = new ReimPie(getActivity(), startAngle, ongoingAngle, mineStatContainer.getWidth(), ViewUtils.getColor(R.color.stat_ongoing));
        mineStatContainer.addView(ongoingReimPie);
	}

	private void drawMonthBar(HashMap<String, Double> monthsData)
	{
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
            monthTotalTextView.setText(Double.toString(total));
			
			for (String month : monthsData.keySet())
			{
				Double data = monthsData.get(month);
				ReimBar monthBar = new ReimBar(getActivity(), data / max);
				
				View view = View.inflate(getActivity(), R.layout.list_month_stat, null);
				
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
	
	private void drawCategory(List<StatCategory> categoryList)
	{
		if (!categoryList.isEmpty())
		{			
			for (StatCategory category : categoryList)
			{
				Category localCategory = dbManager.getCategory(category.getCategoryID());
				if (localCategory != null)
				{
					View view = View.inflate(getActivity(), R.layout.list_category_stat, null);

                    ImageView iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
                    ViewUtils.setImageViewBitmap(localCategory, iconImageView);

					TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
                    nameTextView.setText(localCategory.getName());
					
					TextView countTextView = (TextView) view.findViewById(R.id.countTextView);
					countTextView.setText(Integer.toString(category.getItems().size()));
					
					TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
					amountTextView.setText(Utils.formatDouble(category.getAmount()));
					
					categoryLayout.addView(view);					
				}
			}
		}	
	}

    private void drawCategoryPie(List<StatCategory> categoryList)
    {
        double totalAmount = 0;
        for (StatCategory category : categoryList)
        {
            totalAmount += category.getAmount();
        }

        if (totalAmount < 10000)
        {
            othersTotalTextView.setText(Utils.formatDouble(totalAmount));
            othersUnitTextView.setVisibility(View.GONE);
        }
        else if (totalAmount < 10000000)
        {
            othersTotalTextView.setText(Utils.formatDouble(totalAmount / 10000));
            othersUnitTextView.setText(R.string.ten_thousand);
        }
        else
        {
            othersTotalTextView.setText(Utils.formatDouble(totalAmount / 100000000));
            othersUnitTextView.setText(R.string.one_hundred_million);
        }

        float startAngle = -90;

        // Draw new pie
//        ReimPie newReimPie = new ReimPie(getActivity(), startAngle, newAngle, mineStatContainer.getWidth(), ViewUtils.getColor(R.color.stat_new));
//        mineStatContainer.addView(newReimPie);

        // Draw ongoing pie
//        startAngle += newAngle;
//        ReimPie ongoingReimPie = new ReimPie(getActivity(), startAngle, ongoingAngle, mineStatContainer.getWidth(), ViewUtils.getColor(R.color.stat_ongoing));
//        mineStatContainer.addView(ongoingReimPie);
    }

    private void drawTagBar(List<StatTag> tagList)
    {
        if (!tagList.isEmpty())
        {
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
                    ReimBar tagBar = new ReimBar(getActivity(), amount / max);

                    View view = View.inflate(getActivity(), R.layout.list_tag_stat, null);

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
        if (!userList.isEmpty())
        {
            for (StatUser user : userList)
            {
                User localUser = dbManager.getUser(user.getUserID());
                if (localUser != null)
                {
                    View view = View.inflate(getActivity(), R.layout.list_member_stat, null);

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

	private void sendGetMineDataRequest()
	{
		MineStatRequest request = new MineStatRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final MineStatResponse response = new MineStatResponse(httpResponse);
				if (response.getStatus())
				{
					hasMineData = true;

					appPreference.setLastGetMineStatTime(Utils.getCurrentTime());
					appPreference.saveAppPreference();
					
					getActivity().runOnUiThread(new Runnable()
					{
						public void run()
						{
                            if (!response.hasStaffData())
                            {
                                statTitleTextView.setVisibility(View.VISIBLE);
                                titleLayout.setVisibility(View.GONE);
                            }
                            else
                            {
                                statTitleTextView.setVisibility(View.GONE);
                                titleLayout.setVisibility(View.VISIBLE);
                            }

							resetMineView();
							drawCostPie(response.getOngoingAmount(), response.getNewAmount());
							drawMonthBar(response.getMonthsData());
							drawCategory(response.getStatCategoryList());
							mineAdapter.notifyDataSetChanged();
							statListView.stopRefresh();
							statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetMineStatTime()));
							ReimProgressDialog.dismiss();
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
							statListView.stopRefresh();
							ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
						}
					});					
				}
			}
		});
	}

    private void sendGetOthersDataRequest()
    {
        OthersStatRequest request = new OthersStatRequest(year, month);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final OthersStatResponse response = new OthersStatResponse(httpResponse);
                if (response.getStatus())
                {
                    hasOthersData = true;

                    appPreference.setLastGetOthersStatTime(Utils.getCurrentTime());
                    appPreference.saveAppPreference();

                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            resetOthersView();
                            drawCategoryPie(response.getStatCategoryList());
                            drawTagBar(response.getStatTagList());
                            drawMember(response.getStatUserList());
                            othersAdapter.notifyDataSetChanged();
                            statListView.stopRefresh();
                            statListView.setRefreshTime(Utils.secondToStringUpToMinute(appPreference.getLastGetOthersStatTime()));
                            ReimProgressDialog.dismiss();
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
                            statListView.stopRefresh();
                            ViewUtils.showToast(getActivity(), R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}