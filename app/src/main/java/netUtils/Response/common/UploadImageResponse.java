package netUtils.response.common;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class UploadImageResponse extends BaseResponse
{
    private int imageID;
    private String path;

    public UploadImageResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setImageID(Integer.valueOf(jObject.getString("id")));
            setPath(jObject.getString("url"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getImageID()
    {
        return imageID;
    }

    public void setImageID(int imageID)
    {
        this.imageID = imageID;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
