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

import classes.adapter.TagListViewAdapter;
import classes.model.Tag;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.tag.DeleteTagRequest;
import netUtils.response.tag.DeleteTagResponse;

public class TagActivity extends Activity
{
    private ListView tagListView;
    private TextView tagTextView;
    private TagListViewAdapter adapter;
    private PopupWindow operationPopupWindow;

    private DBManager dbManager;

    private List<Tag> tagList;
    private Tag currentTag;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_tag);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("TagActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshListView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("TagActivity");
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
        dbManager = DBManager.getDBManager();
        tagList = dbManager.getGroupTags(AppPreference.getAppPreference().getCurrentGroupID());
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
                Intent intent = new Intent(TagActivity.this, EditTagActivity.class);
                intent.putExtra("tag", new Tag());
                ViewUtils.goForward(TagActivity.this, intent);
            }
        });

        tagTextView = (TextView) findViewById(R.id.tagTextView);

        adapter = new TagListViewAdapter(this, tagList, null);
        tagListView = (ListView) findViewById(R.id.tagListView);
        tagListView.setAdapter(adapter);
        tagListView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentTag = tagList.get(position);
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

                Intent intent = new Intent(TagActivity.this, EditTagActivity.class);
                intent.putExtra("tag", currentTag);
                ViewUtils.goForward(TagActivity.this, intent);
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
                    ViewUtils.showToast(TagActivity.this, R.string.error_delete_network_unavailable);
                }
                else
                {
                    Builder builder = new Builder(TagActivity.this);
                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.prompt_delete_tag);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            sendDeleteTagRequest(currentTag);
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
        tagList = dbManager.getGroupTags(AppPreference.getAppPreference().getCurrentGroupID());

        if (tagList.isEmpty())
        {
            tagListView.setVisibility(View.INVISIBLE);
            tagTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            tagListView.setVisibility(View.VISIBLE);
            tagTextView.setVisibility(View.INVISIBLE);

            adapter.setTagList(tagList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showOperationWindow()
    {
        operationPopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        operationPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void sendDeleteTagRequest(final Tag tag)
    {
        ReimProgressDialog.show();
        DeleteTagRequest request = new DeleteTagRequest(tag.getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final DeleteTagResponse response = new DeleteTagResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.deleteTag(tag.getServerID());
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            refreshListView();
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(TagActivity.this, R.string.succeed_in_deleting_tag);
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
                            ViewUtils.showToast(TagActivity.this, R.string.failed_to_delete_tag, response.getErrorMessage());
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