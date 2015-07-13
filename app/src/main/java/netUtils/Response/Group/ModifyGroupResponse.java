package netUtils.response.group;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import netUtils.response.common.BaseResponse;

public class ModifyGroupResponse extends BaseResponse
{
    private int groupID;

    public ModifyGroupResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setGroupID(Integer.valueOf(jObject.getString("id")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getGroupID()
    {
        return groupID;
    }

    public void setGroupID(int groupID)
    {
        this.groupID = groupID;
    }
}
