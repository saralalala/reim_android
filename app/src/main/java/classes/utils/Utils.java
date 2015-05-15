package classes.utils;

import android.text.TextUtils;

import com.rushucloud.reim.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	public static int getCurrentTime()
	{
		Date date = new Date();
		long result = date.getTime() / 1000;
		return (int) result;
	}

    public static int getCurrentYear()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) getCurrentTime() * 1000);
        return calendar.get(Calendar.YEAR);
    }

    public static int getCurrentMonth()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) getCurrentTime() * 1000);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static String getMonthString(int year, int month)
    {
        return year + ViewUtils.getString(R.string.year) + month + ViewUtils.getString(R.string.month);
    }

	public static String secondToStringUpToMinute(int second)
	{
		if (second <= 0)
		{
			return "";
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((long) second * 1000);
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
		calendar.setTimeInMillis((long) second * 1000);
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

	public static String dateToWeekday(String date)
	{
		int year = Integer.valueOf(date.substring(0, 4));
		int month = Integer.valueOf(date.substring(5, 7));
		int day = Integer.valueOf(date.substring(8, 10));
		
		GregorianCalendar calendar = new GregorianCalendar(year, month, day);
		switch (calendar.get(GregorianCalendar.DAY_OF_WEEK))
		{
			case GregorianCalendar.SUNDAY:
				return "周日";
			case GregorianCalendar.MONDAY:
				return "周一";
			case GregorianCalendar.TUESDAY:
				return "周二";
			case GregorianCalendar.WEDNESDAY:
				return "周三";
			case GregorianCalendar.THURSDAY:
				return "周四";
			case GregorianCalendar.FRIDAY:
				return "周五";
			case GregorianCalendar.SATURDAY:
				return "周六";
			default:
				return "";
		}
	}
	
	public static boolean isEmailOrPhone(String source)
	{
		return isEmail(source) || isPhone(source);
	}
	
	public static boolean isEmail(String source)
	{
		if (!source.contains("@"))
		{
			return false;
		}

        String regexEmail = "^([a-z0-9A-Z]+[_\\-\\.]?)+[a-z0-9A-Z]?@([a-z0-9A-Z]+([-|_]?[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern pattern = Pattern.compile(regexEmail);
		Matcher matcher = pattern.matcher(source);
		return matcher.find();
	}
	
	public static boolean isPhone(String source)
	{
        String regexPhone = "^[1]+\\d{10}$";
		Pattern pattern = Pattern.compile(regexPhone);
		Matcher matcher = pattern.matcher(source);
		return matcher.find();
	}

    public static boolean isBankAccount(String source)
    {
        String regexBankAccount = "^\\d{12,19}$";
        Pattern pattern = Pattern.compile(regexBankAccount);
        Matcher matcher = pattern.matcher(source);
        return matcher.find();
    }

	public static int booleanToInt(boolean b)
	{
		return b? 1 : 0;
	}

	public static boolean intToBoolean(int i)
	{
		return i > 0;
	}

	public static String booleanToString(boolean b)
	{
		return b? "1" : "0";
	}

    public static List<Integer> stringToIntList(String idString)
    {
    	List<Integer> resultList = new ArrayList<>();
    	String[] result = TextUtils.split(idString, ",");
        for (String resultString : result)
        {
            resultList.add(Integer.valueOf(resultString.trim()));
        }
    	return resultList;
    } 

    public static double stringToDouble(String source)
    {
    	double amount = Double.valueOf(source);
    	return Double.valueOf(formatDouble(amount));
    }
    
    public static double roundDouble(double arg)
    {
    	if (arg > 0 & arg < 0.1)
		{
			return 0.1;
		}
		DecimalFormat format = new DecimalFormat("#0.00");
		return Double.valueOf(format.format(arg));
    }
    
    public static String formatDouble(double arg)
    {
		DecimalFormat format = new DecimalFormat("#0.00");
		String result = format.format(arg);
		if (result.charAt(result.length() - 1) == '0')
		{
			result = result.substring(0, result.length() - 1);
		}
		if (result.charAt(result.length() - 1) == '0')
		{
			result = result.substring(0, result.length() - 1);
		}
		if (result.charAt(result.length() - 1) == '.')
		{
			result = result.substring(0, result.length() - 1);
		}
		return result;
    }
}