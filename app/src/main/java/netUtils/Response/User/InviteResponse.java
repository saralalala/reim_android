package netUtils.response.user;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import netUtils.response.common.BaseResponse;

public class InviteResponse extends BaseResponse
{
    private boolean allInSameCompany;

    public InviteResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();

            allInSameCompany = jObject.getInteger("same_count").equals(jObject.getInteger("total"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isAllInSameCompany()
    {
        return allInSameCompany;
    }
}