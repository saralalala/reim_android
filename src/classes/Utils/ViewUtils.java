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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import classes.ReimApplication;

import com.rushucloud.reim.R;

public class ViewUtils
{
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
		
		int marginPixels = PhoneUtils.dpToPixel(context, 16);
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
		
		int heightPixels = PhoneUtils.dpToPixel(context, height);
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
		
		int marginPixels = PhoneUtils.dpToPixel(context, 10);
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