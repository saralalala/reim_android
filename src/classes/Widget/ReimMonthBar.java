package classes.widget;

import classes.utils.PhoneUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ReimMonthBar extends View
{
	private Paint paint = new Paint();
	private RectF rect = new RectF();
	private double ratio;
	private int width;
	private int height = PhoneUtils.dpToPixel(getResources(), 18);
	private int radius = PhoneUtils.dpToPixel(getResources(), 2);
	
	public ReimMonthBar(Context context, double ratio)
	{
		super(context);
		
		this.ratio = ratio;
		
		paint.setColor(getColor(ratio));
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
	}

	public ReimMonthBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void onDraw(Canvas canvas)
	{
		width = canvas.getWidth();
		
		rect.left = 0;
		rect.top = 0;
		rect.right = (float) (width * ratio);
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