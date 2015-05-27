package classes.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.rushucloud.reim.R;

import classes.utils.ReimApplication;
import classes.utils.ViewUtils;

public class TabItem extends View
{
    private static final String INSTANCE_STATE = "instance_state";
    private static final String STATE_ALPHA = "state_alpha";

    private final int leftPadding = ViewUtils.dpToPixel(15);
    private final int rightPadding = ViewUtils.dpToPixel(15);
    private final int topPadding = ViewUtils.dpToPixel(6);
    private final int centralPadding = ViewUtils.dpToPixel(2);
    private final int bottomPadding = ViewUtils.dpToPixel(3);

    private final int defaultTextSize = ViewUtils.dpToPixel(10);

    private float alpha = 0;
    private Bitmap iconSelected;
    private Bitmap iconNotSelected;
    private Paint iconPaint = new Paint();
    private Rect iconRect = new Rect();
    private String text;
    private int textSize;
    private int textColorSelected;
    private int textColorNotSelected;
    private Paint textPaint = new Paint();
    private Rect textRect = new Rect();

    public TabItem(Context context)
    {
        super(context);
    }

    public TabItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TabItemView);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++)
        {
            int attr = array.getIndex(i);
            switch (attr)
            {
                case R.styleable.TabItemView_tab_icon_selected:
                {
                    BitmapDrawable drawable = (BitmapDrawable) array.getDrawable(attr);
                    iconSelected = drawable.getBitmap();
                    break;
                }
                case R.styleable.TabItemView_tab_icon_unselected:
                {
                    BitmapDrawable drawable = (BitmapDrawable) array.getDrawable(attr);
                    iconNotSelected = drawable.getBitmap().copy(Config.ARGB_8888, true);
                    break;
                }
                case R.styleable.TabItemView_text:
                {
                    text = array.getString(attr);
                    break;
                }
                case R.styleable.TabItemView_text_size:
                {
                    textSize = (int) array.getDimension(attr, defaultTextSize);
                    break;
                }
                case R.styleable.TabItemView_text_color_selected:
                {
                    textColorSelected = array.getColor(attr, getResources().getColor(R.color.tab_item_selected));
                    break;
                }
                case R.styleable.TabItemView_text_color_unselected:
                {
                    textColorNotSelected = array.getColor(attr, getResources().getColor(R.color.tab_item_unselected));
                    break;
                }
            }
        }
        array.recycle();

        textPaint.setTextSize(textSize);
        textPaint.setColor(textColorNotSelected);
        textPaint.setTypeface(ReimApplication.TypeFaceYaHei);
        textPaint.setAntiAlias(true);
        textPaint.getTextBounds(text, 0, text.length(), textRect);
    }

    protected Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putFloat(STATE_ALPHA, alpha);
        return bundle;
    }

    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            alpha = bundle.getFloat(STATE_ALPHA);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        }
        else
        {
            super.onRestoreInstanceState(state);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = getMeasuredWidth() - leftPadding - rightPadding;
        int maxHeight = getMeasuredHeight() - textRect.height() - topPadding - centralPadding - bottomPadding;

        if (maxWidth > maxHeight)
        {
            int sideLength = maxHeight;
            int left = getMeasuredWidth() / 2 - sideLength / 2;
            int top = topPadding;

            iconRect.set(left, top, left + sideLength, top + sideLength);
        }
        else
        {
            int sideLength = maxWidth;
            int left = getMeasuredWidth() / 2 - sideLength / 2;
            int top = topPadding + maxHeight / 2 - sideLength / 2;

            iconRect.set(left, top, left + sideLength, top + sideLength);
        }

        int textWidth = textRect.width();
        int textHeight = textRect.height();

        int left = getMeasuredWidth() / 2 - textWidth / 2;
        int top = getMeasuredHeight() - bottomPadding - textHeight;

        textRect.set(left, top, left + textWidth, top + textHeight);
    }

    protected void onDraw(Canvas canvas)
    {
        int tempAlpha = (int) Math.ceil(255 * alpha);
        drawIcons(canvas, tempAlpha);
        drawSrcText(canvas, tempAlpha);
        drawDstText(canvas, tempAlpha);
    }

    private void drawIcons(Canvas canvas, int alpha)
    {
        canvas.drawColor(Color.TRANSPARENT);

        iconPaint.setAntiAlias(true);
        iconPaint.setDither(true);
        iconPaint.setFilterBitmap(true);

        iconPaint.setAlpha(255 - alpha);
        canvas.drawBitmap(iconNotSelected, null, iconRect, iconPaint);

        iconPaint.setAlpha(alpha);
        canvas.drawBitmap(iconSelected, null, iconRect, iconPaint);
    }

    private void drawSrcText(Canvas canvas, int alpha)
    {
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColorNotSelected);
        textPaint.setAlpha(255 - alpha);

        canvas.drawText(text, textRect.left, textRect.bottom, textPaint);
    }

    private void drawDstText(Canvas canvas, int alpha)
    {
        textPaint.setColor(textColorSelected);
        textPaint.setAlpha(alpha);

        canvas.drawText(text, textRect.left, textRect.bottom, textPaint);
    }

    private void invalidateView()
    {
        if (Looper.getMainLooper() == Looper.myLooper())
        {
            invalidate();
        }
        else
        {
            postInvalidate();
        }
    }

    public void setSelectedIcon(int resId)
    {
        this.iconSelected = BitmapFactory.decodeResource(getResources(), resId);
        if (iconRect != null)
        {
            invalidateView();
        }
    }

    public void setSelectedIcon(Bitmap iconBitmap)
    {
        this.iconSelected = iconBitmap;
        if (iconRect != null)
        {
            invalidateView();
        }
    }

    public void setNotSelectedIcon(int resId)
    {
        this.iconNotSelected = BitmapFactory.decodeResource(getResources(), resId);
        if (iconRect != null)
        {
            invalidateView();
        }
    }

    public void setNotSelectedIcon(Bitmap iconBitmap)
    {
        this.iconNotSelected = iconBitmap;
        if (iconRect != null)
        {
            invalidateView();
        }
    }

    public void setIconAlpha(float alpha)
    {
        this.alpha = alpha;
        if (iconRect != null)
        {
            invalidateView();
        }
    }
}