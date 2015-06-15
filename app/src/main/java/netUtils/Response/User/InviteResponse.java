package netUtils.response.user;

import org.json.JSONException;
import org.json.JSONObject;

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

            allInSameCompany = jObject.getInt("same_count") == jObject.getInt("total");
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