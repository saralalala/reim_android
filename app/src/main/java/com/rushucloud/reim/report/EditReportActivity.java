package com.rushucloud.reim.report;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.model.Category;
import classes.model.Comment;
import classes.model.Currency;
import classes.model.Image;
import classes.model.Item;
import classes.model.Report;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.LogUtils;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.common.SyncDataCallback;
import netUtils.common.SyncUtils;
import netUtils.request.common.UploadImageRequest;
import netUtils.request.group.GetGroupRequest;
import netUtils.request.item.CreateItemRequest;
import netUtils.request.item.ModifyItemRequest;
import netUtils.request.report.CreateReportRequest;
import netUtils.request.report.GetReportRequest;
import netUtils.request.report.ModifyReportRequest;
import netUtils.response.common.UploadImageResponse;
import netUtils.response.group.GetGroupResponse;
import netUtils.response.item.CreateItemResponse;
import netUtils.response.item.ModifyItemResponse;
import netUtils.response.report.CreateReportResponse;
import netUtils.response.report.GetReportResponse;
import netUtils.response.report.ModifyReportResponse;

public class EditReportActivity extends Activity
{
    // Widgets
    private EditText titleEditText;
    private TextView timeTextView;
    private TextView statusTextView;

    private TextView managerTextView;
    private TextView ccTextView;

    private TextView amountTextView;
    private TextView itemCountTextView;
    private LinearLayout itemLayout;
    private ImageView commentTipImageView;
    private PopupWindow deletePopupWindow;

    // Local Data
    private AppPreference appPreference;
    private DBManager dbManager;

    private Report report;
    private List<Item> itemList = new ArrayList<>();
    private ArrayList<Integer> chosenItemIDList = new ArrayList<>();

    private User currentUser;

    private int itemIndex;
    private boolean fromPush;
    private boolean newReport;
    private boolean hasInit = false;
    private int lastCommentCount = 0;

    private List<Image> imageSyncList = new ArrayList<>();
    private List<Item> itemSyncList = new ArrayList<>();
    private int imageTaskCount;
    private int imageTaskSuccessCount;
    private int itemTaskCount;
    private int itemTaskSuccessCount;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_edit);
        initData();
        initView();

        if (newReport)
        {
            ViewUtils.requestFocus(this, titleEditText);
        }
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("EditReportActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        refreshView();

        List<Comment> commentList = dbManager.getReportComments(report.getLocalID());
        lastCommentCount = commentList.size();
        if (!hasInit && report.getServerID() != -1 && PhoneUtils.isNetworkConnected())
        {
            sendGetGroupRequest();
        }
        else if (report.getLocalID() == -1 && report.getServerID() == -1 && fromPush)
        {
            ViewUtils.showToast(this, R.string.error_report_deleted);
            goBackToMainActivity();
        }
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("EditReportActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBackToMainActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case Constant.ACTIVITY_PICK_MANAGER:
                {
                    List<User> managerList = (List<User>) data.getSerializableExtra("managers");
                    report.setManagerList(managerList);
                    managerTextView.setText(report.getManagersName());
                    break;
                }
                case Constant.ACTIVITY_PICK_CC:
                {
                    List<User> ccList = (List<User>) data.getSerializableExtra("ccs");
                    report.setCCList(ccList);
                    ccTextView.setText(report.getCCsName());
                    break;
                }
                case Constant.ACTIVITY_PICK_ITEMS:
                {
                    Bundle bundle = data.getExtras();
                    chosenItemIDList.clear();
                    chosenItemIDList.addAll(bundle.getIntegerArrayList("chosenItemIDList"));
                    itemList = dbManager.getItems(chosenItemIDList);
                    report.setType(bundle.getInt("type"));
                    refreshView();
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
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBackToMainActivity();
            }
        });

        TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (newReport)
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_SAVE");
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_SAVE");
                }

                hideSoftKeyboard();
                if (saveReport())
                {
                    if (SyncUtils.canSyncToServer())
                    {
                        SyncUtils.isSyncOnGoing = true;
                        SyncUtils.syncAllToServer(new SyncDataCallback()
                        {
                            public void execute()
                            {
                                SyncUtils.isSyncOnGoing = false;
                            }
                        });
                    }
                    ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_saving_report);
                    goBackToMainActivity();
                }
                else
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_save_report);
                }
            }
        });

        itemLayout = (LinearLayout) findViewById(R.id.itemLayout);

        titleEditText = (EditText) findViewById(R.id.titleEditText);
        titleEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        titleEditText.setText(report.getTitle());
        if (report.getTitle().isEmpty())
        {
            ViewUtils.requestFocus(this, titleEditText);
        }
        else
        {
            hideSoftKeyboard();
        }

        int createDate = report.getCreatedDate() == -1 ? Utils.getCurrentTime() : report.getCreatedDate();
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        timeTextView.setText(Utils.secondToStringUpToMinute(createDate));

        statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText(report.getStatusString());
        statusTextView.setBackgroundResource(report.getStatusBackground());

        TextView approveInfoTextView = (TextView) findViewById(R.id.approveInfoTextView);
        approveInfoTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_MINE_STATUS");
                Intent intent = new Intent(EditReportActivity.this, ApproveInfoActivity.class);
                intent.putExtra("reportServerID", report.getServerID());
                ViewUtils.goForward(EditReportActivity.this, intent);
            }
        });
        if (report.getStatus() == Report.STATUS_DRAFT && !report.isAaApproved())
        {
            approveInfoTextView.setVisibility(View.INVISIBLE);
        }

        managerTextView = (TextView) findViewById(R.id.managerTextView);
        managerTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (newReport)
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_SEND");
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_SEND");
                }

                hideSoftKeyboard();
                Intent intent = new Intent(EditReportActivity.this, PickManagerActivity.class);
                intent.putExtra("managers", (Serializable) report.getManagerList());
                intent.putExtra("newReport", newReport);
                ViewUtils.goForwardForResult(EditReportActivity.this, intent, Constant.ACTIVITY_PICK_MANAGER);
            }
        });

        ccTextView = (TextView) findViewById(R.id.ccTextView);
        ccTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (newReport)
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_CC");
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_CC");
                }

                hideSoftKeyboard();
                Intent intent = new Intent(EditReportActivity.this, PickCCActivity.class);
                intent.putExtra("ccs", (Serializable) report.getCCList());
                intent.putExtra("newReport", newReport);
                ViewUtils.goForwardForResult(EditReportActivity.this, intent, Constant.ACTIVITY_PICK_CC);
            }
        });

        amountTextView = (TextView) findViewById(R.id.amountTextView);
        amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
        itemCountTextView = (TextView) findViewById(R.id.itemCountTextView);

        ImageView addImageView = (ImageView) findViewById(R.id.addImageView);
        addImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (newReport)
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_ADDITEM");
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_ADDITEM");
                }

                hideSoftKeyboard();
                report.setTitle(titleEditText.getText().toString());

                Bundle bundle = new Bundle();
                bundle.putSerializable("report", report);
                bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
                Intent intent = new Intent(EditReportActivity.this, PickItemActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForwardForResult(EditReportActivity.this, intent, Constant.ACTIVITY_PICK_ITEMS);
            }
        });

        commentTipImageView = (ImageView) findViewById(R.id.commentTipImageView);
        if (getIntent().getExtras().getBoolean("commentPrompt", false))
        {
            commentTipImageView.setVisibility(View.VISIBLE);
        }

        Button commentButton = (Button) findViewById(R.id.commentButton);
        commentButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                commentTipImageView.setVisibility(View.GONE);
                if (report.getCommentList() == null || report.getCommentList().isEmpty())
                {
                    if (newReport)
                    {
                        MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_COMMENT");
                    }
                    else
                    {
                        MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_COMMENT");
                    }

                    if (!PhoneUtils.isNetworkConnected())
                    {
                        ViewUtils.showToast(EditReportActivity.this, R.string.error_comment_network_unavailable);
                    }
                    else
                    {
                        showCommentDialog();
                    }
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_MINE_COMMENT");

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("report", report);
                    bundle.putBoolean("myReport", true);
                    bundle.putBoolean("newReport", newReport);
                    Intent intent = new Intent(EditReportActivity.this, CommentActivity.class);
                    intent.putExtras(bundle);
                    ViewUtils.goForward(EditReportActivity.this, intent);
                }
            }
        });

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (newReport)
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_NEW_SUBMIT");
                }
                else
                {
                    MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_EDIT_SUBMIT");
                }

                hideSoftKeyboard();

                for (Item item : itemList)
                {
                    if (item.missingInfo())
                    {
                        ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_item_miss_info);
                        return;
                    }
                }

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_network_unavailable);
                }
                else if (titleEditText.getText().toString().isEmpty())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_report_title_empty);
                    ViewUtils.requestFocus(EditReportActivity.this, titleEditText);
                }
                else if (report.getManagerList() == null || report.getManagerList().isEmpty())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.no_manager);
                }
                else if (itemList.isEmpty())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_empty);
                }
                else if (SyncUtils.isSyncOnGoing)
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.prompt_sync_ongoing);
                }
                else
                {
                    submitReport();
                }
            }
        });

        initDeleteWindow();
    }

    private void initDeleteWindow()
    {
        View deleteView = View.inflate(this, R.layout.window_delete, null);

        Button deleteButton = (Button) deleteView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                chosenItemIDList.remove(itemIndex);
                itemList.remove(itemIndex);

                deletePopupWindow.dismiss();
                refreshView();
            }
        });

        Button cancelButton = (Button) deleteView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                deletePopupWindow.dismiss();
            }
        });

        deletePopupWindow = ViewUtils.buildBottomPopupWindow(this, deleteView);
    }

    private void refreshView()
    {
        if (report.getCreatedDate() > 0)
        {
            timeTextView.setText(Utils.secondToStringUpToMinute(report.getCreatedDate()));
        }

        statusTextView.setText(report.getStatusString());
        statusTextView.setBackgroundResource(report.getStatusBackground());

        managerTextView.setText(report.getManagersName());
        ccTextView.setText(report.getCCsName());

        itemList = dbManager.getItems(Item.getItemsIDList(itemList));

        itemCountTextView.setText(String.format(getString(R.string.item_count_added), itemList.size()));

        itemLayout.removeAllViews();

        double amount = 0;
        for (int i = 0; i < itemList.size(); i++)
        {
            LayoutInflater inflater = LayoutInflater.from(this);
            final Item item = itemList.get(i);
            final int index = i;
            View view = inflater.inflate(R.layout.list_report_item_edit, null);
            view.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
                    intent.putExtra("itemLocalID", item.getLocalID());
                    intent.putExtra("fromEditReport", true);
                    ViewUtils.goForward(EditReportActivity.this, intent);
                }
            });
            view.setOnLongClickListener(new OnLongClickListener()
            {
                public boolean onLongClick(View v)
                {
                    itemIndex = index;
                    showDeleteWindow();
                    return false;
                }
            });

            TextView symbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
            TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
            ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
            ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);

            symbolTextView.setText(item.getCurrency().getSymbol());

            amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            amountTextView.setText(Utils.formatDouble(item.getAmount()));

            String vendor = item.getVendor().isEmpty() ? getString(R.string.vendor_not_available) : item.getVendor();
            vendorTextView.setText(vendor);

            if (item.missingInfo())
            {
                warningImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                Category category = item.getCategory();
                ViewUtils.setImageViewBitmap(category, categoryImageView);
            }

            itemLayout.addView(view);

            if (item.getCurrency().isCNY())
            {
                amount += item.getAmount();
            }
            else if (item.getRate() != 0)
            {
                amount += item.getAmount() * item.getRate() / 100;
            }
            else
            {
                Currency currency = dbManager.getCurrency(item.getCurrency().getCode());
                amount += item.getAmount() * currency.getRate() / 100;
            }
        }
        amountTextView.setText(Utils.formatDouble(amount));
    }

    private void showDeleteWindow()
    {
        deletePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        deletePopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void showCommentDialog()
    {
        View view = View.inflate(this, R.layout.dialog_report_comment, null);

        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText(R.string.add_comment);

        final EditText commentEditText = (EditText) view.findViewById(R.id.commentEditText);
        commentEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        ViewUtils.requestFocus(this, commentEditText);

        Builder builder = new Builder(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_MINE_DIALOG_COMMENT_SEND");

                String comment = commentEditText.getText().toString();
                if (comment.isEmpty())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_comment_empty);
                }
                else if (report.getLocalID() == -1)
                {
                    saveReport();
                    sendCreateReportCommentRequest(comment);
                }
                else
                {
                    Report localReport = dbManager.getReportByLocalID(report.getLocalID());
                    if (localReport.getServerID() == -1)
                    {
                        saveReport();
                        sendCreateReportCommentRequest(comment);
                    }
                    else
                    {
                        report.setServerID(localReport.getServerID());
                        report.setLocalUpdatedDate(localReport.getLocalUpdatedDate());
                        report.setServerUpdatedDate(localReport.getServerUpdatedDate());
                        saveReport();
                        sendModifyReportCommentRequest(comment);
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                MobclickAgent.onEvent(EditReportActivity.this, "UMENG_REPORT_MINE_DIALOG_COMMENT_CLOSE");
            }
        });
        builder.create().show();
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }

    private void goBackToMainActivity()
    {
        ReimApplication.setTabIndex(Constant.TAB_REPORT);
        ReimApplication.setReportTabIndex(Constant.TAB_REPORT_MINE);
        if (fromPush)
        {
            Intent intent = new Intent(EditReportActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ViewUtils.goBackWithIntent(this, intent);
        }
        else
        {
            ViewUtils.goBack(this);
        }
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();

        currentUser = appPreference.getCurrentUser();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            fromPush = bundle.getBoolean("fromPush", false);
            report = (Report) bundle.getSerializable("report");
            if (fromPush)
            {
                report = dbManager.getReportByServerID(report.getServerID());
            }
            else
            {
                lastCommentCount = report.getCommentList() != null ? report.getCommentList().size() : 0;
            }
            newReport = report.getLocalID() == -1;
            if (!newReport)
            {
                itemList = dbManager.getReportItems(report.getLocalID());
                chosenItemIDList = Item.getItemsIDList(itemList);
            }
        }
    }

    private boolean saveReport()
    {
        Report localReport = dbManager.getReportByLocalID(report.getLocalID());
        if (localReport != null)
        {
            report.setServerID(localReport.getServerID());
        }
        report.setTitle(titleEditText.getText().toString());
        report.setLocalUpdatedDate(Utils.getCurrentTime());
        if (report.getLocalID() == -1)
        {
            report.setCreatedDate(Utils.getCurrentTime());
            report.setLocalID(dbManager.insertReport(report));
        }
        else
        {
            dbManager.updateReportByLocalID(report);
        }
        return dbManager.updateReportItems(chosenItemIDList, report.getLocalID());
    }

    // Network
    private void submitReport()
    {
        ReimProgressDialog.show();

        saveReport();

        imageSyncList.clear();

        for (Item item : itemList)
        {
            for (Image image : item.getInvoices())
            {
                if (image.isNotUploaded())
                {
                    imageSyncList.add(image);
                }
            }
        }

        imageTaskCount = imageSyncList.size();
        imageTaskSuccessCount = imageTaskCount;

        if (imageTaskCount > 0)
        {
            for (Image image : imageSyncList)
            {
                sendUploadImageRequest(image);
            }
        }
        else
        {
            syncItems();
        }
    }

    private void syncItems()
    {
        itemList = dbManager.getItems(Item.getItemsIDList(itemList));
        itemSyncList.clear();

        for (Item item : itemList)
        {
            if (item.needToSync())
            {
                itemSyncList.add(item);
            }
        }

        itemTaskCount = itemSyncList.size();
        itemTaskSuccessCount = itemTaskCount;

        if (itemTaskCount > 0)
        {
            for (Item item : itemSyncList)
            {
                if (item.getServerID() == -1)
                {
                    sendCreateItemRequest(item);
                }
                else
                {
                    sendModifyItemRequest(item);
                }
            }
        }
        else
        {
            syncReport();
        }
    }

    private void syncReport()
    {
        int originalStatus = report.getStatus();
        if (appPreference.getCurrentGroupID() == -1)
        {
            report.setStatus(Report.STATUS_FINISHED);
        }
        else
        {
            report.setStatus(Report.STATUS_SUBMITTED);
        }

        if (report.canBeSubmitted())
        {
            if (report.getServerID() == -1)
            {
                sendCreateReportRequest();
            }
            else
            {
                sendModifyReportRequest(originalStatus);
            }
        }
        else
        {
            ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_item_not_uploaded);
        }
    }

    private void sendUploadImageRequest(final Image image)
    {
        LogUtils.println("upload image：local id " + image.getLocalID());
        UploadImageRequest request = new UploadImageRequest(image.getLocalPath(), NetworkConstant.IMAGE_TYPE_INVOICE);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final UploadImageResponse response = new UploadImageResponse(httpResponse);
                if (response.getStatus())
                {
                    LogUtils.println("upload image：local id " + image.getLocalID() + " *Succeed*");
                    image.setServerID(response.getImageID());
                    image.setServerPath(response.getPath());
                    dbManager.updateImageServerID(image);

                    imageTaskCount--;
                    imageTaskSuccessCount--;
                    if (imageTaskCount == 0 && imageTaskSuccessCount == 0)
                    {
                        syncItems();
                    }
                    else if (imageTaskCount == 0)
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        });
                    }
                }
                else
                {
                    LogUtils.println("upload image：local id " + image.getLocalID() + " *Failed*");

                    imageTaskCount--;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_upload_invoice);
                            if (imageTaskCount == 0)
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendCreateItemRequest(final Item item)
    {
        LogUtils.println("create item：local id " + item.getLocalID());
        CreateItemRequest request = new CreateItemRequest(item);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                CreateItemResponse response = new CreateItemResponse(httpResponse);
                if (response.getStatus())
                {
                    LogUtils.println("create item：local id " + item.getLocalID() + " *Succeed*");
                    int currentTime = Utils.getCurrentTime();
                    item.setLocalUpdatedDate(currentTime);
                    item.setServerUpdatedDate(currentTime);
                    item.setServerID(response.getItemID());
                    item.setRate(response.getRate());
                    dbManager.updateItemByLocalID(item);

                    itemTaskCount--;
                    itemTaskSuccessCount--;
                    if (itemTaskCount == 0 && itemTaskSuccessCount == 0)
                    {
                        syncReport();
                    }
                    else if (itemTaskCount == 0)
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        });
                    }
                }
                else
                {
                    LogUtils.println("create item：local id " + item.getLocalID() + " *Failed*");

                    itemTaskCount--;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_create_item);
                            if (itemTaskCount == 0)
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendModifyItemRequest(final Item item)
    {
        LogUtils.println("modify item：local id " + item.getLocalID());
        ModifyItemRequest request = new ModifyItemRequest(item);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                ModifyItemResponse response = new ModifyItemResponse(httpResponse);
                if (response.getStatus())
                {
                    LogUtils.println("modify item：local id " + item.getLocalID() + " *Succeed*");
                    int currentTime = Utils.getCurrentTime();
                    item.setRate(response.getRate());
                    item.setLocalUpdatedDate(currentTime);
                    item.setServerUpdatedDate(currentTime);
                    dbManager.updateItem(item);

                    itemTaskCount--;
                    itemTaskSuccessCount--;
                    if (itemTaskCount == 0 && itemTaskSuccessCount == 0)
                    {
                        syncReport();
                    }
                    else if (itemTaskCount == 0)
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        });
                    }
                }
                else
                {
                    LogUtils.println("modify item：local id " + item.getLocalID() + " *Failed*");

                    itemTaskCount--;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_modify_item);
                            if (itemTaskCount == 0)
                            {
                                ReimProgressDialog.dismiss();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendGetReportRequest(final int reportServerID)
    {
        GetReportRequest request = new GetReportRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                hasInit = true;
                final GetReportResponse response = new GetReportResponse(httpResponse);
                if (response.getStatus())
                {
                    if (fromPush)
                    {
                        report.setStatus(response.getReport().getStatus());
                        report.setCommentList(response.getReport().getCommentList());
                        dbManager.updateReportByLocalID(report);

                        dbManager.deleteReportComments(report.getLocalID());
                        for (Comment comment : report.getCommentList())
                        {
                            comment.setReportID(report.getLocalID());
                            dbManager.insertComment(comment);
                        }
                    }
                    else if (report.getLocalUpdatedDate() <= response.getReport().getServerUpdatedDate())
                    {
                        report.setAaApproved(response.getReport().isAaApproved());
                        report.setManagerList(response.getReport().getManagerList());
                        report.setCCList(response.getReport().getCCList());
                        report.setCommentList(response.getReport().getCommentList());
                        if (report.getManagerList().isEmpty())
                        {
                            report.setManagerList(currentUser.buildBaseManagerList());
                        }
                        dbManager.updateReportByLocalID(report);

                        for (Item item : response.getItemList())
                        {
                            item.setBelongReport(report);
                            dbManager.updateItemByServerID(item);
                        }
                        itemList = dbManager.getReportItems(report.getLocalID());

                        dbManager.deleteReportComments(report.getLocalID());
                        for (Comment comment : report.getCommentList())
                        {
                            comment.setReportID(report.getLocalID());
                            dbManager.insertComment(comment);
                        }
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            refreshView();

                            if (report.getCommentList().size() != lastCommentCount)
                            {
                                commentTipImageView.setVisibility(View.VISIBLE);
                                lastCommentCount = report.getCommentList().size();
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendGetGroupRequest()
    {
        ReimProgressDialog.show();
        GetGroupRequest request = new GetGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetGroupResponse response = new GetGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentGroupID = response.getGroup() == null ? -1 : response.getGroup().getServerID();

                    // update members
                    List<User> memberList = response.getMemberList();
                    User currentUser = AppPreference.getAppPreference().getCurrentUser();

                    for (int i = 0; i < memberList.size(); i++)
                    {
                        User user = memberList.get(i);
                        if (currentUser != null && user.equals(currentUser))
                        {
                            if (user.getServerUpdatedDate() > currentUser.getServerUpdatedDate())
                            {
                                if (user.getAvatarID() == currentUser.getAvatarID())
                                {
                                    user.setAvatarLocalPath(currentUser.getAvatarLocalPath());
                                }
                            }
                            else
                            {
                                memberList.set(i, currentUser);
                            }
                        }
                    }

                    dbManager.updateGroupUsers(memberList, currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    sendGetReportRequest(report.getServerID());
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }

    private void sendCreateReportRequest()
    {
        CreateReportRequest request = new CreateReportRequest(report);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateReportResponse response = new CreateReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    report.setServerID(response.getReportID());
                    report.setServerUpdatedDate(currentTime);
                    report.setLocalUpdatedDate(currentTime);
                    dbManager.updateReportByLocalID(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_submitting_report);
                            goBackToMainActivity();
                        }
                    });
                }
                else
                {
                    report.setStatus(Report.STATUS_DRAFT);
                    dbManager.updateReportByLocalID(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (response.getCode() != NetworkConstant.ERROR_NOT_UNIQUE)
                            {
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendModifyReportRequest(final int originalStatus)
    {
        ModifyReportRequest request = new ModifyReportRequest(report);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    report.setServerUpdatedDate(currentTime);
                    report.setLocalUpdatedDate(currentTime);
                    dbManager.updateReportByLocalID(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_submitting_report);
                            goBackToMainActivity();
                        }
                    });
                }
                else
                {
                    report.setStatus(originalStatus);
                    dbManager.updateReportByLocalID(report);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            if (response.getCode() != NetworkConstant.ERROR_NOT_UNIQUE)
                            {
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendCreateReportCommentRequest(final String commentContent)
    {
        ReimProgressDialog.show();
        CreateReportRequest request = new CreateReportRequest(report, commentContent);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateReportResponse response = new CreateReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    report.setServerID(response.getReportID());
                    report.setServerUpdatedDate(currentTime);
                    report.setLocalUpdatedDate(currentTime);
                    dbManager.updateReportByLocalID(report);

                    Comment comment = new Comment();
                    comment.setContent(commentContent);
                    comment.setCreatedDate(currentTime);
                    comment.setLocalUpdatedDate(currentTime);
                    comment.setServerUpdatedDate(currentTime);
                    comment.setReportID(report.getLocalID());
                    comment.setReviewer(currentUser);
                    dbManager.insertComment(comment);

                    report.setCommentList(dbManager.getReportComments(report.getLocalID()));
                    lastCommentCount++;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_sending_comment);
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendModifyReportCommentRequest(final String commentContent)
    {
        ReimProgressDialog.show();
        ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
                if (response.getStatus())
                {
                    int currentTime = Utils.getCurrentTime();
                    report.setServerUpdatedDate(currentTime);
                    report.setLocalUpdatedDate(currentTime);
                    dbManager.updateReportByLocalID(report);

                    Comment comment = new Comment();
                    comment.setContent(commentContent);
                    comment.setCreatedDate(currentTime);
                    comment.setLocalUpdatedDate(currentTime);
                    comment.setServerUpdatedDate(currentTime);
                    comment.setReportID(report.getLocalID());
                    comment.setReviewer(currentUser);
                    dbManager.insertComment(comment);

                    report.setCommentList(dbManager.getReportComments(report.getLocalID()));
                    lastCommentCount++;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditReportActivity.this, R.string.succeed_in_sending_comment);
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_send_comment, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}