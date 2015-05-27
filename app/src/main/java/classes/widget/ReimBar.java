package classes.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import classes.utils.ViewUtils;

public class ReimBar extends View
{
    private Paint paint = new Paint();
    private RectF rect = new RectF();
    private double ratio;
    private int height = ViewUtils.dpToPixel(18);
    private int radius = ViewUtils.dpToPixel(2);

    public ReimBar(Context context, double ratio)
    {
        super(context);

        this.ratio = ratio;

        paint.setColor(getColor(ratio));
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
    }

    public ReimBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void onDraw(Canvas canvas)
    {
        rect.left = 0;
        rect.top = 0;
        rect.right = (float) (canvas.getWidth() * ratio);
        rect.bottom = height;

        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    private int getColor(double ratio)
    {
        int red = (int) (225 - 154 * ratio);
        int green = (int) (236 - 73 * ratio);
        int blue = (int) (242 - 32 * ratio);
        return Color.rgb(red, green, blue);
    }
}