package classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils
{	
	private static String regexEmail = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
			 
	private static String regexPhone = "[1]+\\d{10}";
	
	public static String getPathFromUri(Activity activity, Uri uri)
	{
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		return cursor.getString(index);
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
}
