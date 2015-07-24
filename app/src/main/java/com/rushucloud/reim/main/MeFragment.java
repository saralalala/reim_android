package com.rushucloud.reim.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.CategoryActivity;
import com.rushucloud.reim.me.ImportActivity;
import com.rushucloud.reim.me.InviteActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ManagerActivity;
import com.rushucloud.reim.me.MessageListActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.SettingsActivity;
import com.rushucloud.reim.me.TagActivity;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.model.Group;
import classes.model.Image;
import classes.model.SetOfBook;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.CircleImageView;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.DownloadImageRequest;
import netUtils.response.common.DownloadImageResponse;

public class MeFragment extends Fragment
{
    // Widgets
    private View view;
    private TextView nicknameTextView;
    private TextView companyTextView;
    private CircleImageView avatarImageView;
    private ImageView tipImageView;
    private TextView managerTextView;
    private View adminDivider;
    private RelativeLayout defaultManagerLayout;
    private RelativeLayout categoryLayout;
    private RelativeLayout tagLayout;
    private PopupWindow sharePopupWindow;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;
    private boolean hasInit = false;
    private User currentUser;
    private String avatarPath;

    // View
    // 创建该Fragment的视图
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_me, container, false);
        }
        else
        {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null)
            {
                viewGroup.removeView(view);
            }
        }
        return view;
    }

    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("MeFragment");
        if (!hasInit)
        {
            initData();
            initView();
            hasInit = true;
            loadProfileView();
        }
    }

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("MeFragment");
    }

    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && hasInit)
        {
            loadProfileView();
        }
    }

    private void initView()
    {
        // init profile
        nicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);
        companyTextView = (TextView) view.findViewById(R.id.companyTextView);

        avatarImageView = (CircleImageView) view.findViewById(R.id.avatarImageView);

        RelativeLayout profileLayout = (RelativeLayout) view.findViewById(R.id.profileLayout);
        profileLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CHANGE_USERINFO");
                ViewUtils.goForward(getActivity(), ProfileActivity.class);
            }
        });

        // init import
        RelativeLayout importLayout = (RelativeLayout) view.findViewById(R.id.importLayout);
        importLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(getActivity(), ImportActivity.class);
            }
        });

        // init message
        RelativeLayout messageLayout = (RelativeLayout) view.findViewById(R.id.messageLayout);
        messageLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ReimApplication.setUnreadMessagesCount(0);
                ReimApplication.setHasUnreadMessages(false);
                ViewUtils.goForward(getActivity(), MessageListActivity.class);
            }
        });

        tipImageView = (ImageView) view.findViewById(R.id.tipImageView);

        // init invite
        RelativeLayout inviteLayout = (RelativeLayout) view.findViewById(R.id.inviteLayout);
        inviteLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (appPreference.isSandboxMode())
                {
                    ViewUtils.showToast(getActivity(), R.string.error_trial_invite);
                }
                else
                {
                    ViewUtils.goForward(getActivity(), InviteActivity.class);
                }
            }
        });

        // init manager
        managerTextView = (TextView) view.findViewById(R.id.managerTextView);

        defaultManagerLayout = (RelativeLayout) view.findViewById(R.id.defaultManagerLayout);
        defaultManagerLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (currentUser.getGroupID() <= 0)
                {
                    ViewUtils.showToast(getActivity(), R.string.error_no_group);
                }
                else
                {
                    ViewUtils.goForward(getActivity(), ManagerActivity.class);
                }
            }
        });

        // init invoice
        TextView invoiceTextView = (TextView) view.findViewById(R.id.invoiceTextView);
        invoiceTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(getActivity(), InvoiceTitleActivity.class);
            }
        });

        // init adminDivider
        adminDivider = view.findViewById(R.id.adminDivider);

        // init category
        categoryLayout = (RelativeLayout) view.findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_CATEGORT_SETTING");
                ViewUtils.goForward(getActivity(), CategoryActivity.class);
            }
        });

        // init tag
        tagLayout = (RelativeLayout) view.findViewById(R.id.tagLayout);
        tagLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_TAG_SETTING");
                ViewUtils.goForward(getActivity(), TagActivity.class);
            }
        });

        // init share
        TextView shareTextView = (TextView) view.findViewById(R.id.shareTextView);
        shareTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showShareWindow();
            }
        });

        initShareWindow();

        // init settings
        RelativeLayout settingsLayout = (RelativeLayout) view.findViewById(R.id.settingsLayout);
        settingsLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(getActivity(), SettingsActivity.class);
            }
        });

        // init feedback
//        TextView feedbackTextView = (TextView) view.findViewById(R.id.feedbackTextView);
//        feedbackTextView.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_OPINION");
//                ViewUtils.goForward(getActivity(), FeedbackActivity.class);
//            }
//        });
    }

    private void initShareWindow()
    {
        final String title = ViewUtils.getString(R.string.wechat_share_title);
        final String description = ViewUtils.getString(R.string.wechat_share_description);

        View shareView = View.inflate(getActivity(), R.layout.window_me_share, null);

        LinearLayout sessionLayout = (LinearLayout) shareView.findViewById(R.id.sessionLayout);
        sessionLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sharePopupWindow.dismiss();
                WeChatUtils.shareToWX(getActivity(), URLDef.URL_DOWNLOAD_PAGE, title, description, false);
            }
        });

        LinearLayout momentsLayout = (LinearLayout) shareView.findViewById(R.id.momentsLayout);
        momentsLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sharePopupWindow.dismiss();
                WeChatUtils.shareToWX(getActivity(), URLDef.URL_DOWNLOAD_PAGE, title, description, true);
            }
        });

        TextView cancelTextView = (TextView) shareView.findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sharePopupWindow.dismiss();
            }
        });

        sharePopupWindow = ViewUtils.buildBottomPopupWindow(getActivity(), shareView);
    }

    private void showShareWindow()
    {
        sharePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        sharePopupWindow.update();

        ViewUtils.dimBackground(getActivity());
    }

    public void loadProfileView()
    {
        currentUser = appPreference.getCurrentUser();
        Group currentGroup = appPreference.getCurrentGroup();

        if (currentUser != null)
        {
            ViewUtils.setImageViewBitmap(currentUser, avatarImageView);
            nicknameTextView.setText(currentUser.getNickname());

            if (currentUser.hasUndownloadedAvatar() && PhoneUtils.isNetworkConnected() && isAdded())
            {
                sendDownloadAvatarRequest();
            }

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
                companyTextView.setText(R.string.no_company);
            }

            if (currentGroup != null && !currentGroup.showStructure())
            {
                defaultManagerLayout.setVisibility(View.GONE);
            }
            else
            {
                defaultManagerLayout.setVisibility(View.VISIBLE);

                User manager = currentUser.getDefaultManager();
                if (manager != null)
                {
                    managerTextView.setText(manager.getNickname());
                }
                else
                {
                    managerTextView.setText("");
                }
            }

            List<SetOfBook> setOfBookList = dbManager.getUserSetOfBooks(appPreference.getCurrentUserID());
            if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
            {
                adminDivider.setVisibility(View.GONE);
                categoryLayout.setVisibility(View.GONE);
                tagLayout.setVisibility(View.GONE);
            }
            else if (setOfBookList.isEmpty())
            {
                adminDivider.setVisibility(View.VISIBLE);
                categoryLayout.setVisibility(View.VISIBLE);
                tagLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                adminDivider.setVisibility(View.VISIBLE);
                categoryLayout.setVisibility(View.GONE);
                tagLayout.setVisibility(View.VISIBLE);
            }

            showTip();
        }
        else if (isAdded())
        {
            ReimApplication.resetTabIndices();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            if (Utils.isEmailOrPhone(appPreference.getUsername()))
            {
                intent.putExtra("username", appPreference.getUsername());
                intent.putExtra("password", appPreference.getPassword());
            }
            ViewUtils.goBackWithIntent(getActivity(), intent);
        }
    }

    public void showTip()
    {
        if (view == null)
        {
            return;
        }

        if (tipImageView == null)
        {
            tipImageView = (ImageView) view.findViewById(R.id.tipImageView);
        }

        int visibility = ReimApplication.hasUnreadMessages() ? View.VISIBLE : View.GONE;
        tipImageView.setVisibility(visibility);
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
    }

    // Network
    private void sendDownloadAvatarRequest()
    {
        DownloadImageRequest request = new DownloadImageRequest(currentUser.getAvatarServerPath());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    int currentTime = Utils.getCurrentTime();
                    avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), Image.TYPE_AVATAR, currentUser.getAvatarID());
                    currentUser.setAvatarLocalPath(avatarPath);
                    currentUser.setLocalUpdatedDate(currentTime);
                    currentUser.setServerUpdatedDate(currentTime);
                    if (dbManager.updateUser(currentUser) && isAdded())
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                loadProfileView();
                            }
                        });
                    }
                    else if (isAdded())
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ViewUtils.showToast(getActivity(), R.string.failed_to_save_avatar);
                            }
                        });
                    }
                }
                else if (isAdded())
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ViewUtils.showToast(getActivity(), R.string.failed_to_download_avatar);
                        }
                    });
                }
            }
        });
    }
}