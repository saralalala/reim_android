package netUtils.response.common;

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
}