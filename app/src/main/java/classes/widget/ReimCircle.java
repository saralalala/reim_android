package classes.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import classes.utils.ViewUtils;

public class ReimCircle extends View
{
    private Paint paint = new Paint();
    private float cx;
    private float cy;
    private float radius;

    public ReimCircle(Context context, float circleWidth, int diameter, int color, double offset)
    {
        super(context);

        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(ViewUtils.dpToPixel(circleWidth));

        cx = diameter / 2;
        cy = diameter / 2;
        radius = diameter / 2 - ViewUtils.dpToPixel(offset + circleWidth / 2);
    }

    public ReimCircle(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void onDraw(Canvas canvas)
    {
        canvas.drawCircle(cx, cy, radius, paint);
    }
}