package com.rushucloud.reim.item;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.rushucloud.reim.R;
import com.rushucloud.reim.common.GalleryActivity;
import com.rushucloud.reim.common.MultipleImageActivity;
import com.rushucloud.reim.report.EditReportActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classes.model.Category;
import classes.model.Currency;
import classes.model.DidiExpense;
import classes.model.Image;
import classes.model.Item;
import classes.model.Report;
import classes.model.Tag;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.Constant;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ReimApplication;
import classes.utils.TextLengthFilter;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;
import classes.widget.ReimProgressDialog;
import classes.widget.wheelview.WheelView;
import classes.widget.wheelview.adapter.ArrayWheelAdapter;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.NetworkConstant;
import netUtils.request.common.DownloadImageRequest;
import netUtils.request.item.ChangeAmountRequest;
import netUtils.response.common.DownloadImageResponse;
import netUtils.response.item.ChangeAmountResponse;

public class EditItemActivity extends Activity
{
    // Widgets
    private TextView symbolTextView;
    private EditText amountEditText;
    private ImageView amountWarningImageView;

    private PopupWindow typePopupWindow;
    private TextView typeTextView;
    private ImageView consumedImageView;
    private ImageView budgetImageView;
    private ImageView borrowingImageView;
    private RelativeLayout needReimLayout;
    private ToggleButton needReimToggleButton;

    private LinearLayout invoiceLayout;
    private ImageView addInvoiceImageView;
    private PopupWindow picturePopupWindow;

    private TextView timeTextView;
    private PopupWindow timePopupWindow;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private TextView vendorTextView;

    private TextView locationTextView;

    private TextView currencyTextView;
    private PopupWindow currencyPopupWindow;

    private ImageView categoryImageView;
    private TextView categoryTextView;
    private ImageView categoryWarningImageView;

    private LinearLayout tagContainerLayout;
    private LinearLayout tagLayout;

    private LinearLayout memberLayout;

    private EditText noteEditText;

    // Local Data
    private static AppPreference appPreference;
    private static DBManager dbManager;

    private List<ImageView> removeList = null;
    boolean removeImageViewShown = false;

    private List<Currency> currencyList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Tag> tagList = new ArrayList<>();

    private Item item;
    private List<Image> originInvoiceList = new ArrayList<>();

    private boolean fromReim;
    private boolean fromEditReport;
    private boolean fromPickItems;
    private boolean fromApproveReport;
    private boolean newItem = false;

    private LocationClient locationClient = null;
    private BDLocationListener listener = new ReimLocationListener();
    private BDLocation currentLocation;
    private String currentCity = "";

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reim_edit);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("EditItemActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        locationClient.registerLocationListener(listener);
        getLocation();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("EditItemActivity");
        MobclickAgent.onPause(this);
        locationClient.unRegisterLocationListener(listener);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (removeImageViewShown)
            {
                for (ImageView removeImageView : removeList)
                {
                    removeImageView.setVisibility(View.INVISIBLE);
                }
                removeImageViewShown = false;
            }
            else
            {
                goBack();
            }
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
                case Constant.ACTIVITY_PICK_IMAGE:
                {
                    try
                    {
                        String[] paths = data.getStringArrayExtra("paths");

                        ReimProgressDialog.show();
                        for (String path : paths)
                        {
                            String invoicePath = PhoneUtils.saveBitmapToFile(path, NetworkConstant.IMAGE_TYPE_INVOICE);
                            if (!invoicePath.isEmpty())
                            {
                                Image image = new Image();
                                image.setLocalPath(invoicePath);
                                item.getInvoices().add(image);
                            }
                        }

                        refreshInvoiceView();
                        ReimProgressDialog.dismiss();
                    }
                    catch (Exception e)
                    {
                        ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
                        e.printStackTrace();
                    }
                    break;
                }
                case Constant.ACTIVITY_TAKE_PHOTO:
                {
                    try
                    {
                        String invoicePath = PhoneUtils.saveBitmapToFile(appPreference.getTempInvoicePath(), NetworkConstant.IMAGE_TYPE_INVOICE);
                        if (!invoicePath.isEmpty())
                        {
                            Image image = new Image();
                            image.setLocalPath(invoicePath);
                            item.getInvoices().add(image);
                        }
                        else
                        {
                            ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
                        }

                        refreshInvoiceView();
                    }
                    catch (Exception e)
                    {
                        ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
                        e.printStackTrace();
                    }
                    break;
                }
                case Constant.ACTIVITY_PICK_VENDOR:
                {
                    String vendor = data.getStringExtra("vendor");
                    item.setVendor(vendor);
                    item.setLatitude(data.getDoubleExtra("latitude", -1));
                    item.setLongitude(data.getDoubleExtra("longitude", -1));
                    vendorTextView.setText(vendor);
                    Category category = new Category();
                    category.setName(getString(R.string.transport));
                    int categoryIndex = categoryList.indexOf(category);
                    if ((vendor.equals(getString(R.string.vendor_flight)) || vendor.equals(getString(R.string.vendor_train))
                            || vendor.equals(getString(R.string.vendor_taxi))) && categoryIndex > 0)
                    {
                        category = categoryList.get(categoryIndex);
                        item.setCategory(category);
                        refreshCategoryView();
                    }
                    break;
                }
                case Constant.ACTIVITY_PICK_LOCATION:
                {
                    item.setLocation(data.getStringExtra("location"));
                    String location = item.getLocation().isEmpty() ? getString(R.string.no_location) : item.getLocation();
                    locationTextView.setText(location);
                    break;
                }
                case Constant.ACTIVITY_PICK_CATEGORY:
                {
                    Category category = (Category) data.getSerializableExtra("category");
                    item.setCategory(category);
                    refreshCategoryView();
                    break;
                }
                case Constant.ACTIVITY_PICK_TAG:
                {
                    List<Tag> tags = (List<Tag>) data.getSerializableExtra("tags");
                    item.setTags(tags);
                    refreshTagView();
                    break;
                }
                case Constant.ACTIVITY_PICK_MEMBER:
                {
                    List<User> users = (List<User>) data.getSerializableExtra("users");
                    item.setRelevantUsers(users);
                    refreshMemberView();
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
                goBack();
            }
        });

        TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_ITEM_SAVE");

                if (appPreference.hasProxyEditPermission())
                {
                    try
                    {
                        hideSoftKeyboard();

                        item.setAmount(Utils.stringToDouble(amountEditText.getText().toString()));
                        item.setConsumer(appPreference.getCurrentUser());
                        item.setNote(noteEditText.getText().toString());
                        item.setLocalUpdatedDate(Utils.getCurrentTime());

                        if(!fromApproveReport)
                        {
                            item.setAmount(Utils.stringToDouble(amountEditText.getText().toString()));
                            item.setConsumer(appPreference.getCurrentUser());
                            item.setNote(noteEditText.getText().toString());
                            item.setLocalUpdatedDate(Utils.getCurrentTime());

                            if (newItem)
                            {
                                item.setCreatedDate(item.getLocalUpdatedDate());
                            }

                            if (fromReim && !fromPickItems && item.getType() != Item.TYPE_REIM && !item.isAaApproved())
                            {
                                Builder builder = new Builder(EditItemActivity.this);
                                builder.setTitle(R.string.option);
                                builder.setMessage(R.string.prompt_save_approve_ahead_item);
                                builder.setPositiveButton(R.string.only_save, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_ITEM_PROVEAHEAD_SAVE");
                                        saveItem();
                                    }
                                });
                                builder.setNeutralButton(R.string.send_to_approve, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_ITEM_PROVEAHEAD_SUBMIT");

                                        Report report;
                                        if (item.getBelongReport() == null)
                                        {
                                            int title = item.getType() == Item.TYPE_BUDGET ? R.string.report_budget : R.string.report_borrowing;
                                            report = new Report();
                                            report.setTitle(getString(title));
                                            report.setSender(appPreference.getCurrentUser());
                                            report.setCreatedDate(Utils.getCurrentTime());
                                            report.setLocalUpdatedDate(Utils.getCurrentTime());
                                            report.setType(item.getType());
                                            report.setManagerList(appPreference.getCurrentUser().buildBaseManagerList());
                                            report.setLocalID(dbManager.insertReport(report));

                                            item.setBelongReport(report);
                                        }
                                        else
                                        {
                                            report = item.getBelongReport();
                                        }

                                        dbManager.syncItem(item);
                                        ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);

                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("report", report);
                                        Intent intent = new Intent(EditItemActivity.this, EditReportActivity.class);
                                        intent.putExtras(bundle);
                                        ViewUtils.goForwardAndFinish(EditItemActivity.this, intent);
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, null);
                                builder.create().show();
                            }
                            else if (fromPickItems)
                            {
                                item.setLocalID(dbManager.insertItem(item));
                                ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
                                Intent intent = new Intent();
                                intent.putExtra("itemID", item.getLocalID());
                                intent.putExtra("type", item.getType());
                                ViewUtils.goBackWithResult(EditItemActivity.this, intent);
                            }
                            else
                            {
                                saveItem();
                            }
                        }
                        else
                        {
                            item.setAmount(Utils.stringToDouble(amountEditText.getText().toString()));
                            item.setNote(noteEditText.getText().toString());

                            sendChangeAmountRequest(item);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        ViewUtils.showToast(EditItemActivity.this, R.string.error_number_wrong_format);
                        ViewUtils.requestFocus(EditItemActivity.this, amountEditText);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    ViewUtils.showToast(EditItemActivity.this, R.string.error_modify_item_no_permission);
                }
            }
        });

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });

        initStatusView();
        initInvoiceView();
        initCategoryView();
        initVendorView();
        initLocationView();
        initTimeView();
        initCurrencyView();
        initTypeView();
        initTagView();
        initMemberView();
        initNoteView();
    }

    private void initStatusView()
    {
        TextView actualCostTextView = (TextView) findViewById(R.id.actualCostTextView);
        TextView budgetTextView = (TextView) findViewById(R.id.budgetTextView);
        TextView approvedTextView = (TextView) findViewById(R.id.approvedTextView);
        amountWarningImageView = (ImageView) findViewById(R.id.amountWarningImageView);

        TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText(item.getStatusString());
        statusTextView.setBackgroundResource(item.getStatusBackground());

        symbolTextView = (TextView) findViewById(R.id.symbolTextView);
        symbolTextView.setText(item.getCurrency().getSymbol());

        amountEditText = (EditText) findViewById(R.id.amountEditText);
        amountEditText.setTypeface(ReimApplication.TypeFaceAleoLight);
        amountEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        amountEditText.addTextChangedListener(new TextWatcher()
        {
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().contains("."))
                {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2)
                    {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                        amountEditText.setText(s);
                        amountEditText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().equals("."))
                {
                    s = "0" + s;
                    amountEditText.setText(s);
                    amountEditText.setSelection(2);
                }

                if (s.toString().startsWith("0") && s.toString().trim().length() > 1)
                {
                    if (!s.toString().substring(1, 2).equals("."))
                    {
                        amountEditText.setText(s.subSequence(1, 2));
                        amountEditText.setSelection(1);
                    }
                }
            }

            public void afterTextChanged(Editable s)
            {
                int visibility = (s.toString().isEmpty() || Double.valueOf(s.toString()) == 0) && fromEditReport ? View.VISIBLE : View.GONE;
                amountWarningImageView.setVisibility(visibility);
            }
        });
        if (item.getAmount() == 0)
        {
            ViewUtils.requestFocus(this, amountEditText);
            if (fromEditReport)
            {
                amountWarningImageView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            amountEditText.setText(Utils.formatDouble(item.getAmount()));
        }

        if (item.isAaApproved())
        {
            int title = item.getType() == Item.TYPE_BUDGET ? R.string.budget : R.string.borrowing;
            budgetTextView.setText(getString(title) + " " + Utils.formatDouble(item.getAaAmount()));
        }
        else
        {
            actualCostTextView.setVisibility(View.GONE);
            budgetTextView.setVisibility(View.GONE);
            approvedTextView.setVisibility(View.GONE);
        }
    }

    private void initInvoiceView()
    {
        // init invoice
        invoiceLayout = (LinearLayout) findViewById(R.id.invoiceLayout);

        addInvoiceImageView = (ImageView) findViewById(R.id.addInvoiceImageView);
        addInvoiceImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                if (item.getInvoices().size() == Item.MAX_INVOICE_COUNT)
                {
                    ViewUtils.showToast(EditItemActivity.this, R.string.prompt_max_image_count);
                }
                else
                {
                    showPictureWindow();
                }
            }
        });

        removeList = new ArrayList<>();

        refreshInvoiceView();

        // init picture window
        View pictureView = View.inflate(this, R.layout.window_picture, null);

        Button cameraButton = (Button) pictureView.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                picturePopupWindow.dismiss();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, appPreference.getTempInvoiceUri());
                startActivityForResult(intent, Constant.ACTIVITY_TAKE_PHOTO);
            }
        });

        Button galleryButton = (Button) pictureView.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                picturePopupWindow.dismiss();

                Intent intent = new Intent(EditItemActivity.this, GalleryActivity.class);
                intent.putExtra("maxCount", Item.MAX_INVOICE_COUNT - item.getInvoices().size());
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

        if (item.getInvoices() != null && !item.getInvoices().isEmpty() && !PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_download_invoice);
        }
        else if (item.getInvoices() != null && !item.getInvoices().isEmpty())
        {
            for (Image image : item.getInvoices())
            {
                if (image.isNotDownloaded() && PhoneUtils.isNetworkConnected())
                {
                    sendDownloadInvoiceRequest(image);
                }
            }
        }
    }

    private void initCategoryView()
    {
        RelativeLayout categoryLayout = (RelativeLayout) findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!item.isAaApproved())
                {
                    if (newItem)
                    {
                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_CATEGORY");
                    }
                    else
                    {
                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_CATEGORY");
                    }

                    hideSoftKeyboard();
                    Intent intent = new Intent(EditItemActivity.this, PickCategoryActivity.class);
                    intent.putExtra("category", item.getCategory());
                    ViewUtils.goForwardForResult(EditItemActivity.this, intent, Constant.ACTIVITY_PICK_CATEGORY);
                }
            }
        });

        categoryImageView = (ImageView) findViewById(R.id.categoryImageView);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        categoryWarningImageView = (ImageView) findViewById(R.id.categoryWarningImageView);

        refreshCategoryView();
    }

    private void initVendorView()
    {
        vendorTextView = (TextView) findViewById(R.id.vendorTextView);
        vendorTextView.setText(item.getVendor());
        vendorTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                if (newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MERCHANT");
                }
                else
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MERCHANT");
                }

                Intent intent = new Intent(EditItemActivity.this, PickVendorActivity.class);
                intent.putExtra("location", item.getLocation());
                if (currentLocation != null)
                {
                    intent.putExtra("latitude", currentLocation.getLatitude());
                    intent.putExtra("longitude", currentLocation.getLongitude());
                }
                ViewUtils.goForwardForResult(EditItemActivity.this, intent, Constant.ACTIVITY_PICK_VENDOR);
            }
        });
    }

    private void initLocationView()
    {
        String cityName = item.getLocation().isEmpty() ? getString(R.string.no_location) : item.getLocation();
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationTextView.setText(cityName);

        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        locationLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                Intent intent = new Intent(EditItemActivity.this, PickLocationActivity.class);
                intent.putExtra("currentCity", currentCity);
                ViewUtils.goForwardForResult(EditItemActivity.this, intent, Constant.ACTIVITY_PICK_LOCATION);
            }
        });
    }

    private void initTimeView()
    {
        // init time
        int time = item.getConsumedDate() > 0 ? item.getConsumedDate() : Utils.getCurrentTime();
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        timeTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                showTimeWindow();
            }
        });
        timeTextView.setText(Utils.secondToStringUpToMinute(time));

        // init time window
        View timeView = View.inflate(this, R.layout.window_reim_time, null);

        Button confirmButton = (Button) timeView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                timePopupWindow.dismiss();

                GregorianCalendar greCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(),
                                                                 datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                item.setConsumedDate((int) (greCal.getTimeInMillis() / 1000));
                timeTextView.setText(Utils.secondToStringUpToMinute(item.getConsumedDate()));
            }
        });

        datePicker = (DatePicker) timeView.findViewById(R.id.datePicker);

        timePicker = (TimePicker) timeView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        resizePicker();

        timePopupWindow = ViewUtils.buildBottomPopupWindow(this, timeView);
    }

    private void initCurrencyView()
    {
        // init currency
        currencyTextView = (TextView) findViewById(R.id.currencyTextView);
        currencyTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                showCurrencyWindow();
            }
        });
        currencyTextView.setText(item.getCurrency().getName());

        // init currency window
        final View currencyView = View.inflate(this, R.layout.window_reim_currency, null);

        final WheelView currencyWheelView = (WheelView) currencyView.findViewById(R.id.currencyWheelView);
        currencyWheelView.setVisibleItems(7);
        currencyWheelView.setViewAdapter(new ArrayWheelAdapter<>(this, Currency.listToArray(currencyList)));
        if (item.getCurrency() != null && !item.getCurrency().getName().isEmpty())
        {
            int index = currencyList.indexOf(item.getCurrency());
            if (index >= 0)
            {
                currencyWheelView.setCurrentItem(index);
            }
        }

        Button confirmButton = (Button) currencyView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Currency currency = currencyList.get(currencyWheelView.getCurrentItem());
                item.setCurrency(currency);
                currencyTextView.setText(currency.getName());
                symbolTextView.setText(currency.getSymbol());
                currencyPopupWindow.dismiss();
            }
        });

        currencyPopupWindow = ViewUtils.buildBottomPopupWindow(this, currencyView);
    }

    private void initTypeView()
    {
        // init type
        typeTextView = (TextView) findViewById(R.id.typeTextView);
        refreshTypeView();

        LinearLayout typeLayout = (LinearLayout) findViewById(R.id.typeLayout);
        typeLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (fromReim && !item.isAaApproved() || fromPickItems)
                {
                    hideSoftKeyboard();
                    showTypeWindow();
                }
            }
        });

        // init type window
        View typeView = View.inflate(this, R.layout.window_reim_type, null);

        consumedImageView = (ImageView) typeView.findViewById(R.id.consumedImageView);
        budgetImageView = (ImageView) typeView.findViewById(R.id.budgetImageView);
        borrowingImageView = (ImageView) typeView.findViewById(R.id.borrowingImageView);
        needReimLayout = (RelativeLayout) typeView.findViewById(R.id.needReimLayout);

        ImageView disclosureImageView = (ImageView) typeView.findViewById(R.id.disclosureImageView);
        disclosureImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (needReimLayout.getVisibility() == View.VISIBLE)
                {
                    needReimLayout.setVisibility(View.GONE);
                }
                else
                {
                    needReimLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        LinearLayout consumedLayout = (LinearLayout) typeView.findViewById(R.id.consumedLayout);
        consumedLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                consumedImageView.setVisibility(View.VISIBLE);
                budgetImageView.setVisibility(View.INVISIBLE);
                borrowingImageView.setVisibility(View.INVISIBLE);
                needReimLayout.setVisibility(View.VISIBLE);
            }
        });

        LinearLayout budgetLayout = (LinearLayout) typeView.findViewById(R.id.budgetLayout);
        budgetLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_PROVEAHEAD");
                }
                else
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_PROVEAHEAD");
                }

                consumedImageView.setVisibility(View.INVISIBLE);
                budgetImageView.setVisibility(View.VISIBLE);
                borrowingImageView.setVisibility(View.INVISIBLE);
                needReimLayout.setVisibility(View.GONE);
            }
        });

        LinearLayout borrowingLayout = (LinearLayout) typeView.findViewById(R.id.borrowingLayout);
        borrowingLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                consumedImageView.setVisibility(View.INVISIBLE);
                budgetImageView.setVisibility(View.INVISIBLE);
                borrowingImageView.setVisibility(View.VISIBLE);
                needReimLayout.setVisibility(View.GONE);
            }
        });

        needReimToggleButton = (ToggleButton) typeView.findViewById(R.id.needReimToggleButton);
        needReimToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked && newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_REIMBURSE");
                }
                if (isChecked && !newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_REIMBURSE");
                }
            }
        });

        TextView confirmTextView = (TextView) typeView.findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                typePopupWindow.dismiss();

                if (consumedImageView.getVisibility() == View.VISIBLE)
                {
                    item.setType(Item.TYPE_REIM);
                    item.setNeedReimbursed(needReimToggleButton.isChecked());
                }
                else if (budgetImageView.getVisibility() == View.VISIBLE)
                {
                    item.setType(Item.TYPE_BUDGET);
                }
                else
                {
                    item.setType(Item.TYPE_BORROWING);
                }

                refreshTypeView();
            }
        });

        typePopupWindow = ViewUtils.buildBottomPopupWindow(this, typeView);
    }

    private void initTagView()
    {
        tagContainerLayout = (LinearLayout) findViewById(R.id.tagContainerLayout);
        if (tagList == null || tagList.isEmpty())
        {
            tagContainerLayout.setVisibility(View.GONE);
        }
        else
        {
            tagContainerLayout.setVisibility(View.VISIBLE);
            tagLayout = (LinearLayout) findViewById(R.id.tagLayout);

            ImageView addTagImageView = (ImageView) findViewById(R.id.addTagImageView);
            addTagImageView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    if (newItem)
                    {
                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TAG");
                    }
                    else
                    {
                        MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TAG");
                    }

                    hideSoftKeyboard();
                    Intent intent = new Intent(EditItemActivity.this, PickTagActivity.class);
                    intent.putExtra("tags", (Serializable) item.getTags());
                    ViewUtils.goForwardForResult(EditItemActivity.this, intent, Constant.ACTIVITY_PICK_TAG);
                }
            });

            refreshTagView();
        }
    }

    private void initMemberView()
    {
        memberLayout = (LinearLayout) findViewById(R.id.memberLayout);

        ImageView addMemberImageView = (ImageView) findViewById(R.id.addMemberImageView);
        addMemberImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_MEMBER");
                }
                else
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_MEMBER");
                }

                hideSoftKeyboard();
                Intent intent = new Intent(EditItemActivity.this, PickMemberActivity.class);
                intent.putExtra("users", (Serializable) item.getRelevantUsers());
                ViewUtils.goForwardForResult(EditItemActivity.this, intent, Constant.ACTIVITY_PICK_MEMBER);
            }
        });

        refreshMemberView();

        if (item.getRelevantUsers() != null)
        {
            for (User user : item.getRelevantUsers())
            {
                if (user != null && user.hasUndownloadedAvatar())
                {
                    sendDownloadAvatarRequest(user);
                }
            }
        }
    }

    private void initNoteView()
    {
        noteEditText = (EditText) findViewById(R.id.noteEditText);
        noteEditText.setText(item.getNote());
        noteEditText.setFilters(new InputFilter[]{new TextLengthFilter(1000)});
        noteEditText.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            public void onFocusChange(View v, boolean hasFocus)
            {
                Spannable spanText = ((EditText) v).getText();
                Selection.setSelection(spanText, spanText.length());

                if (hasFocus && newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_NOTE");
                }
                if (hasFocus && !newItem)
                {
                    MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_NOTE");
                }
            }
        });
    }

    private void refreshInvoiceView()
    {
        invoiceLayout.removeAllViews();
        removeList.clear();

        int layoutMaxLength = ViewUtils.getPhoneWindowWidth(this) - ViewUtils.dpToPixel(126);
        int sideLength = ViewUtils.dpToPixel(40);
        int verticalInterval = ViewUtils.dpToPixel(5);
        int horizontalInterval = ViewUtils.dpToPixel(5);
        int maxCount = (layoutMaxLength + horizontalInterval) / (sideLength + horizontalInterval);
        horizontalInterval = (layoutMaxLength - sideLength * maxCount) / (maxCount - 1);

        LinearLayout layout = new LinearLayout(this);
        int invoiceCount = item.getInvoices() != null ? item.getInvoices().size() : 0;
        for (int i = 0; i < invoiceCount; i++)
        {
            if (i > Item.MAX_INVOICE_COUNT)
            {
                break;
            }

            if (i % maxCount == 0)
            {
                layout = new LinearLayout(this);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if (i != 0)
                {
                    params.topMargin = verticalInterval;
                }
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                invoiceLayout.addView(layout);
            }

            final int index = i;
            final Bitmap bitmap = item.getInvoices().get(index).getBitmap();

            View view = View.inflate(this, R.layout.grid_invoice, null);

            final ImageView removeImageView = (ImageView) view.findViewById(R.id.removeImageView);
            removeImageView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    item.getInvoices().remove(index);
                    refreshInvoiceView();
                }
            });
            removeList.add(removeImageView);

            ImageView invoiceImageView = (ImageView) view.findViewById(R.id.invoiceImageView);
            invoiceImageView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    if (bitmap != null && removeImageView.getVisibility() != View.VISIBLE)
                    {
                        hideSoftKeyboard();
                        removeImageView.setVisibility(View.INVISIBLE);

                        ArrayList<String> pathList = new ArrayList<>();
                        for (Image image : item.getInvoices())
                        {
                            if (!image.getLocalPath().isEmpty())
                            {
                                pathList.add(image.getLocalPath());
                            }
                        }

                        int pageIndex = pathList.indexOf(item.getInvoices().get(index).getLocalPath());

                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("imagePath", pathList);
                        bundle.putInt("index", pageIndex);

                        Intent intent = new Intent(EditItemActivity.this, MultipleImageActivity.class);
                        intent.putExtras(bundle);
                        ViewUtils.goForward(EditItemActivity.this, intent);
                    }
                }
            });
            invoiceImageView.setOnLongClickListener(new View.OnLongClickListener()
            {
                public boolean onLongClick(View v)
                {
                    for (ImageView removeImageView : removeList)
                    {
                        removeImageView.setVisibility(View.VISIBLE);
                    }
                    removeImageViewShown = true;
                    return false;
                }
            });

            if (bitmap == null)
            {
                invoiceImageView.setImageResource(R.drawable.default_invoice);
            }
            else
            {
                invoiceImageView.setImageBitmap(bitmap);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
            if ((i + 1) % maxCount != 0)
            {
                params.rightMargin = horizontalInterval;
            }
            layout.addView(view, params);
        }

        int visibility = invoiceCount < Item.MAX_INVOICE_COUNT ? View.VISIBLE : View.INVISIBLE;
        addInvoiceImageView.setVisibility(visibility);
    }

    private void refreshCategoryView()
    {
        if (item.getCategory() != null)
        {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryImageView.setVisibility(View.VISIBLE);

            categoryTextView.setText(item.getCategory().getName());
            categoryImageView.setImageResource(R.drawable.default_icon);

            ViewUtils.setImageViewBitmap(item.getCategory(), categoryImageView);

            if (item.getCategory().hasUndownloadedIcon() && PhoneUtils.isNetworkConnected())
            {
                sendDownloadCategoryIconRequest(item.getCategory());
            }
        }
        else
        {
            categoryImageView.setVisibility(View.INVISIBLE);
            categoryTextView.setVisibility(View.INVISIBLE);
            if (fromEditReport)
            {
                categoryWarningImageView.setVisibility(View.GONE);
            }
        }
    }

    private void refreshTypeView()
    {
        String temp = getString(item.getTypeString());
        if (item.getType() == Item.TYPE_REIM && !item.needReimbursed())
        {
            temp += getString(R.string.does_not_need_reimburse);
        }
        typeTextView.setText(temp);
    }

    private void refreshTagView()
    {
        if (tagContainerLayout.getVisibility() == View.VISIBLE)
        {
            tagLayout.removeAllViews();

            int layoutMaxWidth = ViewUtils.getPhoneWindowWidth(this) - ViewUtils.dpToPixel(126);
            int verticalInterval = ViewUtils.dpToPixel(17);
            int horizontalInterval = ViewUtils.dpToPixel(10);
            int padding = ViewUtils.dpToPixel(24);
            int textSize = ViewUtils.dpToPixel(16);

            int space = 0;
            LinearLayout layout = new LinearLayout(this);
            int tagCount = item.getTags() != null ? item.getTags().size() : 0;
            for (int i = 0; i < tagCount; i++)
            {
                String name = item.getTags().get(i).getName();

                View view = View.inflate(this, R.layout.grid_item_tag, null);

                TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
                nameTextView.setText(name);

                Paint textPaint = new Paint();
                textPaint.setTextSize(textSize);
                Rect textRect = new Rect();
                textPaint.getTextBounds(name, 0, name.length(), textRect);
                int width = textRect.width() + padding;

                if (space - width - horizontalInterval <= 0)
                {
                    layout = new LinearLayout(this);
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    params.topMargin = verticalInterval;
                    layout.setLayoutParams(params);
                    layout.setOrientation(LinearLayout.HORIZONTAL);

                    tagLayout.addView(layout);

                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layout.addView(view, params);
                    space = layoutMaxWidth - width;
                }
                else
                {
                    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.leftMargin = horizontalInterval;
                    layout.addView(view, params);
                    space -= width + horizontalInterval;
                }
            }
        }
    }

    private void refreshMemberView()
    {
        memberLayout.removeAllViews();

        int layoutMaxWidth = ViewUtils.getPhoneWindowWidth(this) - ViewUtils.dpToPixel(126);
        int width = ViewUtils.dpToPixel(50);
        int verticalInterval = ViewUtils.dpToPixel(18);
        int horizontalInterval = ViewUtils.dpToPixel(18);
        int maxCount = (layoutMaxWidth + horizontalInterval) / (width + horizontalInterval);
        horizontalInterval = (layoutMaxWidth - width * maxCount) / (maxCount - 1);

        LinearLayout layout = new LinearLayout(this);
        int memberCount = item.getRelevantUsers() != null ? item.getRelevantUsers().size() : 0;
        for (int i = 0; i < memberCount; i++)
        {
            if (i % maxCount == 0)
            {
                layout = new LinearLayout(this);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.topMargin = verticalInterval;
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                memberLayout.addView(layout);
            }

            User user = item.getRelevantUsers().get(i);

            if (user != null)
            {
                View memberView = View.inflate(this, R.layout.grid_member, null);

                CircleImageView avatarImageView = (CircleImageView) memberView.findViewById(R.id.avatarImageView);
                ViewUtils.setImageViewBitmap(user, avatarImageView);

                TextView nameTextView = (TextView) memberView.findViewById(R.id.nameTextView);
                nameTextView.setText(user.getNickname());

                LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
                params.rightMargin = horizontalInterval;

                layout.addView(memberView, params);
            }
        }
    }

    private void showPictureWindow()
    {
        picturePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        picturePopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void showTimeWindow()
    {
        if (newItem)
        {
            MobclickAgent.onEvent(EditItemActivity.this, "UMENG_NEW_TIME");
        }
        else
        {
            MobclickAgent.onEvent(EditItemActivity.this, "UMENG_EDIT_TIME");
        }

        Calendar calendar = Calendar.getInstance();
        if (item.getConsumedDate() <= 0)
        {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        else
        {
            calendar.setTimeInMillis((long) item.getConsumedDate() * 1000);
        }

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        timePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        timePopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void showCurrencyWindow()
    {
        currencyPopupWindow.showAtLocation(findViewById(R.id.baseLayout), Gravity.BOTTOM, 0, 0);
        currencyPopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private void showTypeWindow()
    {
        if (item.getType() == Item.TYPE_REIM)
        {
            consumedImageView.setVisibility(View.VISIBLE);
            budgetImageView.setVisibility(View.INVISIBLE);
            borrowingImageView.setVisibility(View.INVISIBLE);
            needReimLayout.setVisibility(View.VISIBLE);
        }
        else if (item.getType() == Item.TYPE_BUDGET)
        {
            consumedImageView.setVisibility(View.INVISIBLE);
            budgetImageView.setVisibility(View.VISIBLE);
            borrowingImageView.setVisibility(View.INVISIBLE);
            needReimLayout.setVisibility(View.GONE);
        }
        else
        {
            consumedImageView.setVisibility(View.INVISIBLE);
            budgetImageView.setVisibility(View.INVISIBLE);
            borrowingImageView.setVisibility(View.VISIBLE);
            needReimLayout.setVisibility(View.GONE);
        }
        needReimToggleButton.setChecked(item.needReimbursed());

        typePopupWindow.showAtLocation(findViewById(R.id.containerLayout), Gravity.BOTTOM, 0, 0);
        typePopupWindow.update();

        ViewUtils.dimBackground(this);
    }

    private List<NumberPicker> findNumberPickers(DatePicker datePicker)
    {
        List<NumberPicker> pickerList = new ArrayList<>();
        Field[] fields = DatePicker.class.getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            if (field.getType().getSimpleName().equals("NumberPicker"))
            {
                try
                {
                    pickerList.add((NumberPicker) field.get(datePicker));
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return pickerList;
    }

    private List<NumberPicker> findNumberPickers(TimePicker timePicker)
    {
        List<NumberPicker> pickerList = new ArrayList<>();
        Field[] fields = TimePicker.class.getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            if (field.getType().getSimpleName().equals("NumberPicker"))
            {
                try
                {
                    pickerList.add((NumberPicker) field.get(timePicker));
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return pickerList;
    }

    private void resizePicker()
    {
        int yearWidth = ViewUtils.dpToPixel(60);
        int width = ViewUtils.dpToPixel(40);
        int dateMargin = ViewUtils.dpToPixel(15);
        int timeMargin = ViewUtils.dpToPixel(5);

        List<NumberPicker> pickerList = findNumberPickers(datePicker);

        NumberPicker yearPicker = pickerList.get(2);
        LayoutParams params = new LayoutParams(yearWidth, LayoutParams.WRAP_CONTENT);
        params.rightMargin = dateMargin;
        yearPicker.setLayoutParams(params);

        NumberPicker monthPicker = pickerList.get(1);
        params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        params.rightMargin = dateMargin;
        monthPicker.setLayoutParams(params);

        NumberPicker dayPicker = pickerList.get(0);
        params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        dayPicker.setLayoutParams(params);

        pickerList = findNumberPickers(timePicker);

        NumberPicker hourPicker = pickerList.get(1);
        params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        params.rightMargin = timeMargin;
        hourPicker.setLayoutParams(params);

        NumberPicker minutePicker = pickerList.get(2);
        params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        params.leftMargin = timeMargin;
        minutePicker.setLayoutParams(params);
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(noteEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        for (Image newImage : item.getInvoices())
        {
            boolean imageExists = false;
            for (Image oldImage : originInvoiceList)
            {
                if (newImage.getLocalPath().equals(oldImage.getLocalPath()))
                {
                    imageExists = true;
                    break;
                }
            }
            if (!imageExists)
            {
                newImage.deleteFile();
            }
        }
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
        locationClient = new LocationClient(getApplicationContext());

        currencyList.addAll(dbManager.getCurrencyList());
        categoryList.addAll(dbManager.getUserCategories(appPreference.getCurrentUserID()));
        tagList.addAll(dbManager.getGroupTags(appPreference.getCurrentGroupID()));

        Intent intent = getIntent();
        fromReim = intent.getBooleanExtra("fromReim", false);
        fromEditReport = intent.getBooleanExtra("fromEditReport", false);
        fromPickItems = intent.getBooleanExtra("fromPickItems", false);
        fromApproveReport = intent.getBooleanExtra("fromApproveReport", false);

        int itemServerID = intent.getIntExtra("itemServerID", -1);
        int itemLocalID = intent.getIntExtra("itemLocalID", -1);
        if (itemLocalID == -1 && itemServerID == -1)
        {
            newItem = true;
            MobclickAgent.onEvent(this, "UMENG_NEW_ITEM");
            item = new Item();
            if (!categoryList.isEmpty())
            {
                item.setCategory(categoryList.get(0));
            }
            item.setConsumedDate(Utils.getCurrentTime());
            item.setInvoices(new ArrayList<Image>());
            if (fromPickItems)
            {
                item.setType(intent.getIntExtra("type", 0));
            }
            List<User> relevantUsers = new ArrayList<>();
            relevantUsers.add(appPreference.getCurrentUser());
            item.setRelevantUsers(relevantUsers);

            if (intent.getBooleanExtra("fromDidi", false))
            {
                DidiExpense expense = (DidiExpense) intent.getSerializableExtra("expense");
                item.setAmount(expense.getAmount());
                item.setConsumedDate(expense.getTimeStamp());
                item.setVendor(ViewUtils.getString(R.string.vendor_taxi));
                item.setLocation(expense.getCity());
                item.setDidiID(expense.getId());
                item.setNote(String.format(getString(R.string.from_to), expense.getStart(), expense.getDestination()));

                String transport = getString(R.string.transport);
                for (Category category : categoryList)
                {
                    if (category.getName().equals(transport))
                    {
                        item.setCategory(category);
                        break;
                    }
                }
            }
        }
        else if(itemLocalID != -1 && itemServerID == -1)
        {
            newItem = false;
            MobclickAgent.onEvent(this, "UMENG_EDIT_ITEM");
            item = dbManager.getItemByLocalID(itemLocalID);
            if (item == null)
            {
                ViewUtils.showToast(this, R.string.error_item_not_found);
                goBack();
            }
            else
            {
                originInvoiceList.addAll(item.getInvoices());
            }
        }
        else
        {
            newItem = false;
            item = dbManager.getOthersItem(itemServerID);
            if (item == null)
            {
                ViewUtils.showToast(this, R.string.error_item_not_found);
                goBack();
            }
            else
            {
                originInvoiceList.addAll(item.getInvoices());
            }
        }
    }

    private void saveItem()
    {
        Item localItem = dbManager.getItemByLocalID(item.getLocalID());
        if (localItem != null)
        {
            item.setServerID(localItem.getServerID());
        }
        dbManager.syncItem(item);
        ReimApplication.setTabIndex(Constant.TAB_REIM);
        ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
        ViewUtils.goBack(this);
    }

    // Network
    private void sendDownloadInvoiceRequest(final Image image)
    {
        DownloadImageRequest request = new DownloadImageRequest(image.getServerPath());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    final String invoicePath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_INVOICE);
                    if (!invoicePath.isEmpty())
                    {
                        image.setLocalPath(invoicePath);
                        dbManager.updateImageLocalPath(image);

                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                int index = item.getInvoices().indexOf(image);
                                if (index != -1)
                                {
                                    item.getInvoices().set(index, image);
                                }
                                refreshInvoiceView();
                            }
                        });
                    }
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_invoice);
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
                            ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_download_invoice);
                        }
                    });
                }
            }
        });
    }

    private void sendDownloadCategoryIconRequest(final Category category)
    {
        DownloadImageRequest request = new DownloadImageRequest(category.getIconID());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                DownloadImageResponse response = new DownloadImageResponse(httpResponse);
                if (response.getBitmap() != null)
                {
                    PhoneUtils.saveIconToFile(response.getBitmap(), category.getIconID());
                    category.setLocalUpdatedDate(Utils.getCurrentTime());
                    category.setServerUpdatedDate(category.getLocalUpdatedDate());
                    dbManager.updateCategory(category);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            item.setCategory(category);
                            ViewUtils.setImageViewBitmap(item.getCategory(), categoryImageView);
                        }
                    });
                }
            }
        });
    }

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
                    String avatarPath = PhoneUtils.saveOriginalBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
                    user.setAvatarLocalPath(avatarPath);
                    user.setLocalUpdatedDate(Utils.getCurrentTime());
                    user.setServerUpdatedDate(user.getLocalUpdatedDate());
                    dbManager.updateUser(user);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            int index = item.getRelevantUsers().indexOf(user);
                            item.getRelevantUsers().set(index, user);
                            refreshMemberView();
                        }
                    });
                }
            }
        });
    }

    private void sendChangeAmountRequest(final Item item)
    {
        ReimProgressDialog.show();
        ChangeAmountRequest request = new ChangeAmountRequest(item);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ChangeAmountResponse response = new ChangeAmountResponse(httpResponse);
                if (response.getStatus())
                {
                    dbManager.updateOthersItem(item);

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EditItemActivity.this, R.string.succeed_in_saving_item);
                            ViewUtils.goBack(EditItemActivity.this);
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
                            ViewUtils.showToast(EditItemActivity.this, R.string.failed_to_save_item, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }


    private void getLocation()
    {
        if (PhoneUtils.isLocalisationEnabled() || PhoneUtils.isNetworkConnected())
        {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationMode.Hight_Accuracy);
            option.setScanSpan(500);
            option.setOpenGps(false);
            option.setIsNeedAddress(true);
            option.setNeedDeviceDirect(false);
            locationClient.setLocOption(option);
            locationClient.start();
        }
    }

    public class ReimLocationListener implements BDLocationListener
    {
        public void onReceiveLocation(BDLocation location)
        {
            if (location != null)
            {
                currentLocation = location;
                currentCity = currentLocation.getCity() == null ? "" : currentLocation.getCity();
                int index = currentCity.indexOf("市");
                if (index > 0)
                {
                    currentCity = currentCity.substring(0, index);
                }

                if (!currentCity.isEmpty() && locationTextView.getText().toString().equals(getString(R.string.no_location)))
                {
                    item.setLocation(currentCity);
                    locationTextView.setText(currentCity);
                }
                locationClient.stop();
            }
        }
    }
}