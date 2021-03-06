package com.rushucloud.reim.report;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
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
import classes.model.Group;
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

    private TextView totalTextView;
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

    private Group currentGroup;
    private User currentUser;

    private int itemIndex;
    private boolean fromPush;
    private boolean newReport;
    private boolean hasInit = false;
    private int lastCommentCount = 0;
    private List<Integer> idList = new ArrayList<>();
    private SparseIntArray daysArray = new SparseIntArray();
    private SparseIntArray countArray = new SparseIntArray();

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
            sendGetReportRequest(report.getServerID());
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
        ReimProgressDialog.setContext(this);
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
                    report.setType(bundle.getInt("type"));
                    refreshItemList(dbManager.getItems(chosenItemIDList));
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
                if (!appPreference.hasProxyEditPermission())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_modify_report_no_permission);
                }
                else if (chosenItemIDList.isEmpty())
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_no_items);
                }
                else
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

        totalTextView = (TextView) findViewById(R.id.totalTextView);
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
                if (appPreference.hasProxyEditPermission())
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
                        if (item.missingInfo(currentGroup))
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
                else
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_no_permission);
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
                chosenItemIDList.remove(itemIndex); // remove(int index) not remove(Integer id)
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

        refreshItemList(dbManager.getItems(Item.getItemsIDList(itemList)));

        itemCountTextView.setText(String.format(getString(R.string.item_count_added), itemList.size()));

        itemLayout.removeAllViews();

        double amount = 0;
        boolean containsForeignCurrency = false;
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
                    if (deletePopupWindow == null || !deletePopupWindow.isShowing())
                    {
                        Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
                        intent.putExtra("itemLocalID", item.getLocalID());
                        intent.putExtra("fromEditReport", true);
                        ViewUtils.goForward(EditReportActivity.this, intent);
                    }
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

            TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            ImageView categoryImageView = (ImageView) view.findViewById(R.id.categoryImageView);
            TextView categoryTextView = (TextView) view.findViewById(R.id.categoryTextView);
            TextView symbolTextView = (TextView) view.findViewById(R.id.symbolTextView);
            TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            TextView noteTextView = (TextView) view.findViewById(R.id.noteTextView);
            TextView vendorTextView = (TextView) view.findViewById(R.id.vendorTextView);
            ImageView warningImageView = (ImageView) view.findViewById(R.id.warningImageView);

            dateTextView.setText(Utils.secondToStringUpToDay(item.getConsumedDate()));

            symbolTextView.setText(item.getCurrency().getSymbol());

            if (item.getCurrency().isCNY())
            {
                amount += item.getAmount();
            }
            else if (item.getRate() != 0)
            {
                containsForeignCurrency = true;
                amount += item.getAmount() * item.getRate() / 100;
            }
            else
            {
                containsForeignCurrency = true;
                amount += item.getAmount() * item.getCurrency().getRate() / 100;
            }

            amountTextView.setTypeface(ReimApplication.TypeFaceAleoLight);
            amountTextView.setText(Utils.formatDouble(item.getAmount()));

            String note = !item.getNote().isEmpty() ? item.getNote() : "";
            int days = daysArray.get(item.getServerID());
            int count = countArray.get(item.getLocalID());
            if (days > 0)
            {
                note = item.getDailyAverage(days) + " " + note;
            }
            else if (count > 1)
            {
                note = item.getPerCapita(count) + " " + note;
            }
            noteTextView.setText(note);

            String vendor = item.getVendor().isEmpty() ? getString(R.string.vendor_not_available) : item.getVendor();
            vendorTextView.setText(vendor);

            Category category = item.getCategory();
            ViewUtils.setImageViewBitmap(category, categoryImageView);

            String categoryName = category != null ? category.getName() : "";
            categoryTextView.setText(categoryName);

            if (item.missingInfo(currentGroup) || idList.contains(item.getCategory().getServerID()))
            {
                warningImageView.setVisibility(View.VISIBLE);
            }

            itemLayout.addView(view);
        }

        int prompt = containsForeignCurrency ? R.string.equivalent_amount : R.string.total_amount;
        totalTextView.setText(prompt);

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

        currentGroup = appPreference.getCurrentGroup();
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
                refreshItemList(dbManager.getReportItems(report.getLocalID()));
                chosenItemIDList = Item.getItemsIDList(itemList);
            }
        }
    }

    private void refreshItemList(List<Item> items)
    {
        itemList.clear();
        itemList.addAll(items);

        daysArray.clear();
        countArray.clear();
        for (Item item : itemList)
        {
            daysArray.put(item.getServerID(), item.getDurationDays());
            countArray.put(item.getLocalID(), item.getMemberCount());
        }
    }

    private void updateReport(Report responseReport, List<Item> responseItemList)
    {
        if (fromPush)
        {
            report.setStatus(responseReport.getStatus());
            report.setCommentList(responseReport.getCommentList());
            dbManager.updateReportByLocalID(report);

            dbManager.deleteReportComments(report.getLocalID());
            for (Comment comment : report.getCommentList())
            {
                comment.setReportID(report.getLocalID());
                dbManager.insertComment(comment);
            }
        }
        else if (report.getLocalUpdatedDate() <= responseReport.getServerUpdatedDate())
        {
            report.setAaApproved(responseReport.isAaApproved());
            report.setManagerList(responseReport.getManagerList());
            report.setCCList(responseReport.getCCList());
            report.setCommentList(responseReport.getCommentList());
            if (report.getManagerList().isEmpty())
            {
                report.setManagerList(currentUser.buildBaseManagerList());
            }
            dbManager.updateReportByLocalID(report);

            for (Item item : responseItemList)
            {
                item.setBelongReport(report);
                dbManager.updateItemByServerID(item);
            }
            refreshItemList(dbManager.getReportItems(report.getLocalID()));

            dbManager.deleteReportComments(report.getLocalID());
            for (Comment comment : report.getCommentList())
            {
                comment.setReportID(report.getLocalID());
                dbManager.insertComment(comment);
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
        refreshItemList(dbManager.getItems(Item.getItemsIDList(itemList)));
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
        int status = appPreference.getCurrentGroupID() == -1 ? Report.STATUS_FINISHED : Report.STATUS_SUBMITTED;
        report.setStatus(status);

        if (report.canBeSubmitted())
        {
            if (report.getServerID() == -1)
            {
                sendCreateReportRequest(false);
            }
            else
            {
                sendModifyReportRequest(originalStatus, false);
            }
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_item_unsynced);
                }
            });
        }
    }

    private void sendUploadImageRequest(final Image image)
    {
        LogUtils.println("upload image：local id " + image.getLocalID());
        UploadImageRequest request = new UploadImageRequest(image.getLocalPath(), Image.TYPE_INVOICE);
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_image_unsynced);
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_item_unsynced);
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
                            ViewUtils.showToast(EditReportActivity.this, R.string.error_submit_report_item_unsynced);
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
        ReimProgressDialog.show();
        GetReportRequest request = new GetReportRequest(reportServerID);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                hasInit = true;
                final GetReportResponse response = new GetReportResponse(httpResponse);
                if (response.getStatus())
                {
                    if (!response.containsUnsyncedUser())
                    {
                        updateReport(response.getReport(), response.getItemList());
                    }
                    else
                    {
                        Report report = response.getReport();
                        report.setManagerList(response.getManagerList());
                        report.setCCList(response.getCCList());
                        sendGetGroupRequest(response.getReport(), response.getItemList());
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (!response.containsUnsyncedUser())
                            {
                                ReimProgressDialog.dismiss();
                                refreshView();

                                if (report.getCommentList().size() != lastCommentCount)
                                {
                                    commentTipImageView.setVisibility(View.VISIBLE);
                                    lastCommentCount = report.getCommentList().size();
                                }
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

    private void sendGetGroupRequest(final Report responseReport, final List<Item> responseItemList)
    {
        GetGroupRequest request = new GetGroupRequest();
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final GetGroupResponse response = new GetGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Utils.updateGroupMembers(response.getGroup(), response.getMemberList(), dbManager);

                    // update report
                    updateReport(responseReport, responseItemList);
                    report = dbManager.getReportByServerID(responseReport.getServerID());

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
                            goBackToMainActivity();
                        }
                    });
                }
            }
        });
    }

    private void sendCreateReportRequest(boolean forceSubmit)
    {
        CreateReportRequest request = new CreateReportRequest(report, forceSubmit);
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
                            if (response.getCode() == NetworkConstant.ERROR_CATEGORY_COUNT_EXCEED_LIMIT)
                            {
                                idList.clear();
                                idList.addAll(response.getErrorCategoryIDList());
                                refreshView();

                                String errorMessage = String.format(getString(R.string.error_network_category_count_exceed_limit),
                                                                    dbManager.getCategoriesNames(idList));
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report, errorMessage);
                            }
                            else if (response.getCode() == NetworkConstant.ERROR_CATEGORY_AMOUNT_EXCEED_LIMIT)
                            {
                                idList.clear();
                                idList.addAll(response.getErrorCategoryIDList());
                                refreshView();

                                String errorMessage = String.format(getString(R.string.prompt_category_exceed_limit_choose),
                                                                    dbManager.getCategoriesNames(idList));

                                Builder builder = new Builder(EditReportActivity.this);
                                builder.setTitle(R.string.warning);
                                builder.setMessage(errorMessage);
                                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        ReimProgressDialog.show();
                                        int status = appPreference.getCurrentGroupID() == -1 ?
                                                Report.STATUS_FINISHED : Report.STATUS_SUBMITTED;
                                        report.setStatus(status);
                                        sendCreateReportRequest(true);
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, null);
                                builder.create().show();
                            }
                            else
                            {
                                idList.clear();
                                refreshView();
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report, response.getErrorMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendModifyReportRequest(final int originalStatus, final boolean forceSubmit)
    {
        ModifyReportRequest request = new ModifyReportRequest(report, forceSubmit);
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
                            if (response.getCode() == NetworkConstant.ERROR_CATEGORY_COUNT_EXCEED_LIMIT)
                            {
                                idList.clear();
                                idList.addAll(response.getErrorCategoryIDList());
                                refreshView();

                                String errorMessage = String.format(getString(R.string.error_network_category_count_exceed_limit),
                                                                    dbManager.getCategoriesNames(idList));
                                ViewUtils.showToast(EditReportActivity.this, R.string.failed_to_submit_report, errorMessage);
                            }
                            else if (response.getCode() == NetworkConstant.ERROR_CATEGORY_AMOUNT_EXCEED_LIMIT)
                            {
                                idList.clear();
                                idList.addAll(response.getErrorCategoryIDList());
                                refreshView();

                                String errorMessage = String.format(getString(R.string.prompt_category_exceed_limit_choose),
                                                                    dbManager.getCategoriesNames(idList));

                                Builder builder = new Builder(EditReportActivity.this);
                                builder.setTitle(R.string.warning);
                                builder.setMessage(errorMessage);
                                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        ReimProgressDialog.show();
                                        int status = appPreference.getCurrentGroupID() == -1 ?
                                                Report.STATUS_FINISHED : Report.STATUS_SUBMITTED;
                                        report.setStatus(status);
                                        sendModifyReportRequest(originalStatus, true);
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, null);
                                builder.create().show();
                            }
                            else
                            {
                                idList.clear();
                                refreshView();
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