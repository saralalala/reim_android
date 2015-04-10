package classes.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.rushucloud.reim.R;

import classes.Category;
import classes.StatCategory;
import classes.User;

public class ViewUtils
{
    public static OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener()
    {
        public void onFocusChange(View v, boolean hasFocus)
        {
            if (v instanceof EditText && hasFocus)
            {
                Spannable spanText = ((EditText) v).getText();
                Selection.setSelection(spanText, spanText.length());
            }
        }
    };

	public static int getColor(int colorResID)
	{
		return ReimApplication.getContext().getResources().getColor(colorResID);
	}

    public static String getString(int stringResID)
    {
        return ReimApplication.getContext().getResources().getString(stringResID);
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
    
	public static PopupWindow buildTopPopupWindow(final Context context, View view)
	{
		int backgroundColor = ViewUtils.getColor(android.R.color.transparent);
		
		PopupWindow popupWindow = new PopupWindow(context);
		popupWindow.setWidth(LayoutParams.MATCH_PARENT);
		popupWindow.setHeight(LayoutParams.MATCH_PARENT);
		popupWindow.setContentView(view);
		popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(R.style.WindowTopAnimation);
		
		return popupWindow;
	}
    
	public static PopupWindow buildCenterPopupWindow(final Context context, View view)
	{
		int backgroundColor = ViewUtils.getColor(android.R.color.transparent);
		
		PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setWidth(getPhoneWindowWidth(context) - dpToPixel(70));
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
		int backgroundColor = ViewUtils.getColor(android.R.color.transparent);
		
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

    public static PopupWindow buildSurprisePopupWindow(final Activity activity, View view)
    {
        int backgroundColor = ViewUtils.getColor(android.R.color.transparent);

        PopupWindow popupWindow = new PopupWindow(activity);
        popupWindow.setWidth(getPhoneWindowWidth(activity) - dpToPixel(100));
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.WindowCenterAnimation);
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

    public static void setImageViewBitmap(User user, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.default_avatar);
        if (user != null && !user.getAvatarLocalPath().isEmpty())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarLocalPath());
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static void setImageViewBitmap(Category category, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.default_icon);
        if (category != null && !category.getIconPath().isEmpty())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static void setImageViewBitmap(StatCategory category, ImageView imageView)
    {
        imageView.setImageResource(R.drawable.default_icon);
        if (category != null && !category.getIconPath().isEmpty())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(category.getIconPath());
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static int getPhoneWindowWidth(Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int dpToPixel(double dp)
    {
    	DisplayMetrics metrics = ReimApplication.getContext().getResources().getDisplayMetrics();
    	return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }
}