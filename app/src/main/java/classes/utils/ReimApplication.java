package classes.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.mechat.mechatlibrary.MCClient;
import com.mechat.mechatlibrary.callback.OnInitCallback;
import com.rushucloud.reim.R;
import com.rushucloud.reim.main.MainActivity;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.model.Currency;
import classes.widget.ReimProgressDialog;

public class ReimApplication extends Application
{
    public static Typeface TypeFaceYaHei;
    public static Typeface TypeFaceAleoLight;

    public static int GUIDE_VERSION = 1;

    private static Context context;

    private static int tabIndex = Constant.TAB_REIM;
    private static int reportTabIndex = Constant.TAB_REPORT_MINE;
    private static int statTabIndex = Constant.TAB_STATISTICS_MINE;
    private static List<Integer> mineUnreadList = new ArrayList<>();
    private static List<Integer> othersUnreadList = new ArrayList<>();
    private static int unreadMessagesCount;
    private static boolean hasUnreadMessages;

    public void onCreate()
    {
        super.onCreate();

        initPushService();
        initMeChat();
        initData();
        WeChatUtils.regToWX();
        MobclickAgent.openActivityDurationTrack(false);
        createDirectories();
        saveCategoryIcon();

        LogUtils.println("**************** Application Started *****************");
        LogUtils.println(AVInstallation.getCurrentInstallation().getInstallationId());
    }

    public static int getTabIndex()
    {
        return tabIndex;
    }
    public static void setTabIndex(int tabIndex)
    {
        ReimApplication.tabIndex = tabIndex;
    }

    public static int getReportTabIndex()
    {
        return reportTabIndex;
    }
    public static void setReportTabIndex(int reportTabIndex)
    {
        ReimApplication.reportTabIndex = reportTabIndex;
    }

    public static int getStatTabIndex()
    {
        return statTabIndex;
    }
    public static void setStatTabIndex(int statTabIndex)
    {
        ReimApplication.statTabIndex = statTabIndex;
    }

    public static List<Integer> getMineUnreadList()
    {
        return mineUnreadList;
    }
    public static void setMineUnreadList(List<Integer> mineUnreadList)
    {
        ReimApplication.mineUnreadList = mineUnreadList;
    }

    public static List<Integer> getOthersUnreadList()
    {
        return othersUnreadList;
    }
    public static void setOthersUnreadList(List<Integer> othersUnreadList)
    {
        ReimApplication.othersUnreadList = othersUnreadList;
    }

    public static int getUnreadMessagesCount()
    {
        return unreadMessagesCount;
    }
    public static void setUnreadMessagesCount(int unreadMessagesCount)
    {
        ReimApplication.unreadMessagesCount = unreadMessagesCount;
    }

    public static boolean hasUnreadMessages()
    {
        return hasUnreadMessages;
    }
    public static void setHasUnreadMessages(boolean hasUnreadMessages)
    {
        ReimApplication.hasUnreadMessages = hasUnreadMessages;
    }

    public static Context getContext()
    {
        return context;
    }

    private void createDirectories()
    {
        try
        {
            AppPreference appPreference = AppPreference.getAppPreference();
            File dir = new File(appPreference.getAppDirectory());
            if (!dir.exists())
            {
                dir.mkdir();
            }
            dir = new File(appPreference.getAppImageDirectory());
            if (!dir.exists())
            {
                dir.mkdir();
                File nomediaFile = new File(dir, ".nomedia");
                nomediaFile.createNewFile();
            }
            dir = new File(appPreference.getAvatarImageDirectory());
            if (!dir.exists())
            {
                dir.mkdir();
                File nomediaFile = new File(dir, ".nomedia");
                nomediaFile.createNewFile();
                File tempAvatarFile = new File(dir, "temp.jpg");
                tempAvatarFile.createNewFile();
            }
            dir = new File(appPreference.getInvoiceImageDirectory());
            if (!dir.exists())
            {
                dir.mkdir();
                File nomediaFile = new File(dir, ".nomedia");
                nomediaFile.createNewFile();
                File tempInvoiceFile = new File(dir, "temp.jpg");
                tempInvoiceFile.createNewFile();
            }
            dir = new File(appPreference.getIconImageDirectory());
            if (!dir.exists())
            {
                dir.mkdir();
                File nomediaFile = new File(dir, ".nomedia");
                nomediaFile.createNewFile();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void saveCategoryIcon()
    {
        List<Integer> iconList = new ArrayList<>();
        iconList.add(R.drawable.icon_food);
        iconList.add(R.drawable.icon_transport);
        iconList.add(R.drawable.icon_office_supplies);
        iconList.add(R.drawable.icon_business_development);
        iconList.add(R.drawable.icon_marketing);
        iconList.add(R.drawable.icon_recruiting);
        iconList.add(R.drawable.icon_travel);
        iconList.add(R.drawable.icon_operating);
        iconList.add(R.drawable.icon_entertainment);
        iconList.add(R.drawable.icon_others);

        for (int i = 0; i < iconList.size(); i++)
        {
            File file = new File(PhoneUtils.getIconFilePath(i + 1));
            if (!file.exists())
            {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconList.get(i));
                PhoneUtils.saveIconToFile(bitmap, i + 1);
            }
        }
    }

    private void initPushService()
    {
        AVOSCloud.initialize(this, "25tdcbg3l8kp6yeqa4iqju6g788saf4xlseat1dxma3pdzfc",
                             "yc9e5h624ch14cgavj0r6b5yxq7fmn3y2nlm3hliq763syr1");

        PushService.subscribe(this, "public", MainActivity.class);
        AVInstallation.getCurrentInstallation().saveInBackground();
    }

    private void initMeChat()
    {
        MCClient.init(this, "5567e8fb4eae35495f000003", new OnInitCallback()
        {
            public void onSuccess(String s)
            {

            }

            public void onFailed(String s)
            {

            }
        });
    }

    private void initData()
    {
        context = getApplicationContext();
        TypeFaceYaHei = Typeface.createFromAsset(getAssets(), "fonts/YaHei.ttf");
        TypeFaceAleoLight = Typeface.createFromAsset(getAssets(), "fonts/Aleo_Light.ttf");

        try
        {
            Field field = Typeface.class.getDeclaredField("SERIF");
            field.setAccessible(true);
            field.set(null, TypeFaceYaHei);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ReimProgressDialog.init(context);

        AppPreference.createAppPreference(getApplicationContext());
        DBManager.createDBManager(getApplicationContext());

        String language = AppPreference.getAppPreference().getLanguage();
        if (!language.isEmpty())
        {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = getResources().getConfiguration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        DBManager dbManager = DBManager.getDBManager();
        if (dbManager.isCurrencyTableEmpty())
        {
            Currency currency = new Currency("CNY", "¥", 1);
            dbManager.insertCurrency(currency);

            currency = new Currency("USD", "$", 622.12);
            dbManager.insertCurrency(currency);

            currency = new Currency("EUR", "€", 702.37);
            dbManager.insertCurrency(currency);

            currency = new Currency("HKD", "$", 80.24);
            dbManager.insertCurrency(currency);

            currency = new Currency("MOP", "$", 80.52);
            dbManager.insertCurrency(currency);

            currency = new Currency("TWD", "$", 20.88);
            dbManager.insertCurrency(currency);

            currency = new Currency("JPY", "¥", 5.0389);
            dbManager.insertCurrency(currency);

            currency = new Currency("KER", "₩", 0.5881);
            dbManager.insertCurrency(currency);

            currency = new Currency("GBP", "£", 983.05);
            dbManager.insertCurrency(currency);

            currency = new Currency("RUB", "Rbs", 11.56);
            dbManager.insertCurrency(currency);

            currency = new Currency("SGD", "$", 465.38);
            dbManager.insertCurrency(currency);

            currency = new Currency("PHP", "₱", 14.23);
            dbManager.insertCurrency(currency);

            currency = new Currency("IDR", "Rps", 0.0482);
            dbManager.insertCurrency(currency);

            currency = new Currency("MYR", "$", 165.96);
            dbManager.insertCurrency(currency);

            currency = new Currency("THB", "฿", 19.06);
            dbManager.insertCurrency(currency);

            currency = new Currency("CAD", "$", 505.07);
            dbManager.insertCurrency(currency);

            currency = new Currency("AUD", "$", 480.12);
            dbManager.insertCurrency(currency);

            currency = new Currency("NZD", "$", 429.4);
            dbManager.insertCurrency(currency);

            currency = new Currency("CHF", "₣", 672.45);
            dbManager.insertCurrency(currency);

            currency = new Currency("DKK", "Kr", 94.17);
            dbManager.insertCurrency(currency);

            currency = new Currency("NOK", "Kr", 80.1);
            dbManager.insertCurrency(currency);

            currency = new Currency("SEK", "Kr", 76.1);
            dbManager.insertCurrency(currency);

            currency = new Currency("BRL", "$", 210.98);
            dbManager.insertCurrency(currency);
        }
    }
}