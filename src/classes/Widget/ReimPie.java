package classes.Widget;

import com.rushucloud.reim.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ReimPie extends View
{
	private Paint occupyPaint = new Paint();
	private Paint remainingPaint = new Paint();
	private RectF pieRect = new RectF();
	private float occupyAngle = 0;
	private float startAngle = 0;

	public ReimPie(Context context, float angle, float start, int canvasWidth, int canvasHeight)
	{
		super(context);
		occupyPaint.setColor(getResources().getColor(R.color.reim_pie_occupy));
		occupyPaint.setStyle(Style.FILL_AND_STROKE);
		remainingPaint.setColor(getResources().getColor(R.color.reim_pie_remain));
		remainingPaint.setStyle(Style.FILL_AND_STROKE);
		occupyAngle = angle;
		startAngle = start;
		pieRect.left = 0;
		pieRect.top = 0;
		pieRect.right = canvasWidth;
		pieRect.bottom = canvasHeight;
	}

	public ReimPie(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void onDraw(Canvas canvas)
	{
		float width = pieRect.right;
		float height = pieRect.bottom;
		float diff  = Math.abs(width - height);
		
		if(width > height)
		{
			pieRect.left += diff / 2 + 10; // 左边
			pieRect.top = 10; // 上边
			pieRect.right -= diff / 2 + 10; // 右边
			pieRect.bottom = height - 10; // 下边
		} 
		else
		{
			pieRect.left = 10; // 左边
			pieRect.top += diff / 2 + 10; // 上边
			pieRect.right = width - 10; // 右边
			pieRect.bottom -= diff / 2 + 10; // 下边	
		}

		occupyPaint.setAntiAlias(true);
		canvas.drawArc(pieRect, startAngle, occupyAngle, true, occupyPaint); // 绘制圆弧

		remainingPaint.setAntiAlias(true);
		canvas.drawArc(pieRect, startAngle + occupyAngle, 360 - occupyAngle, true, remainingPaint); // 绘制圆弧
	}
	
	public void setPieRect(float angle, float start, int canvasWidth, int canvasHeight)
	{
		occupyAngle = angle;
		startAngle = start;
		pieRect.left = 0;
		pieRect.top = 0;
		pieRect.right = canvasWidth;
		pieRect.bottom = canvasHeight;
	}
}