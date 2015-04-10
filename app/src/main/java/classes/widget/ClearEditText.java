package classes.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.rushucloud.reim.R;

import classes.utils.ViewUtils;

public class ClearEditText extends EditText implements OnFocusChangeListener, TextWatcher
{
    private Drawable mClearDrawable;
    private boolean hasFocus;

    public ClearEditText(Context context)
    {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (!isInEditMode())
        {
            init();
        }
    }

    public boolean hasFocus()
    {
        return hasFocus;
    }

    private void init()
    {
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null)
        {
            mClearDrawable = getResources().getDrawable(R.drawable.delete_drawable);
        }

        int sideLength = ViewUtils.dpToPixel(18);
        mClearDrawable.setBounds(0, 0, sideLength, sideLength);
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    public boolean onTouchEvent(@NonNull MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if (getCompoundDrawables()[2] != null)
            {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));

                if (touchable)
                {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void onFocusChange(View v, boolean hasFocus)
    {
        this.hasFocus = hasFocus;
        if (hasFocus)
        {
            setClearIconVisible(getText().length() > 0);
        }
        else
        {
            setClearIconVisible(false);
        }
        Spannable spanText = ((EditText) v).getText();
        Selection.setSelection(spanText, spanText.length());
    }

    public void setClearIconVisible(boolean visible)
    {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                             getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    public void onTextChanged(CharSequence s, int start, int count, int after)
    {
        if (hasFocus)
        {
            setClearIconVisible(s.length() > 0);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    public void afterTextChanged(Editable s)
    {

    }
}