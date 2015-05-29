package com.rushucloud.reim;

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

import com.rushucloud.reim.me.AboutActivity;
import com.rushucloud.reim.me.CategoryActivity;
import com.rushucloud.reim.me.FeedbackActivity;
import com.rushucloud.reim.me.InputInviteActivity;
import com.rushucloud.reim.me.InviteActivity;
import com.rushucloud.reim.me.InvoiceTitleActivity;
import com.rushucloud.reim.me.ManagerActivity;
import com.rushucloud.reim.me.MessageListActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.TagActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.CircleImageView;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.URLDef;
import netUtils.request.DownloadImageRequest;
import netUtils.response.DownloadImageResponse;

public class MeFragment extends Fragment
{
    private boolean hasInit = false;

    private AppPreference appPreference;
    private DBManager dbManager;

    private View view;
    private TextView nicknameTextView;
    private TextView companyTextView;
    private CircleImageView avatarImageView;
    private ImageView tipImageView;
    private TextView managerTextView;
    private RelativeLayout categoryLayout;
    private RelativeLayout tagLayout;
    private PopupWindow sharePopupWindow;

    private User currentUser;
    private String avatarPath;

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

    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
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

        RelativeLayout defaultManagerLayout = (RelativeLayout) view.findViewById(R.id.defaultManagerLayout);
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

        // init about
        TextView aboutTextView = (TextView) view.findViewById(R.id.aboutTextView);
        aboutTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_ABOUT");
                ViewUtils.goForward(getActivity(), AboutActivity.class);
            }
        });

        // init feedback
        TextView feedbackTextView = (TextView) view.findViewById(R.id.feedbackTextView);
        feedbackTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(getActivity(), "UMENG_MINE_SETTING_OPINION");
                ViewUtils.goForward(getActivity(), FeedbackActivity.class);
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
                WeChatUtils.shareToWX(URLDef.URL_DOWNLOAD_PAGE, title, description, false);
            }
        });

        LinearLayout momentsLayout = (LinearLayout) shareView.findViewById(R.id.momentsLayout);
        momentsLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sharePopupWindow.dismiss();
                WeChatUtils.shareToWX(URLDef.URL_DOWNLOAD_PAGE, title, description, true);
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

    public void loadProfileView()
    {
        currentUser = appPreference.getCurrentUser();
        Group currentGroup = appPreference.getCurrentGroup();

        if (currentUser != null)
        {
            ViewUtils.setImageViewBitmap(currentUser, avatarImageView);
            nicknameTextView.setText(currentUser.getNickname());

            if (currentUser.hasUndownloadedAvatar() && PhoneUtils.isNetworkConnected())
            {
                sendDownloadAvatarRequest();
            }

            User manager = currentUser.getDefaultManager();
            if (manager != null)
            {
                managerTextView.setText(manager.getNickname());
            }
            else
            {
                managerTextView.setText("");
            }

            if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
            {
                categoryLayout.setVisibility(View.GONE);
                tagLayout.setVisibility(View.GONE);
            }
            else
            {
                categoryLayout.setVisibility(View.VISIBLE);
                tagLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            avatarImageView.setImageResource(R.drawable.default_avatar);
            nicknameTextView.setText(R.string.not_available);
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

        showTip();
    }

    private void showShareWindow()
    {
        sharePopupWindow.showAtLocation(getActivity().findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        sharePopupWindow.update();

        ViewUtils.dimBackground(getActivity());
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
                    avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
                    currentUser.setAvatarLocalPath(avatarPath);
                    currentUser.setLocalUpdatedDate(currentTime);
                    currentUser.setServerUpdatedDate(currentTime);
                    if (dbManager.updateUser(currentUser))
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                loadProfileView();
                            }
                        });
                    }
                    else
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
                else if (getUserVisibleHint())
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