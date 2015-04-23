package classes.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class Spotlight extends View
{
    private Canvas mCanvas = null;
    private Paint mPaint = null;
    private Bitmap bitmap = null;
    private RectF highLightRect;

    public Spotlight(Context context, int width, int height, float cx, float cy, float radius, int backgroundColor)
    {
        super(context);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setAlpha(0);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        mCanvas.drawColor(backgroundColor);

        highLightRect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
    }

    public Spotlight(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        mCanvas.drawArc(highLightRect, 0, 360, true, mPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}