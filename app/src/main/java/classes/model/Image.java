package classes.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Image
{
    private int localID = -1;
    private int serverID = -1;
    private String serverPath = "";
    private String localPath = "";
    private int itemID = -1;

    public Image()
    {

    }

    public Image(JSONObject jObject)
    {
        try
        {
            setServerID(jObject.getInt("id"));
            setServerPath(jObject.getString("path"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
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

    public String getServerPath()
    {
        return serverPath;
    }
    public void setServerPath(String serverPath)
    {
        this.serverPath = serverPath;
    }

    public String getLocalPath()
    {
        return localPath;
    }
    public void setLocalPath(String localPath)
    {
        this.localPath = localPath;
    }

    public int getItemID()
    {
        return itemID;
    }
    public void setItemID(int itemID)
    {
        this.itemID = itemID;
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        return BitmapFactory.decodeFile(localPath, options);
    }

    public void deleteFile()
    {
        File file = new File(localPath);
        file.delete();
    }

    public boolean isNotDownloaded()
    {
        return getLocalPath().isEmpty() || getBitmap() == null;
    }

    public boolean isNotUploaded()
    {
        return getServerID() == -1;
    }

    public boolean equals(Object o)
    {
        if (o instanceof Image)
        {
            Image image = (Image) o;
            return image.getServerID() == this.getServerID();
        }
        return super.equals(o);
    }
}