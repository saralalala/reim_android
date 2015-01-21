package classes.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import classes.ReimApplication;

import com.rushucloud.reim.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;

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
    
    public static void showToast(Context context, String content)
    {
    	Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
    
    public static void showToast(Context context, int resID)
    {
    	Toast.makeText(context, resID, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int resID, String errorMessage)
    {
    	Toast.makeText(context, context.getString(resID) + "，" + errorMessage, Toast.LENGTH_SHORT).show();
    }
    
	public static PopupWindow constructTopPopupWindow(final Activity activity, View view)
	{
		int backgroundColor = activity.getResources().getColor(R.color.hint_dark_grey);
		
		PopupWindow popupWindow = new PopupWindow(activity);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.MATCH_PARENT);
		popupWindow.setContentView(view);
		popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(R.style.WindowTopAnimation);
		
		return popupWindow;
	}
	
	public static PopupWindow constructBottomPopupWindow(final Activity activity, View view)
	{
		int backgroundColor = activity.getResources().getColor(R.color.hint_dark_grey);
		
		PopupWindow popupWindow = new PopupWindow(activity);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(view);
		popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(R.style.WindowBottomAnimation);
		popupWindow.setOnDismissListener(new OnDismissListener()
		{
			public void onDismiss()
			{
				recoverBackground(activity);
			}
		});
		
		return popupWindow;
	}
	
	public static PopupWindow constructHorizontalPopupWindow(final Activity activity, View view)
	{
		int backgroundColor = activity.getResources().getColor(R.color.hint_dark_grey);
		
		PopupWindow popupWindow = new PopupWindow(activity);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.MATCH_PARENT);
		popupWindow.setContentView(view);
		popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(R.style.WindowHorizontalAnimation);
		
		return popupWindow;
	}
	
	public static void dimBackground(Activity activity)
	{
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		params.alpha = (float) 0.4;
		activity.getWindow().setAttributes(params);		
	}
	
	public static void recoverBackground(Activity activity)
	{
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		params.alpha = (float) 1;
		activity.getWindow().setAttributes(params);
	}

	public static Button resizeLongButton(Button button)
	{
		Context context = ReimApplication.getContext();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.button_long_solid_light);
		double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		ViewGroup.LayoutParams params = button.getLayoutParams();
		params.width = metrics.widthPixels - marginPixels * 2;
		params.height = (int)(params.width * ratio);
		
		button.setLayoutParams(params);
		return button;
	}

	public static Button resizeShortButton(Button button, int height)
	{
		Context context = ReimApplication.getContext();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		int heightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.button_short_solid_light);
		double ratio = ((double)bitmap.getWidth()) / bitmap.getHeight();
		
		ViewGroup.LayoutParams params = button.getLayoutParams();
		params.width = (int)(heightPixels * ratio);
		params.height = heightPixels;
		
		button.setLayoutParams(params);
		return button;
	}
	
	public static Button resizeWindowButton(Button button)
	{
		Context context = ReimApplication.getContext();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.window_button_selected);
		double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
		
		ViewGroup.LayoutParams params = button.getLayoutParams();
		params.width = metrics.widthPixels - marginPixels * 2;
		params.height = (int)(params.width * ratio);
		
		button.setLayoutParams(params);
		return button;
	}

	public static OnFocusChangeListener getEditTextFocusChangeListener()
	{
		OnFocusChangeListener listener = new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (v instanceof EditText && hasFocus)
				{
					Spannable spanText = ((EditText)v).getText();
					Selection.setSelection(spanText, spanText.length());
				}
			}
		};
		
		return listener;
	}
}