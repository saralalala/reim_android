package com.rushucloud.reim.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.CategoryExpandableListAdapter;
import classes.model.Category;
import classes.model.SetOfBook;
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
    private List<SetOfBook> setOfBookList = new ArrayList<>();
    private String[] setOfBookNameList;
    private List<List<Category>> categoryList = new ArrayList<>();
    private List<List<List<Category>>> subCategoryList = new ArrayList<>();
    private List<List<Boolean>> check = new ArrayList<>();
    private List<List<List<Boolean>>> subCheck = new ArrayList<>();
    private int sobIndex = 0;

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
                    List<Boolean> checkList = check.get(i);
                    for (int j = 0; j < checkList.size(); j++)
                    {
                        if (checkList.get(j))
                        {
                            category = categoryList.get(i).get(j);
                            flag = true;
                            break;
                        }
                    }
                }

                if (!flag)
                {
                    for (int i = 0; i < subCheck.size(); i++)
                    {
                        List<List<Boolean>> sobSubCheck = subCheck.get(i);
                        for (int j = 0; j < sobSubCheck.size(); j++)
                        {
                            List<Boolean> subCheckList = sobSubCheck.get(j);
                            for (int k = 0; k < subCheckList.size(); k++)
                            {
                                if (subCheckList.get(k))
                                {
                                    category = subCategoryList.get(i).get(j).get(k);
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag)
                            {
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

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, setOfBookNameList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner sobSpinner = (Spinner) findViewById(R.id.sobSpinner);
        sobSpinner.setAdapter(spinnerAdapter);
        sobSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                sobIndex = position;
                adapter.setCategory(categoryList.get(sobIndex), subCategoryList.get(sobIndex));
                adapter.setCheck(check.get(sobIndex), subCheck.get(sobIndex));
                adapter.notifyDataSetChanged();
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        if (setOfBookList.isEmpty())
        {
            titleTextView.setVisibility(View.VISIBLE);
            sobSpinner.setVisibility(View.GONE);
        }
        else
        {
            titleTextView.setVisibility(View.GONE);
            sobSpinner.setVisibility(View.VISIBLE);
        }

        adapter = new CategoryExpandableListAdapter(this, categoryList.get(0), subCategoryList.get(0),
                                                    check.get(0), subCheck.get(0));

        ExpandableListView categoryListView = (ExpandableListView) findViewById(R.id.categoryListView);
        categoryListView.setAdapter(adapter);
        categoryListView.setOnGroupClickListener(new OnGroupClickListener()
        {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                resetCheck();
                check.get(sobIndex).set(groupPosition, true);
                adapter.setCheck(check.get(sobIndex), subCheck.get(sobIndex));
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
                subCheck.get(sobIndex).get(groupPosition).set(childPosition, true);
                adapter.setCheck(check.get(sobIndex), subCheck.get(sobIndex));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        if (PhoneUtils.isNetworkConnected())
        {
            for (List<Category> categories : categoryList)
            {
                for (Category category : categories)
                {
                    if (category.hasUndownloadedIcon())
                    {
                        sendDownloadCategoryIconRequest(category);
                    }
                }
            }

            for (List<List<Category>> subCategories : subCategoryList)
            {
                for (List<Category> categories : subCategories)
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

        setOfBookList = dbManager.getUserSetOfBooks(AppPreference.getAppPreference().getCurrentUserID());
        setOfBookNameList = new String[setOfBookList.size()];
        for (int i = 0; i < setOfBookList.size(); i++)
        {
            setOfBookNameList[i] = setOfBookList.get(i).getName();
        }

        readCategoryList();

        if (setOfBookList.isEmpty())
        {
            check.add(Category.getCategoryCheck(categoryList.get(0), chosenCategory));
            subCheck.add(new ArrayList<List<Boolean>>());

            for (List<Category> categories : subCategoryList.get(0))
            {
                subCheck.get(0).add(Category.getCategoryCheck(categories, chosenCategory));
            }
        }
        else
        {
            for (List<Category> categories : categoryList)
            {
                check.add(Category.getCategoryCheck(categories, chosenCategory));
            }

            for (List<List<Category>> sobSubCategories : subCategoryList)
            {
                List<List<Boolean>> sobSubCheckList = new ArrayList<>();
                for (List<Category> subCategories : sobSubCategories)
                {
                    sobSubCheckList.add(Category.getCategoryCheck(subCategories, chosenCategory));
                }
                subCheck.add(sobSubCheckList);
            }
        }
    }

    private void readCategoryList()
    {
        int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
        int currentUserID = AppPreference.getAppPreference().getCurrentUserID();

        categoryList.clear();
        subCategoryList.clear();
        if (setOfBookList.isEmpty())
        {
            categoryList.add(dbManager.getUserCategories(currentUserID, currentGroupID));
            subCategoryList.add(new ArrayList<List<Category>>());

            for (Category category : categoryList.get(0))
            {
                List<Category> subCategories = dbManager.getSubCategories(category.getServerID(), currentGroupID);
                subCategoryList.get(0).add(subCategories);
            }
        }
        else
        {
            for (SetOfBook setOfBook : setOfBookList)
            {
                categoryList.add(dbManager.getSetOfBookCategories(setOfBook.getServerID()));
            }

            for (List<Category> categories : categoryList)
            {
                List<List<Category>> sobSubCategoryList = new ArrayList<>();
                for (Category category : categories)
                {
                    List<Category> subCategories = dbManager.getSubCategories(category.getServerID(), currentGroupID);
                    sobSubCategoryList.add(subCategories);
                }
                subCategoryList.add(sobSubCategoryList);
            }
        }
    }

    private void resetCheck()
    {
        for (List<Boolean> checkList : check)
        {
            for (int i = 0; i < checkList.size(); i++)
            {
                checkList.set(i, false);
            }
        }

        for (List<List<Boolean>> subCheckList : subCheck)
        {
            for (List<Boolean> booleans : subCheckList)
            {
                for (int i = 0; i < booleans.size(); i++)
                {
                    booleans.set(i, false);
                }
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
                            boolean isInCurrentSetOfBook = category.isInCategoryList(categoryList.get(sobIndex));
                            if (!isInCurrentSetOfBook)
                            {
                                for (List<Category> subCategories : subCategoryList.get(sobIndex))
                                {
                                    if (category.isInCategoryList(subCategories))
                                    {
                                        isInCurrentSetOfBook = true;
                                        break;
                                    }
                                }
                            }

                            if (isInCurrentSetOfBook)
                            {
                                adapter.setCategory(categoryList.get(sobIndex), subCategoryList.get(sobIndex));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }
}
