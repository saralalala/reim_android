package classes.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class Utils
{
	private static String regexEmail = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
			 
	private static String regexPhone = "[1]+\\d{10}";

	public static int getCurrentTime()
	{
		Date date = new Date();
		long result = date.getTime() / 1000;
		return (int)result;
	}
	
	public static String secondToStringUpToMinute(int second)
	{
		if (second <= 0)
		{
			return "";
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long)second * 1000);
		String result = "";
		result += calendar.get(Calendar.YEAR) + "-";
		
		int month = calendar.get(Calendar.MONTH) + 1;
		if (month < 10)
		{
			result += "0";			
		}
		result += month + "-";
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
		{
			result += "0";			
		}
		result += day + "  ";
		
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
		result += calendar.get(Calendar.YEAR) + "-";
		
		int month = calendar.get(Calendar.MONTH) + 1;
		if (month < 10)
		{
			result += "0";			
		}
		result += month + "-";
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
		{
			result += "0";			
		}
		result += day;
		
		return result;
	}

	public static boolean isEmailOrPhone(String source)
	{
		return isEmail(source) || isPhone(source);
	}
	
	public static boolean isEmail(String source)
	{
		Pattern pattern = Pattern.compile(regexEmail);
		Matcher matcher = pattern.matcher(source);
		return matcher.find();
	}
	
	public static boolean isPhone(String source)
	{
		Pattern pattern = Pattern.compile(regexPhone);
		Matcher matcher = pattern.matcher(source);
		return matcher.find();
	}

	public static int booleanToInt(boolean b)
	{
		return b ? 1 : 0;
	}

	public static boolean intToBoolean(int i)
	{
		return i > 0;
	}

	public static String booleanToString(boolean b)
	{
		return b ? "1" : "0";
	}

    public static List<Integer> stringToIntList(String idString)
    {
    	List<Integer> resultList = new ArrayList<Integer>();
    	String[] result = TextUtils.split(idString, ",");
    	for (int i = 0; i < result.length; i++)
		{
    		resultList.add(Integer.valueOf(result[i].trim()));
		}
    	return resultList;
    } 

    public static double roundDouble(double arg)
    {
    	if (arg > 0 & arg < 0.1)
		{
			return 0.1;
		}
		DecimalFormat format = new DecimalFormat("#0.0");
		return Double.valueOf(format.format(arg));
    }
    
    public static String formatDouble(double arg)
    {
		DecimalFormat format = new DecimalFormat("#0.0");
		return format.format(arg);
    }
}