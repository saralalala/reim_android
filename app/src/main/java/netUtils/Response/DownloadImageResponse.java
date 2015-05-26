package netUtils.response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import com.rushucloud.reim.R;

import classes.utils.ViewUtils;

public class DownloadImageResponse extends BaseDownloadResponse
{
	private Bitmap bitmap;
	
	public DownloadImageResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getInputStream() != null)
		{
            bitmap = BitmapFactory.decodeStream(getInputStream());
		}
	}

	public Bitmap getBitmap()
	{
		return bitmap;
	}
}