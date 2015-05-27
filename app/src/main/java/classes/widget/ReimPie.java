package classes.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import classes.utils.ViewUtils;

public class ReimPie extends View
{
    private Paint paint = new Paint();
    private RectF pieRect = new RectF();
    private float occupyAngle = 0;
    private float startAngle = 0;

    public ReimPie(Context context, float start, float angle, int diameter, int color, double offset)
    {
        super(context);

        paint.setColor(color);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);

        startAngle = start;
        occupyAngle = angle;

        int pieOffset = ViewUtils.dpToPixel(offset);

        pieRect.left = pieOffset;
        pieRect.top = pieOffset;
        pieRect.right = diameter - pieOffset;
        pieRect.bottom = diameter - pieOffset;
    }

    public ReimPie(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void onDraw(Canvas canvas)
    {
        canvas.drawArc(pieRect, startAngle, occupyAngle, true, paint);
    }
}