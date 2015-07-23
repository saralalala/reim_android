package classes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rushucloud.reim.R;

import java.util.HashMap;

import classes.model.Category;
import classes.model.StatCategory;
import classes.model.User;
import classes.widget.CircleImageView;

public class ViewUtils
{
    public static String[] indexLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

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

    public static ColorStateList getColorStateList(int colorResID)
    {
        return ReimApplication.getContext().getResources().getColorStateList(colorResID);
    }

    public static int getCategoryColorR(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 56;
            case 1:
                return 60;
            case 2:
                return 181;
            case 3:
                return 232;
            case 4:
                return 181;
            case 5:
                return 141;
            case 6:
                return 62;
            case 7:
                return 255;
            case 8:
                return 138;
            case 9:
                return 238;
            case 10:
                return 125;
            case 11:
                return 242;
            default:
                return 56;
        }
    }

    public static int getCategoryColorG(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 56;
            case 1:
                return 183;
            case 2:
                return 112;
            case 3:
                return 140;
            case 4:
                return 184;
            case 5:
                return 192;
            case 6:
                return 119;
            case 7:
                return 196;
            case 8:
                return 118;
            case 9:
                return 149;
            case 10:
                return 173;
            case 11:
                return 137;
            default:
                return 56;
        }
    }

    public static int getCategoryColorB(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 56;
            case 1:
                return 152;
            case 2:
                return 178;
            case 3:
                return 192;
            case 4:
                return 69;
            case 5:
                return 219;
            case 6:
                return 219;
            case 7:
                return 0;
            case 8:
                return 203;
            case 9:
                return 50;
            case 10:
                return 165;
            case 11:
                return 92;
            default:
                return 56;
        }
    }

    public static int getCategoryColorRDiff(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 169;
            case 1:
                return 137;
            case 2:
                return 52;
            case 3:
                return 16;
            case 4:
                return 52;
            case 5:
                return 80;
            case 6:
                return 135;
            case 7:
                return 0;
            case 8:
                return 82;
            case 9:
                return 12;
            case 10:
                return 91;
            case 11:
                return 10;
            default:
                return 169;
        }
    }

    public static int getCategoryColorGDiff(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 169;
            case 1:
                return 51;
            case 2:
                return 100;
            case 3:
                return 81;
            case 4:
                return 50;
            case 5:
                return 44;
            case 6:
                return 95;
            case 7:
                return 41;
            case 8:
                return 96;
            case 9:
                return 74;
            case 10:
                return 58;
            case 11:
                return 94;
            default:
                return 169;
        }
    }

    public static int getCategoryColorBDiff(int iconID)
    {
        switch (iconID)
        {
            case 0:
                return 169;
            case 1:
                return 72;
            case 2:
                return 54;
            case 3:
                return 45;
            case 4:
                return 131;
            case 5:
                return 25;
            case 6:
                return 25;
            case 7:
                return 179;
            case 8:
                return 37;
            case 9:
                return 144;
            case 10:
                return 63;
            case 11:
                return 130;
            default:
                return 169;
        }
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

    public static void requestFocus(final Context context, final EditText editText)
    {
        editText.requestFocus();
        editText.postDelayed(new Runnable()
        {
            public void run()
            {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, 0);
            }
        }, 200);
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

    public static void setImageViewBitmap(User user, CircleImageView imageView)
    {
        imageView.setImageResource(R.drawable.default_avatar);
        if (user != null && !user.getAvatarLocalPath().isEmpty())
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarLocalPath(), options);
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

    public static void setTextBold(SpannableString text, int start, int end)
    {
        text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void setTextBoldAndUnderlined(SpannableString text, int start, int end)
    {
        text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static int getPhoneWindowWidth(Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getPhoneWindowHeight(Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static int getStatusBarHeight(Context context)
    {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static void initIndexLayout(Context context, int margin, final HashMap<String, Integer> selector,
                                       final ListView listView, final LinearLayout indexLayout, final TextView centralTextView)
    {
        final int height = (getPhoneWindowHeight(context) - dpToPixel(margin) - getStatusBarHeight(context)) / indexLetters.length;

        indexLayout.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height);
        for (String string : indexLetters)
        {
            TextView textView = new TextView(context);
            textView.setLayoutParams(params);
            textView.setTextColor(getColor(R.color.major_dark));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textView.setText(string);

            indexLayout.addView(textView);
            indexLayout.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View v, MotionEvent event)
                {
                    float y = event.getY();
                    int index = (int) (y / height);
                    if (index > -1 && index < ViewUtils.indexLetters.length)
                    {
                        String key = ViewUtils.indexLetters[index];
                        centralTextView.setVisibility(View.VISIBLE);
                        centralTextView.setText(key);
                        if (selector.containsKey(key))
                        {
                            int position = selector.get(key);
                            listView.setSelection(position + listView.getHeaderViewsCount());
                        }
                    }
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            indexLayout.setBackgroundColor(getColor(R.color.index_layout_pressed));
                            break;
                        case MotionEvent.ACTION_UP:
                            indexLayout.setBackgroundColor(getColor(android.R.color.transparent));
                            centralTextView.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    public static int dpToPixel(double dp)
    {
        DisplayMetrics metrics = ReimApplication.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }

    public static void goForward(Activity activity, Intent intent)
    {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.window_right_in, R.anim.window_left_out);
    }

    public static void goForwardForResult(Activity activity, Intent intent, int requestCode)
    {
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.window_right_in, R.anim.window_left_out);
    }

    public static void goForward(Activity activity, Class cls)
    {
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.window_right_in, R.anim.window_left_out);
    }

    public static void goForwardAndFinish(Activity activity, Intent intent)
    {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.window_right_in, R.anim.window_left_out);
        activity.finish();
    }

    public static void goForwardAndFinish(Activity activity, Class cls)
    {
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.window_right_in, R.anim.window_left_out);
        activity.finish();
    }

    public static void goBack(Activity activity)
    {
        activity.finish();
        activity.overridePendingTransition(R.anim.window_left_in, R.anim.window_right_out);
    }

    public static void goBackWithIntent(Activity activity, Intent intent)
    {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.window_left_in, R.anim.window_right_out);
        activity.finish();
    }

    public static void goBackWithIntent(Activity activity, Class cls)
    {
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.window_left_in, R.anim.window_right_out);
        activity.finish();
    }

    public static void goBackWithResult(Activity activity, Intent intent)
    {
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.window_left_in, R.anim.window_right_out);
    }
}