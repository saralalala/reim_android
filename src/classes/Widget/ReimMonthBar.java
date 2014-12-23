package classes.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ReimMonthBar extends View
{
	private Paint paint = new Paint();
	private RectF rect = new RectF();
	private int width;
	private int height;
	private int radius;
	
	public ReimMonthBar(Context context, double ratio)
	{
		super(context);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		width = metrics.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 154, metrics);
		height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, metrics);
		radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
		
		paint.setColor(getColor(ratio));
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		
		rect.left = 0;
		rect.top = 0;
		rect.right = (float) (width * ratio);
		rect.bottom = height;
	}

	public ReimMonthBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void onDraw(Canvas canvas)
	{
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