package classes.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;

import com.avos.avoscloud.AVInstallation;

import java.io.File;

import classes.model.Group;
import classes.model.Proxy;
import classes.model.User;

public class AppPreference
{
    private static AppPreference appPreference = null;
    private Context context = null;

    private int currentUserID = -1;
    private int currentGroupID = -1;
    private int proxyUserID = -1;
    private int proxyPermission = -1;
    private String username = "";
    private String password = "";
    private boolean hasPassword = true;
    private String deviceToken = "";
    private String serverToken = "";
    private boolean syncOnlyWithWifi = true;
    private boolean enablePasswordProtection = true;
    private int lastSyncTime = 0;
    private int lastGetOthersReportTime = 0;
    private int lastGetMineStatTime = 0;
    private int lastGetOthersStatTime = 0;
    private int lastShownGuideVersion = 0;
    private boolean needToShowReimGuide = true;
    private boolean needToShowReportGuide = true;
    private boolean sandboxMode = false;
    private String language = "";
    private String appDirectory = "";
    private String appImageDirectory = "";
    private String avatarImageDirectory = "";
    private String invoiceImageDirectory = "";
    private String iconImageDirectory = "";

    private AppPreference(Context context)
    {
        this.context = context;
    }

    public static synchronized void createAppPreference(Context context)
    {
        if (appPreference == null)
        {
            appPreference = new AppPreference(context);
            appPreference.readAppPreference();
        }
    }

    public static AppPreference getAppPreference()
    {
        return appPreference;
    }

    public void readAppPreference()
    {
        SharedPreferences preferences = context.getSharedPreferences("ReimApplication", Application.MODE_PRIVATE);
        appPreference.setCurrentUserID(preferences.getInt("currentUserID", -1));
        appPreference.setCurrentGroupID(preferences.getInt("currentGroupID", -1));
        appPreference.setProxyUserID(preferences.getInt("proxyUserID", -1));
        appPreference.setProxyPermission(preferences.getInt("proxyPermission", -1));
        appPreference.setUsername(preferences.getString("username", ""));
        appPreference.setPassword(preferences.getString("password", ""));
        appPreference.setHasPassword(preferences.getBoolean("hasPassword", true));
        appPreference.setDeviceToken(AVInstallation.getCurrentInstallation().getInstallationId());
        appPreference.setServerToken(preferences.getString("serverToken", ""));
        appPreference.setSyncOnlyWithWifi(preferences.getBoolean("syncOnlyWithWifi", true));
        appPreference.setEnablePasswordProtection(preferences.getBoolean("enablePasswordProtection", true));
        appPreference.setLastSyncTime(preferences.getInt("lastSyncTime", 0));
        appPreference.setLastGetOthersReportTime(preferences.getInt("lastGetOthersReportTime", 0));
        appPreference.setLastGetMineStatTime(preferences.getInt("lastGetMineStatTime", 0));
        appPreference.setLastGetOthersStatTime(preferences.getInt("lastGetOthersStatTime", 0));
        appPreference.setLastShownGuideVersion(preferences.getInt("lastShownGuideVersion", 0));
        appPreference.setNeedToShowReimGuide(preferences.getBoolean("needToShowReimGuide", true));
        appPreference.setNeedToShowReportGuide(preferences.getBoolean("needToShowReportGuide", true));
        appPreference.setSandboxMode(preferences.getBoolean("sandboxMode", false));
        appPreference.setLanguage(preferences.getString("language", ""));

        appPreference.setAppDirectory(Environment.getExternalStorageDirectory() + "/cloudbaoxiao");
        appPreference.setAppImageDirectory(appPreference.getAppDirectory() + "/images");
        appPreference.setAvatarImageDirectory(appPreference.getAppImageDirectory() + "/avatar");
        appPreference.setInvoiceImageDirectory(appPreference.getAppImageDirectory() + "/invoice");
        appPreference.setIconImageDirectory(appPreference.getAppImageDirectory() + "/icon");
    }

    public void saveAppPreference()
    {
        SharedPreferences sharedPreference = context.getSharedPreferences("ReimApplication", Application.MODE_PRIVATE);
        Editor editor = sharedPreference.edit();
        editor.putInt("currentUserID", appPreference.getCurrentUserID());
        editor.putInt("currentGroupID", appPreference.getCurrentGroupID());
        editor.putInt("proxyUserID", appPreference.getProxyUserID());
        editor.putInt("proxyPermission", appPreference.getProxyPermission());
        editor.putString("username", appPreference.getUsername());
        editor.putString("password", appPreference.getPassword());
        editor.putBoolean("hasPassword", appPreference.hasPassword());
        editor.putString("deviceToken", appPreference.getDeviceToken());
        editor.putString("serverToken", appPreference.getServerToken());
        editor.putBoolean("syncOnlyWithWifi", appPreference.syncOnlyWithWifi());
        editor.putBoolean("enablePasswordProtection", appPreference.passwordProtectionEnabled());
        editor.putInt("lastShownGuideVersion", appPreference.getLastShownGuideVersion());
        editor.putInt("lastSyncTime", appPreference.getLastSyncTime());
        editor.putInt("lastGetOthersReportTime", appPreference.getLastGetOthersReportTime());
        editor.putInt("lastGetMineStatTime", appPreference.getLastGetMineStatTime());
        editor.putBoolean("needToShowReimGuide", appPreference.needToShowReimGuide());
        editor.putBoolean("needToShowReportGuide", appPreference.needToShowReportGuide());
        editor.putBoolean("sandboxMode", appPreference.isSandboxMode());
        editor.putString("language", appPreference.getLanguage());
        editor.apply();
    }

    public int getCurrentUserID()
    {
        return currentUserID;
    }
    public void setCurrentUserID(int currentUserID)
    {
        this.currentUserID = currentUserID;
    }
    public User getCurrentUser()
    {
        return DBManager.getDBManager().getUser(currentUserID);
    }

    public int getCurrentGroupID()
    {
        return currentGroupID;
    }
    public void setCurrentGroupID(int currentGroupID)
    {
        this.currentGroupID = currentGroupID;
    }
    public Group getCurrentGroup()
    {
        return DBManager.getDBManager().getGroup(currentGroupID);
    }

    public int getProxyUserID()
    {
        return proxyUserID;
    }
    public void setProxyUserID(int proxyUserID)
    {
        this.proxyUserID = proxyUserID;
    }
    public boolean isProxyMode()
    {
        return proxyUserID != -1;
    }

    public int getProxyPermission()
    {
        return proxyPermission;
    }
    public void setProxyPermission(int proxyPermission)
    {
        this.proxyPermission = proxyPermission;
    }
    public boolean hasProxyEditPermission() // include the situation of no proxy
    {
        return proxyPermission != Proxy.PERMISSION_APPROVE;
    }

    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean hasPassword()
    {
        return hasPassword;
    }
    public void setHasPassword(boolean hasPassword)
    {
        this.hasPassword = hasPassword;
    }

    public String getDeviceToken()
    {
        return deviceToken;
    }
    public void setDeviceToken(String deviceToken)
    {
        this.deviceToken = deviceToken;
    }

    public String getServerToken()
    {
        return serverToken;
    }
    public void setServerToken(String serverToken)
    {
        this.serverToken = serverToken;
    }

    public boolean syncOnlyWithWifi()
    {
        return syncOnlyWithWifi;
    }
    public void setSyncOnlyWithWifi(boolean syncOnlyWithWifi)
    {
        this.syncOnlyWithWifi = syncOnlyWithWifi;
    }

    public boolean passwordProtectionEnabled()
    {
        return enablePasswordProtection;
    }
    public void setEnablePasswordProtection(boolean enablePasswordProtection)
    {
        this.enablePasswordProtection = enablePasswordProtection;
    }

    public int getLastSyncTime()
    {
        return lastSyncTime;
    }
    public void setLastSyncTime(int lastSyncTime)
    {
        this.lastSyncTime = lastSyncTime;
    }

    public int getLastGetOthersReportTime()
    {
        return lastGetOthersReportTime;
    }
    public void setLastGetOthersReportTime(int lastGetOthersReportTime)
    {
        this.lastGetOthersReportTime = lastGetOthersReportTime;
    }

    public int getLastGetMineStatTime()
    {
        return lastGetMineStatTime;
    }
    public void setLastGetMineStatTime(int lastGetMineStatTime)
    {
        this.lastGetMineStatTime = lastGetMineStatTime;
    }

    public int getLastGetOthersStatTime()
    {
        return lastGetOthersStatTime;
    }
    public void setLastGetOthersStatTime(int lastGetOthersStatTime)
    {
        this.lastGetOthersStatTime = lastGetOthersStatTime;
    }

    public int getLastShownGuideVersion()
    {
        return lastShownGuideVersion;
    }
    public void setLastShownGuideVersion(int lastShownGuideVersion)
    {
        this.lastShownGuideVersion = lastShownGuideVersion;
    }

    public boolean needToShowReimGuide()
    {
        return needToShowReimGuide;
    }
    public void setNeedToShowReimGuide(boolean needToShowReimGuide)
    {
        this.needToShowReimGuide = needToShowReimGuide;
    }

    public boolean needToShowReportGuide()
    {
        return needToShowReportGuide;
    }
    public void setNeedToShowReportGuide(boolean needToShowReportGuide)
    {
        this.needToShowReportGuide = needToShowReportGuide;
    }

    public boolean isSandboxMode()
    {
        return sandboxMode;
    }
    public void setSandboxMode(boolean sandboxMode)
    {
        this.sandboxMode = sandboxMode;
    }

    public String getLanguage()
    {
        return language;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getAppDirectory()
    {
        return appDirectory;
    }
    public void setAppDirectory(String appDirectory)
    {
        this.appDirectory = appDirectory;
    }

    public String getAppImageDirectory()
    {
        return appImageDirectory;
    }
    public void setAppImageDirectory(String appImageDirectory)
    {
        this.appImageDirectory = appImageDirectory;
    }

    public String getAvatarImageDirectory()
    {
        return avatarImageDirectory;
    }
    public void setAvatarImageDirectory(String avatarImageDirectory)
    {
        this.avatarImageDirectory = avatarImageDirectory;
    }

    public String getTempAvatarPath()
    {
        return getAvatarImageDirectory() + "/temp.png";
    }
    public Uri getTempAvatarUri()
    {
        return Uri.fromFile(new File(getAvatarImageDirectory() + "/temp.png"));
    }

    public String getInvoiceImageDirectory()
    {
        return invoiceImageDirectory;
    }
    public void setInvoiceImageDirectory(String invoiceImageDirectory)
    {
        this.invoiceImageDirectory = invoiceImageDirectory;
    }

    public String getTempInvoicePath()
    {
        return getInvoiceImageDirectory() + "/temp.png";
    }
    public Uri getTempInvoiceUri()
    {
        return Uri.fromFile(new File(getInvoiceImageDirectory() + "/temp.png"));
    }

    public String getIconImageDirectory()
    {
        return iconImageDirectory;
    }
    public void setIconImageDirectory(String iconImageDirectory)
    {
        this.iconImageDirectory = iconImageDirectory;
    }
}