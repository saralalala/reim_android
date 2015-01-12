package classes;

import java.io.File;
import java.util.List;

import classes.Utils.DBManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

public class Image
{
	private int localID = -1;
	private int serverID = -1;
	private String path = "";
	private int itemID = -1;
	
	public Image()
	{
		
	}
	
	public int getLocalID()
	{
		return localID;
	}
	public void setLocalID(int localID)
	{
		this.localID = localID;
	}
	
	public int getServerID()
	{
		return serverID;
	}
	public void setServerID(int serverID)
	{
		this.serverID = serverID;
	}
	
	public String getPath()
	{
		return path;
	}
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public int getItemID()
	{
		return itemID;
	}

	public void setItemID(int itemID)
	{
		this.itemID = itemID;
	}

	public static void deleteUnusedImage()
	{
		DBManager dbManager = DBManager.getDBManager();
		List<Image> imageList = dbManager.getUnusedImages();
		for (Image image : imageList)
		{
			image.deleteImage();
		}
		dbManager.deleteUnusedImages();
		
		imageList = dbManager.getOthersUnusedImages();
		for (Image image : imageList)
		{
			image.deleteImage();
		}
		dbManager.deleteOthersUnusedImages();
	}

	public static String getImagesIDString(List<Image> imageList)
	{
		if (imageList == null || imageList.isEmpty())
		{
			return "";
		}
		
		Integer[] imagesIDs = new Integer[imageList.size()];
		for (int i = 0; i < imageList.size(); i++)
		{
			imagesIDs[i] = imageList.get(i).getServerID();
		}
		
		return TextUtils.join(",", imagesIDs);
	}
		
	public Bitmap getBitmap()
	{
		return BitmapFactory.decodeFile(path);
	}
	
	public void deleteImage()
	{
		File file = new File(path);
		if (file != null)
		{
			file.delete();			
		}
	}
	
	public boolean isDownloaded()
	{
		return getBitmap() != null;
	}
}