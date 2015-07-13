package classes.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import classes.utils.Utils;

public class Image
{
    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_INVOICE = 1;
    public static final int TYPE_ICON = 2;

    private int localID = 0;
    private int serverID = 0;
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
            setServerID(jObject.getInteger("id"));
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

        List<Integer> imagesIDs = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++)
        {
            imagesIDs.add(imageList.get(i).getServerID());
        }

        return Utils.intListToString(imagesIDs);
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
        return getServerID() <= 0;
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