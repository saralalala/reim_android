package classes.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.rushucloud.reim.R;

import classes.ReimApplication;

public class ViewUtils
{
	public static int getColor(int colorResID)
	{
		return ReimApplication.getContext().getResources().getColor(colorResID);
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
    
	public static PopupWindow buildTopPopupWindow(final Activity activity, View view)
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
    
	public static PopupWindow buildCenterPopupWindow(final Activity activity, View view)
	{
		int backgroundColor = activity.getResources().getColor(R.color.hint_dark_grey);
		
		PopupWindow popupWindow = new PopupWindow(activity);
		popupWindow.setWidth(PhoneUtils.dpToPixel(activity, 210));
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(view);
		popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setAnimationStyle(R.style.WindowCenterAnimation);
		
		return popupWindow;
	}
	
	public static PopupWindow buildBottomPopupWindow(final Activity activity, View view)
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

	public static void dimBackground(Activity activity)
	{
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		params.alpha = 0.4f;
		activity.getWindow().setAttributes(params);		
	}
	
	public static void recoverBackground(Activity activity)
	{
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		params.alpha = 1f;
		activity.getWindow().setAttributes(params);
	}

	public static OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener()
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
}