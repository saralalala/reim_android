package classes;

import java.io.File;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.rushucloud.reim.start.WelcomeActivity;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class ReimApplication extends Application
{
	public static ProgressDialog pDialog;

	public void onCreate()
	{
		AVOSCloud.initialize(this, "25tdcbg3l8kp6yeqa4iqju6g788saf4xlseat1dxma3pdzfc",
				"yc9e5h624ch14cgavj0r6b5yxq7fmn3y2nlm3hliq763syr1");

		super.onCreate();

		createDirectories();
		AppPreference.createAppPreference(getApplicationContext());
		DBManager.createDBManager(getApplicationContext());
		PushService.setDefaultPushCallback(this, WelcomeActivity.class);
		PushService.subscribe(this, "public", WelcomeActivity.class);
		AVInstallation.getCurrentInstallation().saveInBackground();
		MobclickAgent.openActivityDurationTrack(false);
//		System.out.println(getDeviceInfo(this));
	}

	private void createDirectories()
	{
		try
		{
			String appDirectory = Environment.getExternalStorageDirectory() + "/如数云报销";
			File dir = new File(appDirectory);
			if (!dir.exists())
			{
				dir.mkdir();
			}
			dir = new File(appDirectory + "/images");
			if (!dir.exists())
			{
				dir.mkdir();
				File nomediaFile = new File(dir, ".nomedia");
				nomediaFile.createNewFile();
			}
			dir = new File(appDirectory + "/images/profile");
			if (!dir.exists())
			{
				dir.mkdir();
				File nomediaFile = new File(dir, ".nomedia");
				nomediaFile.createNewFile();
			}
			dir = new File(appDirectory + "/images/invoice");
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

	public static void setProgressDialog(Context context)
	{
		pDialog = new ProgressDialog(context);
		pDialog.setMessage("读取数据中，请稍等……");
	}

	public static String getDeviceInfo(Context context)
	{
		try
		{
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id))
			{
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id))
			{
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}