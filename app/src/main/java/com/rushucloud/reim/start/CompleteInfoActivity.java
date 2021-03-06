package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.common.SingleImageActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.Image;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.common.UploadImageRequest;
import netUtils.request.user.ModifyUserRequest;
import netUtils.response.common.UploadImageResponse;
import netUtils.response.user.ModifyUserResponse;

public class CompleteInfoActivity extends Activity
{
    // Widgets
    private ImageView avatarImageView;
    private ClearEditText nicknameEditText;
    private PopupWindow picturePopupWindow;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;

    private User currentUser;
    private String avatarPath = "";
    private boolean newAvatar = false;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_complete_info);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("CompleteInfoActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("CompleteInfoActivity");
        MobclickAgent.onPause(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            try
            {
                if (requestCode == Constant.ACTIVITY_PICK_IMAGE)
                {
                    cropImage(data.getData());
                }
                else if (requestCode == Constant.ACTIVITY_TAKE_PHOTO)
                {
                    cropImage(appPreference.getTempAvatarUri());
                }
                else if (requestCode == Constant.ACTIVITY_CROP_IMAGE)
                {
                    avatarPath = PhoneUtils.saveBitmapToFile(appPreference.getTempAvatarPath(), Image.TYPE_AVATAR);
                    newAvatar = true;
                    avatarImageView.setImageBitmap(BitmapFactory.decodeFile(avatarPath));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        avatarImageView = (ImageView) findViewById(R.id.avatarImageView);
        avatarImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!avatarPath.isEmpty())
                {
                    Intent intent = new Intent(CompleteInfoActivity.this, SingleImageActivity.class);
                    intent.putExtra("imagePath", avatarPath);
                    startActivity(intent);
                }
            }
        });
        avatarImageView.setOnLongClickListener(new View.OnLongClickListener()
        {
            public boolean onLongClick(View v)
            {
                showPictureWindow();
                return false;
            }
        });

        nicknameEditText = (ClearEditText) findViewById(R.id.nicknameEditText);
        nicknameEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    completeInfo();
                }
                return false;
            }
        });
        ViewUtils.requestFocus(this, nicknameEditText);

        Button completeButton = (Button) findViewById(R.id.completeButton);
        completeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                completeInfo();
            }
        });

        // init picture window
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

        RelativeLayout baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void showPictureWindow()
    {
        picturePopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempAvatarUri());
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, Constant.ACTIVITY_CROP_IMAGE);
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        startActivity(new Intent(CompleteInfoActivity.this, SignInActivity.class));
        finish();
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
        currentUser = AppPreference.getAppPreference().getCurrentUser();
    }

    // Network
    private void completeInfo()
    {
        MobclickAgent.onEvent(CompleteInfoActivity.this, "UMENG_LOGIN");
        hideSoftKeyboard();

        String nickname = nicknameEditText.getText().toString();
        currentUser.setNickname(nickname);

        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(CompleteInfoActivity.this, R.string.error_update_network_unavailable);
            startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
            finish();
        }
        else if (!newAvatar && nickname.isEmpty())
        {
            startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
            finish();
        }
        else if (newAvatar)
        {
            ReimProgressDialog.show();
            sendUploadAvatarRequest();
        }
        else
        {
            ReimProgressDialog.show();
            sendModifyUserInfoRequest();
        }
    }

    private void sendUploadAvatarRequest()
    {
        UploadImageRequest request = new UploadImageRequest(avatarPath, Image.TYPE_AVATAR);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UploadImageResponse response = new UploadImageResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    currentUser.setAvatarID(response.getImageID());
                    currentUser.setAvatarLocalPath(avatarPath);
                    currentUser.setLocalUpdatedDate(currentTime);
                    currentUser.setServerUpdatedDate(currentTime);
                    dbManager.updateUser(currentUser);
                    newAvatar = false;

                    if (currentUser.getNickname().isEmpty())
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(CompleteInfoActivity.this, R.string.succeed_in_modifying_user_info);
                                startActivity(new Intent(CompleteInfoActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        });
                    }
                    else
                    {
                        sendModifyUserInfoRequest();
                    }
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CompleteInfoActivity.this, R.string.failed_to_upload_avatar, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendModifyUserInfoRequest()
    {
        ModifyUserRequest request = new ModifyUserRequest(currentUser);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.updateUser(currentUser);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CompleteInfoActivity.this, R.string.succeed_in_modifying_user_info);
                            startActivity(new Intent(CompleteInfoActivity.this, MainActivity.class));
                            finish();
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
                            ViewUtils.showToast(CompleteInfoActivity.this, R.string.failed_to_upload_avatar, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}