package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.adapter.CategoryListViewAdapter;
import classes.model.Category;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.category.DeleteCategoryRequest;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.category.DeleteCategoryResponse;
import netUtils.response.common.DownloadImageResponse;

public class SubCategoryActivity extends Activity
{
    // Widgets
    private ListView categoryListView;
    private TextView categoryTextView;
    private CategoryListViewAdapter adapter;
    private PopupWindow operationPopupWindow;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;

    private List<Category> categoryList;
    private Category currentCategory;
    private int parentID;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_category);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SubCategoryActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshListView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SubCategoryActivity");
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

    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        parentID = this.getIntent().getIntExtra("parentID", 0);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(R.string.sub_category);

        TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Category category = new Category();
                category.setParentID(parentID);

                Intent intent = new Intent(SubCategoryActivity.this, EditCategoryActivity.class);
                intent.putExtra("category", category);
                ViewUtils.goForward(SubCategoryActivity.this, intent);
            }
        });

        categoryTextView = (TextView) findViewById(R.id.categoryTextView);

        categoryListView = (ListView) findViewById(R.id.categoryListView);
        categoryListView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentCategory = categoryList.get(position);
                showOperationWindow();
                return false;
            }
        });

        initOperationWindow();
    }

    private void initOperationWindow()
    {
        View operationView = View.inflate(this, R.layout.window_operation, null);

        Button modifyButton = (Button) operationView.findViewById(R.id.modifyButton);
        modifyButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                operationPopupWindow.dismiss();

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(SubCategoryActivity.this, R.string.error_modify_network_unavailable);
                }
                else
                {
                    Intent intent = new Intent(SubCategoryActivity.this, EditCategoryActivity.class);
                    intent.putExtra("category", currentCategory);
                    ViewUtils.goForward(SubCategoryActivity.this, intent);
                }
            }
        });

        Button deleteButton = (Button) operationView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                operationPopupWindow.dismiss();

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(SubCategoryActivity.this, R.string.error_delete_network_unavailable);
                }
                else
                {
                    Builder builder = new Builder(SubCategoryActivity.this);
                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.prompt_delete_category);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendDeleteCategoryRequest(currentCategory);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            }
        });

        Button cancelButton = (Button) operationView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                operationPopupWindow.dismiss();
            }
        });

        operationPopupWindow = ViewUtils.buildBottomPopupWindow(this, operationView);
    }

    private void refreshListView()
    {
        categoryList = dbManager.getSubCategories(parentID, appPreference.getCurrentGroupID());
        adapter = new CategoryListViewAdapter(this, categoryList, null);
        categoryListView.setAdapter(adapter);

        if (categoryList.isEmpty())
        {
            categoryListView.setVisibility(View.INVISIBLE);
            categoryTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            categoryListView.setVisibility(View.VISIBLE);
            categoryTextView.setVisibility(View.INVISIBLE);
        }

        if (PhoneUtils.isNetworkConnected())
        {
            for (Category category : categoryList)
            {
                if (category.hasUndownloadedIcon())
                {
                    sendDownloadIconRequest(category);
                }
            }
        }
    }

    private void showOperationWindow()
    {
        operationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        operationPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void sendDeleteCategoryRequest(final Category category)
    {
        ReimProgressDialog.show();
        DeleteCategoryRequest request = new DeleteCategoryRequest(category.getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DeleteCategoryResponse response = new DeleteCategoryResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.deleteCategory(category.getServerID());
                    dbManager.deleteSubCategories(category.getServerID(), appPreference.getCurrentGroupID());
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            refreshListView();
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(SubCategoryActivity.this, R.string.succeed_in_deleting_category);
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
                            ViewUtils.showToast(SubCategoryActivity.this, R.string.failed_to_delete_category, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendDownloadIconRequest(final Category category)
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

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
                            adapter.setCategory(categoryList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}