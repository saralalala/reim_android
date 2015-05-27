package netUtils.response.tag;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class ModifyTagResponse extends BaseResponse
{
    private int tagID;

    public ModifyTagResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setTagID(Integer.valueOf(jObject.getString("tid")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getTagID()
    {
        return tagID;
    }

    public void setTagID(int tagID)
    {
        this.tagID = tagID;
    }
}
