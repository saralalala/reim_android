package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.CategoryExpandableListAdapter;
import classes.model.Category;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class PickCategoryActivity extends Activity
{
    // Widgets
    private CategoryExpandableListAdapter adapter;

    // Local Data
    private DBManager dbManager;
    private List<Category> categoryList = null;
    private List<List<Category>> subCategoryList = null;
    private List<Boolean> check = null;
    private List<List<Boolean>> subCheck = null;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reim_category);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickCategoryActivity");
        MobclickAgent.onResume(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickCategoryActivity");
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                boolean flag = false;
                Category category = null;
                for (int i = 0; i < check.size(); i++)
                {
                    if (check.get(i))
                    {
                        category = categoryList.get(i);
                        flag = true;
                        break;
                    }
                }

                if (!flag)
                {
                    for (int i = 0; i < subCheck.size(); i++)
                    {
                        List<Boolean> booleans = subCheck.get(i);
                        for (int j = 0; j < booleans.size(); j++)
                        {
                            if (booleans.get(j))
                            {
                                category = subCategoryList.get(i).get(j);
                                flag = true;
                                break;
                            }
                        }
                        if (flag)
                        {
                            break;
                        }
                    }
                }

                Intent intent = new Intent();
                intent.putExtra("category", category);
                ViewUtils.goBackWithResult(PickCategoryActivity.this, intent);
            }
        });

        adapter = new CategoryExpandableListAdapter(this, categoryList, subCategoryList, check, subCheck);

        ExpandableListView categoryListView = (ExpandableListView) findViewById(R.id.categoryListView);
        categoryListView.setAdapter(adapter);
        categoryListView.setOnGroupClickListener(new OnGroupClickListener()
        {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                resetCheck();
                check.set(groupPosition, true);
                adapter.setCheck(check, subCheck);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        categoryListView.setOnChildClickListener(new OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id)
            {
                resetCheck();
                subCheck.get(groupPosition).set(childPosition, true);
                adapter.setCheck(check, subCheck);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        if (PhoneUtils.isNetworkConnected())
        {
            for (Category category : categoryList)
            {
                if (category.hasUndownloadedIcon())
                {
                    sendDownloadCategoryIconRequest(category);
                }
            }

            for (List<Category> categories : subCategoryList)
            {
                for (Category category : categories)
                {
                    if (category.hasUndownloadedIcon())
                    {
                        sendDownloadCategoryIconRequest(category);
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

        Category chosenCategory = (Category) getIntent().getSerializableExtra("category");

        readCategoryList();

        check = Category.getCategoryCheck(categoryList, chosenCategory);
        subCheck = new ArrayList<>();
        for (List<Category> categories : subCategoryList)
        {
            subCheck.add(Category.getCategoryCheck(categories, chosenCategory));
        }
    }

    private void readCategoryList()
    {
        int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
        int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
        if (categoryList == null)
        {
            categoryList = dbManager.getUserCategories(currentUserID);
        }
        else
        {
            categoryList.clear();
            categoryList.addAll(dbManager.getUserCategories(currentUserID));
        }

        if (subCategoryList == null)
        {
            subCategoryList = new ArrayList<>();
        }
        else
        {
            subCategoryList.clear();
        }

        for (Category category : categoryList)
        {
            List<Category> subCategories = dbManager.getSubCategories(category.getServerID(), currentGroupID);
            subCategoryList.add(subCategories);
        }
    }

    private void resetCheck()
    {
        for (int i = 0; i < check.size(); i++)
        {
            check.set(i, false);
        }

        for (List<Boolean> booleans : subCheck)
        {
            for (int i = 0; i < booleans.size(); i++)
            {
                booleans.set(i, false);
            }
        }
    }

    // Network
    private void sendDownloadCategoryIconRequest(final Category category)
    {
        DownloadImageRequest request = new DownloadImageRequest(category.getIconID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
                    category.setLocalUpdatedDate(Utils.getCurrentTime());
                    category.setServerUpdatedDate(category.getLocalUpdatedDate());
                    dbManager.updateCategory(category);

                    readCategoryList();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.setCategory(categoryList, subCategoryList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}
