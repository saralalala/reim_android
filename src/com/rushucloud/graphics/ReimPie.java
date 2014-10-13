package com.rushucloud.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ReimPie extends View
{
	Paint paint = new Paint();
	Path pat = new Path();
	float _angle = 0;
	float _start = 270;

	public ReimPie(Context context, float angle, float start)
	{
		super(context);
		paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		this._angle = angle;
		this._start = start;
	}

	public ReimPie(Context con, AttributeSet atts)
	{
		super(con, atts);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		float width = 0;
		float height = 0;
		int cwidth = canvas.getWidth();
		int cheight = canvas.getHeight();
		int vertical = 0;
		RectF oval = new RectF(); // RectF对象
		float diff  = Math.abs(cwidth - cheight);
		if(cwidth > cheight){
			width = cheight;
			oval.left = diff / 2; // 左边
			oval.top = 0; // 上边
		} else {
			width = cwidth;
			oval.left = 0; // 左边
			oval.top = diff / 2; // 上边
			
		}
		
		oval.left = diff / 2; // 左边
		oval.top = diff / 2;; // 上边
		oval.right = width; // 右边
		oval.bottom = width; // 下边
		canvas.drawArc(oval, this._start, this._angle, false, paint); // 绘制圆弧
		
	}
}
