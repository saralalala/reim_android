package com.rushucloud.reim.item;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.ModifyHistoryListViewAdapter;
import classes.model.Image;
import classes.model.ModifyHistory;
import classes.model.User;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class ModifyHistoryActivity extends Activity
{
    // Widgets
    private ModifyHistoryListViewAdapter adapter;

    // Local Data
    private DBManager dbManager;
    private List<ModifyHistory> historyList = new ArrayList<>();

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reim_modify_history);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ModifyHistoryActivity");
        MobclickAgent.onResume(this);
        downloadAvatars();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ModifyHistoryActivity");
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
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        adapter = new ModifyHistoryListViewAdapter(this, historyList);

        ListView historyListView = (ListView) findViewById(R.id.historyListView);
        historyListView.setAdapter(adapter);
    }

    private void downloadAvatars()
    {
        if (PhoneUtils.isNetworkConnected())
        {
            for (ModifyHistory history : historyList)
            {
                if (history.getUser().hasUndownloadedAvatar())
                {
                    sendDownloadAvatarRequest(history.getUser());
                }
            }
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    @SuppressWarnings("unchecked")
    private void initData()
    {
        dbManager = DBManager.getDBManager();
        historyList.addAll((List<ModifyHistory>) getIntent().getSerializableExtra("historyList"));
    }

    // Network
    private void sendDownloadAvatarRequest(final User user)
    {
        DownloadImageRequest request = new DownloadImageRequest(user.getAvatarServerPath());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), Image.TYPE_AVATAR);
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}