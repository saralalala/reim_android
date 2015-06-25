package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.common.SingleImageActivity;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.common.CommonRequest;
import netUtils.request.common.UploadImageRequest;
import netUtils.request.user.SignOutRequest;
import netUtils.response.common.CommonResponse;
import netUtils.response.common.UploadImageResponse;
import netUtils.response.user.SignOutResponse;

public class ProfileActivity extends Activity
{
    // Widgets
    private CircleImageView avatarImageView;
    private PopupWindow picturePopupWindow;

    private TextView nicknameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView wechatTextView;
    private TextView companyTextView;
    private TextView switchTextView;
    private RelativeLayout passwordLayout;
    private TextView passwordTextView;

    // Local Data
    private AppPreference appPreference;

    private Group currentGroup;
    private User currentUser;
    private String avatarPath;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_profile);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ProfileActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        loadInfoView();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ProfileActivity");
        MobclickAgent.onPause(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            try
            {
                switch (requestCode)
                {
                    case Constant.ACTIVITY_PICK_IMAGE:
                    {
                        cropImage(data.getData());
                        break;
                    }
                    case Constant.ACTIVITY_TAKE_PHOTO:
                    {
                        cropImage(appPreference.getTempAvatarUri());
                        break;
                    }
                    case Constant.ACTIVITY_CROP_IMAGE:
                    {
                        avatarPath = PhoneUtils.saveBitmapToFile(appPreference.getTempAvatarPath(), NetworkConstant.IMAGE_TYPE_AVATAR);

                        if (!avatarPath.isEmpty() && PhoneUtils.isNetworkConnected())
                        {
                            ViewUtils.showToast(this, R.string.succeed_in_saving_avatar);
                            avatarImageView.setImageBitmap(BitmapFactory.decodeFile(avatarPath));
                            sendUploadAvatarRequest();
                        }
                        else if (avatarPath.isEmpty())
                        {
                            ViewUtils.showToast(this, R.string.failed_to_save_avatar);
                        }
                        else
                        {
                            ViewUtils.showToast(this, R.string.error_upload_avatar_network_unavailable);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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

        TextView signOutTextView = (TextView) findViewById(R.id.signOutTextView);
        signOutTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String message = getString(R.string.prompt_sign_out) + currentUser.getNickname();
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle(R.string.tip);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (PhoneUtils.isNetworkConnected())
                        {
                            sendSignOutRequest();
                        }
                        else
                        {
                            ViewUtils.showToast(ProfileActivity.this, R.string.error_sign_out_network_unavailable);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        });

        initAvatarView();

        // init nickname
        nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);

        LinearLayout nicknameLayout = (LinearLayout) findViewById(R.id.nicknameLayout);
        nicknameLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(ProfileActivity.this, NicknameActivity.class);
            }
        });

        // init email
        emailTextView = (TextView) findViewById(R.id.emailTextView);

        LinearLayout emailLayout = (LinearLayout) findViewById(R.id.emailLayout);
        emailLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String email = appPreference.getCurrentUser().getEmail();
                if (email.isEmpty())
                {
                    ViewUtils.goForward(ProfileActivity.this, BindEmailActivity.class);
                }
                else
                {
                    Intent intent = new Intent(ProfileActivity.this, EmailActivity.class);
                    intent.putExtra("email", email);
                    ViewUtils.goForward(ProfileActivity.this, intent);
                }
            }
        });

        // init phone
        phoneTextView = (TextView) findViewById(R.id.phoneTextView);

        LinearLayout phoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        phoneLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String phone = appPreference.getCurrentUser().getPhone();
                if (phone.isEmpty())
                {
                    ViewUtils.goForward(ProfileActivity.this, BindPhoneActivity.class);
                }
                else
                {
                    Intent intent = new Intent(ProfileActivity.this, PhoneActivity.class);
                    intent.putExtra("phone", phone);
                    ViewUtils.goForward(ProfileActivity.this, intent);
                }
            }
        });

        // init wechat
        wechatTextView = (TextView) findViewById(R.id.wechatTextView);

        LinearLayout wechatLayout = (LinearLayout) findViewById(R.id.wechatLayout);
        wechatLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                String wechat = appPreference.getCurrentUser().getWeChat();
                if (wechat.isEmpty())
                {
                    ViewUtils.goForward(ProfileActivity.this, BindWeChatActivity.class);
                }
                else
                {
                    Intent intent = new Intent(ProfileActivity.this, WeChatActivity.class);
                    intent.putExtra("wechat", wechat);
                    ViewUtils.goForward(ProfileActivity.this, intent);
                }
            }
        });

        // init bank
        LinearLayout bankLayout = (LinearLayout) findViewById(R.id.bankLayout);
        bankLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(ProfileActivity.this, BankActivity.class);
            }
        });

        // init proxy
        switchTextView = (TextView) findViewById(R.id.switchTextView);

        LinearLayout proxyLayout = (LinearLayout) findViewById(R.id.proxyLayout);
        proxyLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(ProfileActivity.this, ProxyActivity.class);
            }
        });

        // init company
        companyTextView = (TextView) findViewById(R.id.companyTextView);

        LinearLayout companyLayout = (LinearLayout) findViewById(R.id.companyLayout);
        companyLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(ProfileActivity.this, CompanyActivity.class);
            }
        });

        // init switch
        LinearLayout switchLayout = (LinearLayout) findViewById(R.id.switchLayout);
        switchLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (appPreference.isProxyMode())
                {
                    int permission = appPreference.getProxyPermission();
                    appPreference.setCurrentUserID(appPreference.getProxyUserID());
                    appPreference.setProxyUserID(-1);
                    appPreference.setProxyPermission(-1);
                    sendCommonRequest(permission);
                }
                else
                {
                    ViewUtils.goForward(ProfileActivity.this, ClientActivity.class);
                }
            }
        });

        // init password
        passwordLayout = (RelativeLayout) findViewById(R.id.passwordLayout);
        passwordLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (appPreference.hasPassword())
                {
                    ViewUtils.goForward(ProfileActivity.this, ChangePasswordActivity.class);
                }
                else
                {
                    ViewUtils.goForward(ProfileActivity.this, SetPasswordActivity.class);
                }
            }
        });

        passwordTextView = (TextView) findViewById(R.id.passwordTextView);
    }

    private void initAvatarView()
    {
        // init avatar
        RelativeLayout avatarLayout = (RelativeLayout) findViewById(R.id.avatarLayout);
        avatarLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                showPictureWindow();
            }
        });

        avatarImageView = (CircleImageView) findViewById(R.id.avatarImageView);
        avatarImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (currentUser != null && !currentUser.getAvatarLocalPath().isEmpty())
                {
                    Intent intent = new Intent(ProfileActivity.this, SingleImageActivity.class);
                    intent.putExtra("imagePath", currentUser.getAvatarLocalPath());
                    ViewUtils.goForward(ProfileActivity.this, intent);
                }
                else if (currentUser != null)
                {
                    showPictureWindow();
                }
            }
        });

        // init avatar window
        View pictureView = View.inflate(this, R.layout.window_picture, null);

        Button cameraButton = (Button) pictureView.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                picturePopupWindow.dismiss();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempAvatarUri());
                startActivityForResult(intent, Constant.ACTIVITY_TAKE_PHOTO);
            }
        });

        Button galleryButton = (Button) pictureView.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                picturePopupWindow.dismiss();

                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setType("image/*");
                startActivityForResult(intent, Constant.ACTIVITY_PICK_IMAGE);
            }
        });

        Button cancelButton = (Button) pictureView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                picturePopupWindow.dismiss();
            }
        });

        picturePopupWindow = ViewUtils.buildBottomPopupWindow(this, pictureView);
    }

    private void loadInfoView()
    {
        appPreference = AppPreference.getAppPreference();
        currentUser = appPreference.getCurrentUser();
        currentGroup = appPreference.getCurrentGroup();

        if (currentUser == null)
        {
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (Utils.isEmailOrPhone(appPreference.getUsername()))
            {
                intent.putExtra("username", appPreference.getUsername());
                intent.putExtra("password", appPreference.getPassword());
            }
            ViewUtils.goBackWithIntent(ProfileActivity.this, intent);
        }
        else
        {
            ViewUtils.setImageViewBitmap(currentUser, avatarImageView);

            String nickname = !currentUser.getNickname().isEmpty() ? currentUser.getNickname() : getString(R.string.empty);
            nicknameTextView.setText(nickname);

            String email = !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : getString(R.string.not_binding);
            if (!currentUser.getEmail().isEmpty() && !currentUser.isActive())
            {
                email += getString(R.string.not_active);
            }
            emailTextView.setText(email);

            String phone = !currentUser.getPhone().isEmpty() ? currentUser.getPhone() : getString(R.string.not_binding);
            phoneTextView.setText(phone);

            String wechat = !currentUser.getWeChat().isEmpty() ? currentUser.getWeChat() : getString(R.string.not_binding);
            wechatTextView.setText(wechat);

            if (currentGroup != null)
            {
                companyTextView.setText(currentGroup.getName());
            }
            else if (!currentUser.getAppliedCompany().isEmpty())
            {
                companyTextView.setText(currentUser.getAppliedCompany() + ViewUtils.getString(R.string.waiting_for_approve));
            }
            else
            {
                companyTextView.setText(R.string.not_joined);
            }

            int switchPrompt = appPreference.isProxyMode()? R.string.switch_back : R.string.switch_identity;
            switchTextView.setText(switchPrompt);

            if (currentUser.getEmail().isEmpty() && currentUser.getPhone().isEmpty())
            {
                passwordLayout.setVisibility(View.GONE);
            }
            else
            {
                passwordLayout.setVisibility(View.VISIBLE);
                int text = appPreference.hasPassword()? R.string.change_password : R.string.set_password;
                passwordTextView.setText(text);
            }
        }
    }

    private void showPictureWindow()
    {
        picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        picturePopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void cropImage(Uri uri)
    {
        int width = ViewUtils.getPhoneWindowWidth(this);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", width);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempAvatarUri());
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, Constant.ACTIVITY_CROP_IMAGE);
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        currentUser = appPreference.getCurrentUser();
        currentGroup = appPreference.getCurrentGroup();
    }

    // Network
    private void sendUploadAvatarRequest()
    {
        UploadImageRequest request = new UploadImageRequest(avatarPath, NetworkConstant.IMAGE_TYPE_AVATAR);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UploadImageResponse response = new UploadImageResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    DBManager dbManager = DBManager.getDBManager();
                    currentUser.setAvatarID(response.getImageID());
                    currentUser.setAvatarLocalPath(avatarPath);
                    currentUser.setLocalUpdatedDate(currentTime);
                    currentUser.setServerUpdatedDate(currentTime);
                    dbManager.updateUser(currentUser);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            loadInfoView();
                            ViewUtils.showToast(ProfileActivity.this, R.string.succeed_in_uploading_avatar);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            loadInfoView();
                            ViewUtils.showToast(ProfileActivity.this, R.string.failed_to_upload_avatar);
                        }
                    });
                }
            }
        });
    }

    private void sendSignOutRequest()
    {
        ReimProgressDialog.show();
        SignOutRequest request = new SignOutRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final SignOutResponse response = new SignOutResponse(httpResponse);
                if (response.getStatus())
                {
                    AppPreference appPreference = AppPreference.getAppPreference();

                    final String username = appPreference.getUsername();
                    final String password = appPreference.getPassword();

                    appPreference.setCurrentUserID(-1);
                    appPreference.setProxyUserID(-1);
                    appPreference.setProxyPermission(-1);
                    appPreference.setCurrentGroupID(-1);
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.setHasPassword(true);
                    appPreference.setServerToken("");
                    appPreference.setLastSyncTime(0);
                    appPreference.setSandboxMode(false);
                    appPreference.saveAppPreference();

                    ReimApplication.setTabIndex(Constant.TAB_REIM);
                    ReimApplication.setReportTabIndex(Constant.TAB_REPORT_MINE);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            if (Utils.isEmailOrPhone(username))
                            {
                                intent.putExtra("username", username);
                                intent.putExtra("password", password);
                            }
                            ViewUtils.goBackWithIntent(ProfileActivity.this, intent);
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
                            ViewUtils.showToast(ProfileActivity.this, R.string.failed_to_sign_out, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendCommonRequest(final int permission)
    {
        ReimProgressDialog.show();
        CommonRequest request = new CommonRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CommonResponse response = new CommonResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = -1;

                    DBManager dbManager = DBManager.getDBManager();
                    appPreference.setServerToken(response.getServerToken());

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
                            ViewUtils.showToast(ProfileActivity.this, R.string.succeed_in_switch_identity);
                            loadInfoView();
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
                            int proxyUserID = appPreference.getProxyUserID();
                            appPreference.setProxyUserID(appPreference.getCurrentUserID());
                            appPreference.setCurrentUserID(proxyUserID);
                            appPreference.setProxyPermission(permission);
                            appPreference.saveAppPreference();
                            ViewUtils.showToast(ProfileActivity.this, R.string.failed_to_switch_identity, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}