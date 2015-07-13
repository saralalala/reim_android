package netUtils.response.category;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import netUtils.response.common.BaseResponse;

public class CreateCategoryResponse extends BaseResponse
{
    private int categoryID;

    public CreateCategoryResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setCategoryID(Integer.valueOf(jObject.getString("id")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getCategoryID()
    {
        return categoryID;
    }

    public void setCategoryID(int categoryID)
    {
        this.categoryID = categoryID;
    }
}
