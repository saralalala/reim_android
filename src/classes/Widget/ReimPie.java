package classes.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ReimPie extends View
{
	private Paint paint = new Paint();
	private RectF pieRect = new RectF();
	private float occupyAngle = 0;
	private float startAngle = 0;
	private int offset;

	public ReimPie(Context context, float start, float angle, int diameter, int colorResID)
	{
		super(context);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
		
		paint.setColor(getResources().getColor(colorResID));
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		
		startAngle = start;
		occupyAngle = angle;
		
		pieRect.left = offset;
		pieRect.top = offset;
		pieRect.right = diameter - offset;
		pieRect.bottom = diameter - offset;
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