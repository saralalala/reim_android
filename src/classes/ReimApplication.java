package classes;

import java.io.File;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.rushucloud.reim.start.WelcomeActivity;

import database.DBManager;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

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
}