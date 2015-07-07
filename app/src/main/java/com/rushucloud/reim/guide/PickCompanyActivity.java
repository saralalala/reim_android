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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.PickAdminActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.CompanyListViewAdapter;
import classes.model.Group;
import classes.model.Invite;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.group.GetInvitedGroupRequest;
import netUtils.request.group.SearchGroupRequest;
import netUtils.request.user.ApplyRequest;
import netUtils.request.user.InviteReplyRequest;
import netUtils.request.user.SetAdminRequest;
import netUtils.response.group.GetInvitedGroupResponse;
import netUtils.response.group.SearchGroupResponse;
import netUtils.response.user.ApplyResponse;
import netUtils.response.user.InviteReplyResponse;
import netUtils.response.user.SetAdminResponse;

public class PickCompanyActivity extends Activity
{
    // Widgets
    private TextView completeTextView;
    private EditText companyEditText;
    private TextView sectionTextView;
    private CompanyListViewAdapter adapter;

    // Local Data
    private List<Group> companyList = new ArrayList<>();
    private List<Group> invitedList = new ArrayList<>();
    private List<Invite> inviteList = new ArrayList<>();
    private Group company;
    private Invite invite;
    private boolean hasInit = false;
    private boolean fromGuide = false;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_pick_company);
        initView();
        fromGuide = getIntent().getBooleanExtra("fromGuide", false);
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PickCompanyActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (!hasInit)
        {
            hasInit = true;
            if (PhoneUtils.isNetworkConnected())
            {
                sendGetInvitedGroupRequest();
            }
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PickCompanyActivity");
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

    @SuppressWarnings("unchecked")
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case Constant.ACTIVITY_PICK_ADMIN:
                {
                    ReimProgressDialog.setContext(this);
                    List<User> users = (List<User>) data.getSerializableExtra("users");
                    sendSetAdminRequest(users);
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        completeTextView = (TextView) findViewById(R.id.completeTextView);
        completeTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (company != null)
                {
                    hideSoftKeyboard();
                    int index = invitedList.indexOf(company);
                    if (!PhoneUtils.isNetworkConnected())
                    {
                        ViewUtils.showToast(PickCompanyActivity.this, R.string.error_get_data_network_unavailable);
                    }
                    else if (index != -1)
                    {
                        invite = inviteList.get(index);
                        sendInviteReplyRequest(invite);
                    }
                    else
                    {
                        invite = null;
                        ReimProgressDialog.show();
                        sendApplyRequest();
                    }
                }
            }
        });

        ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
        searchImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                searchGroups();
            }
        });

        companyEditText = (EditText) findViewById(R.id.companyEditText);
        companyEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        companyEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    searchGroups();
                }
                return false;
            }
        });
        ViewUtils.requestFocus(this, companyEditText);

        sectionTextView = (TextView) findViewById(R.id.sectionTextView);

        adapter = new CompanyListViewAdapter(this, companyList, company);
        ListView companyListView = (ListView) findViewById(R.id.companyListView);
        companyListView.setAdapter(adapter);
        companyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!companyList.isEmpty())
                {
                    Group chosenCompany = companyList.get(position);
                    company = chosenCompany.equals(company) ? null : chosenCompany;
                    adapter.setCompany(company);
                    adapter.notifyDataSetChanged();
                    if (company == null)
                    {
                        completeTextView.setTextColor(ViewUtils.getColor(R.color.font_title_pressed));
                    }
                    else
                    {
                        completeTextView.setTextColor(ViewUtils.getColorStateList(R.color.title_text_color));
                    }
                }
            }
        });
    }

    private void showLastAdminDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.prompt_last_admin);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(PickCompanyActivity.this, PickAdminActivity.class);
                ViewUtils.goForwardForResult(PickCompanyActivity.this, intent, Constant.ACTIVITY_PICK_ADMIN);
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

    private void goBack()
    {
        if (fromGuide)
        {
            Intent intent = new Intent(this, SetNicknameActivity.class);
            intent.putExtra("nickname", AppPreference.getAppPreference().getCurrentUser().getNickname());
            intent.putExtra("join", true);
            ViewUtils.goBackWithIntent(this, intent);
        }
        else
        {
            ViewUtils.goBack(this);
        }
    }

    // Network
    private void searchGroups()
    {
        if (companyEditText.getText().toString().isEmpty())
        {
            ViewUtils.showToast(PickCompanyActivity.this, R.string.error_search_company_name_empty);
        }
        else if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(PickCompanyActivity.this, R.string.error_search_network_unavailable);
        }
        else
        {
            sendSearchGroupRequest();
        }
    }

    private void sendGetInvitedGroupRequest()
    {
        ReimProgressDialog.show();
        GetInvitedGroupRequest request = new GetInvitedGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetInvitedGroupResponse response = new GetInvitedGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    inviteList.clear();
                    inviteList.addAll(response.getInviteList());
                    invitedList.clear();
                    invitedList.addAll(response.getGroupList());
                    companyList.clear();
                    companyList.addAll(response.getGroupList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (!companyList.isEmpty())
                            {
                                sectionTextView.setText(R.string.invited_company);
                                adapter.setCompanyList(companyList);
                                adapter.notifyDataSetChanged();
                            }
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
                        }
                    });
                }
            }
        });
    }

    private void sendSearchGroupRequest()
    {
        ReimProgressDialog.show();
        SearchGroupRequest request = new SearchGroupRequest(companyEditText.getText().toString());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SearchGroupResponse response = new SearchGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    company = null;
                    companyList.clear();
                    companyList.addAll(response.getGroupList());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            sectionTextView.setText(R.string.search_result);
                            adapter.setCompany(company);
                            adapter.setCompanyList(companyList);
                            adapter.setHasInit(true);
                            adapter.notifyDataSetChanged();
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
                            ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_search_companies, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendInviteReplyRequest(Invite invite)
    {
        InviteReplyRequest request = new InviteReplyRequest(Invite.TYPE_ACCEPTED, invite.getInviteCode(), 1);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final InviteReplyResponse response = new InviteReplyResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    final DBManager dbManager = DBManager.getDBManager();
                    final AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
                    appPreference.setProxyUserID(-1);
                    appPreference.setSyncOnlyWithWifi(true);
                    appPreference.setEnablePasswordProtection(true);

                    if (response.getGroup() != null)
                    {
                        currentGroupID = response.getGroup().getServerID();

                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update members
                        User currentUser = response.getCurrentUser();
                        User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                        if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                        {
                            currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                        }

                        dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                        dbManager.updateUser(currentUser);

                        // update set of books
                        dbManager.updateUserSetOfBooks(response.getSetOfBookList(), appPreference.getCurrentUserID());

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                        // update tags
                        dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                        // update group info
                        dbManager.syncGroup(response.getGroup());
                    }
                    else
                    {
                        // update AppPreference
                        appPreference.setCurrentGroupID(currentGroupID);
                        appPreference.saveAppPreference();

                        // update current user
                        dbManager.syncUser(response.getCurrentUser());

                        // update set of books
                        dbManager.updateUserSetOfBooks(response.getSetOfBookList(), appPreference.getCurrentUserID());

                        // update categories
                        dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (fromGuide)
                            {
                                Intent intent = new Intent(PickCompanyActivity.this, JoinedActivity.class);
                                intent.putExtra("companyName", company.getName());
                                ViewUtils.goForwardAndFinish(PickCompanyActivity.this, intent);
                            }
                            else
                            {
                                ViewUtils.goBack(PickCompanyActivity.this);
                            }
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
                            if (response.getCode() == NetworkConstant.ERROR_LAST_ADMIN)
                            {
                                showLastAdminDialog();
                            }
                            else if (response.getCode() == NetworkConstant.ERROR_SAME_COMPANY && fromGuide)
                            {
                                Intent intent = new Intent(PickCompanyActivity.this, JoinedActivity.class);
                                intent.putExtra("companyName", company.getName());
                                ViewUtils.goForwardAndFinish(PickCompanyActivity.this, intent);
                            }
                            else
                            {
                                ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_apply, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendApplyRequest()
    {
        ApplyRequest request = new ApplyRequest(company.getServerID(), 1);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApplyResponse response = new ApplyResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (fromGuide)
                            {
                                Intent intent = new Intent(PickCompanyActivity.this, JoinCompleteActivity.class);
                                intent.putExtra("companyName", company.getName());
                                ViewUtils.goForwardAndFinish(PickCompanyActivity.this, intent);
                            }
                            else
                            {
                                User currentUser = AppPreference.getAppPreference().getCurrentUser();
                                currentUser.setAppliedCompany(company.getName());
                                DBManager.getDBManager().updateUser(currentUser);
                                ViewUtils.showToast(PickCompanyActivity.this, R.string.succeed_in_applying);
                                ViewUtils.goBack(PickCompanyActivity.this);
                            }
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
                            if (response.getCode() == NetworkConstant.ERROR_LAST_ADMIN)
                            {
                                showLastAdminDialog();
                            }
                            else if (response.getCode() == NetworkConstant.ERROR_SAME_COMPANY && fromGuide)
                            {
                                Intent intent = new Intent(PickCompanyActivity.this, JoinCompleteActivity.class);
                                intent.putExtra("companyName", company.getName());
                                ViewUtils.goForwardAndFinish(PickCompanyActivity.this, intent);
                            }
                            else
                            {
                                ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_apply, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendSetAdminRequest(List<User> userList)
    {
        ReimProgressDialog.show();
        SetAdminRequest request = new SetAdminRequest(userList);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SetAdminResponse response = new SetAdminResponse(httpResponse);
                if (response.getStatus())
                {
                    if (invite != null)
                    {
                        sendInviteReplyRequest(invite);
                    }
                    else
                    {
                        sendApplyRequest();
                    }
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(PickCompanyActivity.this, R.string.failed_to_set_admin, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}