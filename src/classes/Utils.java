package classes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils
{	
	private static String regexEmail = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
			 
	private static String regexPhone = "[1]+\\d{10}";

	public static boolean isWiFiConnected(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo != null)
		{
			return networkInfo.isConnected();			
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isDataConnected(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null)
		{
			return networkInfo.isConnected();			
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isNetworkConnected(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null)
		{
			return networkInfo.isAvailable();			
		}
		else
		{
			return false;
		}
	}
	
	public static boolean canSyncToServer(Context context)
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		if (appPreference.syncOnlyWithWifi() && isWiFiConnected(context))
		{
			return true;
		}
		else if (!appPreference.syncOnlyWithWifi() && isDataConnected(context))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static String getPathFromUri(Activity activity, Uri uri)
	{
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		return cursor.getString(index);
	}
	
	public static int getCurrentTime()
	{
		Date date = new Date();
		long result = date.getTime() / 1000;
		return (int)result;
	}
	
	public static String secondToStringUpToMinute(int second)
	{
		if (second == -1)
		{
			return "";
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)second * 1000);
		String result = "";
		result += calendar.get(Calendar.YEAR) + "年";
		result += (calendar.get(Calendar.MONTH) + 1) + "月";
		result += calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
		
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.HOUR_OF_DAY) + ":";
		
		if (calendar.get(Calendar.MINUTE) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.MINUTE);
		
		return result;
	}
	
	public static String secondToStringUpToDay(int second)
	{
		if (second == -1)
		{
			return "";
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)second * 1000);
		String result = "";
		result += calendar.get(Calendar.YEAR) + "年";
		result += (calendar.get(Calendar.MONTH) + 1) + "月";
		result += calendar.get(Calendar.DAY_OF_MONTH) + "日";
		
		return result;
	}

	public static Boolean isEmailOrPhone(String source)
	{
		if (isEmail(source) || isPhone(source))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static Boolean isEmail(String source)
	{
		Pattern pattern = Pattern.compile(regexEmail);
		Matcher matcher = pattern.matcher(source);
		if (matcher.find())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static Boolean isPhone(String source)
	{
		Pattern pattern = Pattern.compile(regexPhone);
		Matcher matcher = pattern.matcher(source);
		if (matcher.find())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static int booleanToInt(boolean b)
	{
		return b ? 1 : 0;
	}

	public static Boolean intToBoolean(int i)
	{
		return i > 0 ? true : false;
	}

	public static int booleanToString(boolean b)
	{
		return b ? 1 : 0;
	}
	
	public static int[] intListToArray(List<Integer> intList)
	{
		int[] intArray = new int[intList.size()];
		for (int i = 0; i < intArray.length; i++)
		{
			intArray[i] = intList.get(i);
		}
		return intArray;
	}

    public static ArrayList<Integer> itemListToIDArray(List<Item> itemList)
    {
    	ArrayList<Integer> idArrayList = new ArrayList<Integer>();
    	for (int i = 0; i < itemList.size(); i++)
		{
			idArrayList.add(itemList.get(i).getLocalID());
		}
    	return idArrayList;
    }

    public static String getImageName()
    {
    	int time = Utils.getCurrentTime();
    	
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)time * 1000);
		String result = "";
		result += calendar.get(Calendar.YEAR);
		
		if (calendar.get(Calendar.MONTH)+1 < 10)
		{
			result += "0";			
		}
		result += (calendar.get(Calendar.MONTH) + 1);
		
		if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.DAY_OF_MONTH);
		
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.HOUR_OF_DAY);	
		
		if (calendar.get(Calendar.MINUTE) < 10)
		{
			result += "0";			
		}
		result += calendar.get(Calendar.MINUTE) + ".jpg";
		
		return result;
    }
}