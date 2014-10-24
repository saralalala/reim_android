package com.rushucloud.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ReimPie extends View
{
	private Paint occupyPaint = new Paint();
	private Paint remainingPaint = new Paint();
	private RectF oval = new RectF();
	private float _angle = 0;
	private float _start = 0;

	public ReimPie(Context context, float angle, float start, int canvasWidth, int canvasHeight)
	{
		super(context);
		occupyPaint.setColor(Color.rgb(46, 139, 87));
		occupyPaint.setStyle(Style.FILL_AND_STROKE);
		remainingPaint.setColor(Color.rgb(127, 255, 212));
		remainingPaint.setStyle(Style.FILL_AND_STROKE);
		this._angle = angle;
		this._start = start;
		oval.left = 0;
		oval.top = 0;
		oval.right = canvasWidth;
		oval.bottom = canvasHeight;		
	}

	public ReimPie(Context con, AttributeSet atts)
	{
		super(con, atts);
	}

	public void onDraw(Canvas canvas)
	{
		float width = oval.right;
		float height = oval.bottom;
		float diff  = Math.abs(width - height);
		
		if(width > height)
		{
			oval.left += diff / 2 + 10; // 左边
			oval.top = 10; // 上边
			oval.right -= diff / 2 + 10; // 右边
			oval.bottom = height - 10; // 下边
		} 
		else
		{
			oval.left = 10; // 左边
			oval.top += diff / 2 + 10; // 上边
			oval.right = width - 10; // 右边
			oval.bottom -= diff / 2 + 10; // 下边	
		}

		canvas.drawArc(oval, this._start, this._angle, true, occupyPaint); // 绘制圆弧
		canvas.drawArc(oval, this._start+this._angle, 360-this._angle, true, remainingPaint); // 绘制圆弧
	}
}