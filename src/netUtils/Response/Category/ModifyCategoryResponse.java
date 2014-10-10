package netUtils.Response.Category;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class ModifyCategoryResponse extends BaseResponse
{
	private int categoryID;
	
	public ModifyCategoryResponse(Object httpResponse)
	{
		super(httpResponse);
		if (getStatus())
		{
			constructData();
		}
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
