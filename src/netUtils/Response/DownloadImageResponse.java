package netUtils.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}

}
