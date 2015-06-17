package com.rushucloud.reim.guide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.group.CreateGroupRequest;
import netUtils.request.group.ModifyGroupRequest;
import netUtils.response.group.CreateGroupResponse;
import netUtils.response.group.ModifyGroupResponse;

public class CompanyNameActivity extends Activity
{
    // Widgets
    private ClearEditText companyEditText;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
    private Group currentGroup;
    private String companyName;
    private ArrayList<String> inputList;
    private ArrayList<String> inputChosenList;
    private List<User> contactChosenList;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_create_company);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("CreateCompanyActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("CreateCompanyActivity");
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

        TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                boolean isNetworkConnected = PhoneUtils.isNetworkConnected();
                companyName = companyEditText.getText().toString();
                if (companyName.isEmpty())
                {
                    ViewUtils.showToast(CompanyNameActivity.this, R.string.error_company_name_empty);
                }
                else if (!isNetworkConnected && currentGroup == null)
                {
                    ViewUtils.showToast(CompanyNameActivity.this, R.string.error_create_network_unavailable);
                }
                else if (!isNetworkConnected)
                {
                    ViewUtils.showToast(CompanyNameActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (currentGroup == null)
                {
                    sendCreateGroupRequest(false);
                }
                else
                {
                    sendModifyGroupRequest();
                }
            }
        });

        companyEditText = (ClearEditText) findViewById(R.id.companyEditText);
        companyEditText.setText(companyName);
        ViewUtils.requestFocus(this, companyEditText);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void showCompanyDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(String.format(getString(R.string.prompt_company_exists), companyName));
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                sendCreateGroupRequest(true);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);
    }

    private void goForward()
    {
        Bundle bundle = new Bundle();
        bundle.putString("companyName", companyName);
        bundle.putStringArrayList("inputList", inputList);
        bundle.putStringArrayList("inputChosenList", inputChosenList);
        bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
        Intent intent = new Intent(CompanyNameActivity.this, InviteListActivity.class);
        intent.putExtras(bundle);
        ViewUtils.goForwardAndFinish(CompanyNameActivity.this, intent);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        Bundle bundle = new Bundle();
        bundle.putString("companyName", companyName);
        bundle.putStringArrayList("inputList", inputList);
        bundle.putStringArrayList("inputChosenList", inputChosenList);
        bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
        Intent intent = new Intent(this, SetNicknameActivity.class);
        intent.putExtras(bundle);
        ViewUtils.goBackWithIntent(this, intent);
    }

    // Data
    @SuppressWarnings("unchecked")
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
        currentGroup = appPreference.getCurrentGroup();

        Bundle bundle = getIntent().getExtras();
        companyName = bundle.getString("companyName", "");
        inputList = bundle.getStringArrayList("inputList");
        inputChosenList = bundle.getStringArrayList("inputChosenList");
        contactChosenList = (List<User>) bundle.getSerializable("contactChosenList");
    }

    // Network
    private void sendCreateGroupRequest(boolean forceCreate)
    {
        ReimProgressDialog.show();
        CreateGroupRequest request = new CreateGroupRequest(companyName, forceCreate, 1);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateGroupResponse response = new CreateGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Group group = new Group();
                    group.setName(companyName);
                    group.setServerID(response.getGroupID());
                    group.setLocalUpdatedDate(response.getDate());
                    group.setServerUpdatedDate(response.getDate());

                    User currentUser = appPreference.getCurrentUser();
                    currentUser.setGroupID(group.getServerID());
                    currentUser.setIsAdmin(true);

                    dbManager.insertGroup(group);
                    dbManager.updateUser(currentUser);
                    appPreference.setCurrentGroupID(group.getServerID());
                    appPreference.saveAppPreference();

                    int currentGroupID = response.getGroup().getServerID();

                    // update AppPreference
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setCurrentGroupID(currentGroupID);
                    appPreference.saveAppPreference();

                    // update members
                    DBManager dbManager = DBManager.getDBManager();
                    currentUser = response.getCurrentUser();
                    User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                    if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                    {
                        currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                    }

                    dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                    dbManager.syncUser(currentUser);

                    // update categories
                    dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                    // update tags
                    dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CompanyNameActivity.this, R.string.succeed_in_creating_company);
                            goForward();
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
                            if (response.getCode() == NetworkConstant.ERROR_COMPANY_EXISTS)
                            {
                                showCompanyDialog();
                            }
                            else
                            {
                                ViewUtils.showToast(CompanyNameActivity.this, R.string.failed_to_create_company, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendModifyGroupRequest()
    {
        ModifyGroupRequest request = new ModifyGroupRequest(companyName);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyGroupResponse response = new ModifyGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    currentGroup.setName(companyName);
                    dbManager.updateGroup(currentGroup);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CompanyNameActivity.this, R.string.succeed_in_modifying);
                            goForward();
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
                            ViewUtils.showToast(CompanyNameActivity.this, R.string.failed_to_modify, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}