package netUtils.response.category;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.common.BaseResponse;

public class DeleteCategoryResponse extends BaseResponse
{
    private int categoryID;

    public DeleteCategoryResponse(Object httpResponse)
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
