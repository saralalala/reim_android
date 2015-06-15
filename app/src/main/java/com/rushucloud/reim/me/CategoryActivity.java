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
import android.widget.AdapterView.OnItemClickListener;
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

public class CategoryActivity extends Activity
{
    private ListView categoryListView;
    private TextView categoryTextView;
    private CategoryListViewAdapter adapter;
    private PopupWindow operationPopupWindow;

    private AppPreference appPreference;
    private DBManager dbManager;

    private List<Category> categoryList;
    private Category currentCategory;

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
        MobclickAgent.onPageStart("CategoryActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshListView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("CategoryActivity");
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

        categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
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

        TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CategoryActivity.this, EditCategoryActivity.class);
                intent.putExtra("category", new Category());
                ViewUtils.goForward(CategoryActivity.this, intent);
            }
        });

        categoryTextView = (TextView) findViewById(R.id.categoryTextView);

        adapter = new CategoryListViewAdapter(this, categoryList, null);
        categoryListView = (ListView) findViewById(R.id.categoryListView);
        categoryListView.setAdapter(adapter);
        categoryListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (operationPopupWindow == null || !operationPopupWindow.isShowing())
                {
                    Intent intent = new Intent(CategoryActivity.this, SubCategoryActivity.class);
                    intent.putExtra("parentID", categoryList.get(position).getServerID());
                    ViewUtils.goForward(CategoryActivity.this, intent);
                }
            }
        });
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

                Intent intent = new Intent(CategoryActivity.this, EditCategoryActivity.class);
                intent.putExtra("category", currentCategory);
                ViewUtils.goForward(CategoryActivity.this, intent);
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
                    ViewUtils.showToast(CategoryActivity.this, R.string.error_delete_network_unavailable);
                }
                else
                {
                    Builder builder = new Builder(CategoryActivity.this);
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

    private void showOperationWindow()
    {
        operationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        operationPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void refreshListView()
    {
        categoryList = dbManager.getGroupCategories(appPreference.getCurrentGroupID());
        adapter.setCategory(categoryList);
        adapter.notifyDataSetChanged();

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
                            ViewUtils.showToast(CategoryActivity.this, R.string.succeed_in_deleting_category);
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
                            ViewUtils.showToast(CategoryActivity.this, R.string.failed_to_delete_category, response.getErrorMessage());
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