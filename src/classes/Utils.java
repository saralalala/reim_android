package classes;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils
{	
	public static String getPathFromUri(Activity activity, Uri uri)
	{
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		return cursor.getString(index);
	}
}
