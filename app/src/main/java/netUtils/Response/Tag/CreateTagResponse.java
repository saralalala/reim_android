package netUtils.response.tag;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import netUtils.response.common.BaseResponse;

public class CreateTagResponse extends BaseResponse
{
    private int tagID;

    public CreateTagResponse(Object httpResponse)
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
