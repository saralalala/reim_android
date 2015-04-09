package classes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.rushucloud.reim.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import netUtils.NetworkConstant;

public class PhoneUtils
{
	public static String getAppVersion()
	{
		Context context = ReimApplication.getContext();
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			return context.getString(R.string.failed_to_get_version);
		}
	}

	public static boolean isWiFiConnected()
	{
		ConnectivityManager manager = (ConnectivityManager) ReimApplication.getContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	public static boolean isDataConnected()
	{
		ConnectivityManager manager = (ConnectivityManager) ReimApplication.getContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	public static boolean isNetworkConnected()
	{
		ConnectivityManager manager = (ConnectivityManager) ReimApplication.getContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	public static boolean isLocalisationEnabled()
	{
		LocationManager locationManager = (LocationManager) ReimApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		return gpsEnabled || networkEnabled? true : false;
	}

	public static String getPathFromUri(Activity activity, Uri uri)
	{
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		String result = cursor.getString(index);
		cursor.close();
		return result;
	}

	public static String getImageName()
	{
		long currentTime = new Date().getTime();

		return Long.toString(currentTime) + ".jpg";
	}

	public static String getAvatarFilePath()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		return appPreference.getAvatarImageDirectory() + "/" + getImageName();
	}

	public static String getInvoiceFilePath()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		return appPreference.getInvoiceImageDirectory() + "/" + getImageName();
	}

	public static String getIconFilePath(int iconID)
	{
		return AppPreference.getAppPreference().getIconImageDirectory() + "/" + iconID + ".png";
	}

	public static String saveBitmapToFile(Bitmap bitmap, int type)
	{
		try
		{
			Matrix matrix = new Matrix();
			matrix.postScale(0.5f, 0.5f);

			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

			String path;
			if (type == NetworkConstant.IMAGE_TYPE_AVATAR)
			{
				path = getAvatarFilePath();
			}
			else
			{
				path = getInvoiceFilePath();
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

	public static String saveIconToFile(Bitmap bitmap, int iconID)
	{
		try
		{
			Matrix matrix = new Matrix();
			matrix.postScale(0.5f, 0.5f);

			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

			String path = getIconFilePath(iconID);

			File compressedBitmapFile = new File(path);
			compressedBitmapFile.createNewFile();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 100, outputStream);
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

	public static boolean copyFile(String oldPath, String newPath)
	{
		try
		{
			int byteRead = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists())
			{
				InputStream inputStream = new FileInputStream(oldPath);
				FileOutputStream outputStream = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteRead = inputStream.read(buffer)) != -1)
				{
					outputStream.write(buffer, 0, byteRead);
				}
				inputStream.close();
				outputStream.close();
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

    public static boolean isMIUIV6()
    {
        return Build.MANUFACTURER.equals("Xiaomi") && getSystemProperty("ro.miui.ui.version.name").equals("V6");
    }

    public static String getSystemProperty(String propName)
    {
        String line = "";
        BufferedReader input = null;
        try
        {
            Process process = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read sysprop " + propName);
            return line;
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    System.out.println("Exception while closing InputStream");
                }
            }
        }
        return line;
    }
}