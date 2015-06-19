package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.model.Apply;
import classes.model.Invite;
import classes.model.Message;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.user.ApplyReplyRequest;
import netUtils.request.user.GetMessageRequest;
import netUtils.request.user.InviteReplyRequest;
import netUtils.request.user.SetAdminRequest;
import netUtils.response.user.ApplyReplyResponse;
import netUtils.response.user.GetMessageResponse;
import netUtils.response.user.InviteReplyResponse;
import netUtils.response.user.SetAdminResponse;

public class MessageActivity extends Activity
{
    // Widgets
    private TextView contentTextView;
    private TextView dateTextView;

    // Local Data
    private Message message;
    private Invite invite;
    private Apply apply;
    private boolean fromPush;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_message);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("MessageActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (message.getType() == Message.TYPE_MESSAGE && PhoneUtils.isNetworkConnected())
        {
            sendGetMessageRequest();
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("MessageActivity");
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

        contentTextView = (TextView) findViewById(R.id.contentTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);

        Button agreeButton = (Button) findViewById(R.id.agreeButton);
        agreeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(MessageActivity.this, R.string.error_send_reply_network_unavailable);
                }
                else if (invite != null)
                {
                    ReimProgressDialog.show();
                    sendInviteReplyRequest(Invite.TYPE_ACCEPTED, invite.getInviteCode());
                }
                else if (apply != null)
                {
                    sendApplyReplyRequest(apply.getServerID(), Apply.TYPE_ACCEPTED);
                }
            }
        });

        Button rejectButton = (Button) findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(MessageActivity.this, R.string.error_send_reply_network_unavailable);
                }
                else if (invite != null)
                {
                    ReimProgressDialog.show();
                    sendInviteReplyRequest(Invite.TYPE_REJECTED, invite.getInviteCode());
                }
                else if (apply != null)
                {
                    sendApplyReplyRequest(apply.getServerID(), Apply.TYPE_REJECTED);
                }
            }
        });

        String currentNickname = AppPreference.getAppPreference().getCurrentUser().getNickname();
        if (invite != null && invite.getTypeCode() == Invite.TYPE_NEW && !invite.getInvitor().equals(currentNickname))
        {
            agreeButton.setVisibility(View.VISIBLE);
            rejectButton.setVisibility(View.VISIBLE);
        }
        else if (apply != null && apply.getTypeCode() == Apply.TYPE_NEW && !apply.getApplicant().equals(currentNickname))
        {
            agreeButton.setVisibility(View.VISIBLE);
            agreeButton.setText(R.string.approve_apply);
            rejectButton.setVisibility(View.VISIBLE);
            rejectButton.setText(R.string.reject_apply);
        }

        refreshView();
    }

    private void refreshView()
    {
        contentTextView.setText(message.getContent());
        dateTextView.setText(Utils.secondToStringUpToDay(message.getUpdateTime()));
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
                Intent intent = new Intent(MessageActivity.this, PickAdminActivity.class);
                ViewUtils.goForwardForResult(MessageActivity.this, intent, Constant.ACTIVITY_PICK_ADMIN);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void goBack()
    {
        if (fromPush)
        {
            ReimApplication.setTabIndex(Constant.TAB_ME);
            Intent intent = new Intent(MessageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Intent intent2 = new Intent(MessageActivity.this, MessageListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivities(new Intent[]{intent, intent2});
            overridePendingTransition(R.anim.window_left_in, R.anim.window_right_out);
            finish();
        }
        else
        {
            ViewUtils.goBack(this);
        }
    }

    // Data
    private void initData()
    {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            message = (Message) bundle.getSerializable("message");
            fromPush = bundle.getBoolean("fromPush", false);
            if (message.getType() == Message.TYPE_INVITE)
            {
                invite = (Invite) message;
            }
            else if (message.getType() == Message.TYPE_APPLY)
            {
                apply = (Apply) message;
            }
        }
    }

    // Network
    private void sendGetMessageRequest()
    {
        ReimProgressDialog.show();
        final GetMessageRequest request = new GetMessageRequest(Message.TYPE_MESSAGE, message.getServerID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetMessageResponse response = new GetMessageResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (invite == null)
                            {
                                message = response.getMessage();
                                refreshView();
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
                            ViewUtils.showToast(MessageActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            goBack();
                        }
                    });
                }
            }
        });
    }

    private void sendInviteReplyRequest(final int agree, String inviteCode)
    {
        InviteReplyRequest request = new InviteReplyRequest(agree, inviteCode);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final InviteReplyResponse response = new InviteReplyResponse(httpResponse);
                if (response.getStatus())
                {
                    if (agree == Invite.TYPE_ACCEPTED)
                    {
                        int currentGroupID = -1;

                        DBManager dbManager = DBManager.getDBManager();
                        AppPreference appPreference = AppPreference.getAppPreference();
                        appPreference.setServerToken(response.getServerToken());
                        appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
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

                            // update categories
                            dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                        }

                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(MessageActivity.this, R.string.succeed_in_sending_invite_reply);
                                goBack();
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
                                ViewUtils.showToast(MessageActivity.this, R.string.succeed_in_sending_invite_reply);
                                goBack();
                            }
                        });
                    }
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
                            else
                            {
                                ViewUtils.showToast(MessageActivity.this, R.string.failed_to_send_invite_reply, response.getErrorMessage());
                                if (response.getCode() == NetworkConstant.ERROR_MESSAGE_DONE)
                                {
                                    goBack();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendApplyReplyRequest(int applyID, final int agree)
    {
        ReimProgressDialog.show();
        ApplyReplyRequest request = new ApplyReplyRequest(applyID, agree);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ApplyReplyResponse response = new ApplyReplyResponse(httpResponse);
                if (response.getStatus())
                {
                    if (agree == Invite.TYPE_ACCEPTED)
                    {
                        int currentGroupID = -1;

                        DBManager dbManager = DBManager.getDBManager();
                        AppPreference appPreference = AppPreference.getAppPreference();
                        appPreference.setServerToken(response.getServerToken());
                        appPreference.setCurrentUserID(response.getCurrentUser().getServerID());
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

                            // update categories
                            dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
                        }

                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(MessageActivity.this, R.string.succeed_in_sending_apply_reply);
                                goBack();
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
                                ViewUtils.showToast(MessageActivity.this, R.string.succeed_in_sending_apply_reply);
                                goBack();
                            }
                        });
                    }
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(MessageActivity.this, R.string.failed_to_send_apply_reply, response.getErrorMessage());
                            if (response.getCode() == NetworkConstant.ERROR_MESSAGE_DONE)
                            {
                                goBack();
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
                    sendInviteReplyRequest(Invite.TYPE_ACCEPTED, invite.getInviteCode());
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(MessageActivity.this, R.string.failed_to_set_admin, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}