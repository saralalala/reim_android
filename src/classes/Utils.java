package classes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import netUtils.HttpConstant;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

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
	
	public static boolean isLocalisationEnabled(Context context)
	{
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		return gpsEnabled || networkEnabled ? true :false;
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

    public static List<Integer> stringToIntList(String idString)
    {
    	List<Integer> resultList = new ArrayList<Integer>();
    	String[] result = TextUtils.split(idString, ",");
    	for (int i = 0; i < result.length; i++)
		{
    		resultList.add(Integer.valueOf(result[i]));
		}
    	return resultList;
    } 
    
    public static String getImageName()
    {
//    	int time = Utils.getCurrentTime();
//    	
//    	Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis((long)time * 1000);
//		String result = "";
//		result += calendar.get(Calendar.YEAR);
//		
//		if (calendar.get(Calendar.MONTH)+1 < 10)
//		{
//			result += "0";			
//		}
//		result += (calendar.get(Calendar.MONTH) + 1);
//		
//		if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
//		{
//			result += "0";			
//		}
//		result += calendar.get(Calendar.DAY_OF_MONTH);
//		
//		if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
//		{
//			result += "0";			
//		}
//		result += calendar.get(Calendar.HOUR_OF_DAY);	
//		
//		if (calendar.get(Calendar.MINUTE) < 10)
//		{
//			result += "0";			
//		}
//		result += calendar.get(Calendar.MINUTE) + ".jpg";
		
		long currentTime = new Date().getTime();
		
		return Long.toString(currentTime) + ".jpg";
    }
    
    public static String saveBitmapToFile(Bitmap bitmap, int type)
    {
    	try
		{    		
    		AppPreference appPreference = AppPreference.getAppPreference();
    		Matrix matrix = new Matrix();
    		matrix.postScale((float)0.5, (float)0.5);
    		
    		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    		
    		String path;
    		if (type == HttpConstant.IMAGE_TYPE_AVATAR)
			{
				path = appPreference.getProfileImageDirectory() + "/" + getImageName();
			}
    		else
    		{
				path = appPreference.getInvoiceImageDirectory() + "/" + getImageName();    			
    		}
    		
    		File compressedBitmapFile = new File(path);
    		compressedBitmapFile.createNewFile();
    		
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		bitmap.compress(CompressFormat.JPEG, 90, outputStream);
    		byte[] bitmapData = outputStream.toByteArray();
    		
    		FileOutputStream fileOutputStream = new FileOutputStream(compressedBitmapFile);
    		fileOutputStream.write(bitmapData);
    		fileOutputStream.flush();
    		fileOutputStream.close();	
    		
    		return path;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}
    }
}