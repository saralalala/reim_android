package classes;

import java.io.File;

import database.DBManager;

import android.app.Application;
import android.os.Environment;

public class ReimApplication extends Application
{	
	public void onCreate()
	{
		super.onCreate();
		createDirectories();
		AppPreference.createAppPreference(getApplicationContext());
		DBManager.createDBManager(getApplicationContext());
	}
	
	private void createDirectories()
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
		}
		dir = new File(appDirectory + "/images/profile");
		if (!dir.exists())
		{
			dir.mkdir();
		}
		dir = new File(appDirectory + "/images/invoice");
		if (!dir.exists())
		{
			dir.mkdir();
		}		
	}
}