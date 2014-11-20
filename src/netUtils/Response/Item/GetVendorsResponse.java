package netUtils.Response.Item;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetVendorsResponse
{
	private boolean status;
	private List<String> vendorList;
	
	public GetVendorsResponse(Object httpResponse)
	{
		try
		{
			JSONObject jObject = new JSONObject((String)httpResponse);
			status = jObject.getString("status").equals("OK") ? true : false;
			JSONArray jsonArray = jObject.getJSONArray("businesses");
			vendorList = new ArrayList<String>();

			int count = jsonArray.length();
			for (int i = 0; i < count; i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				vendorList.add(object.getString("name")); 
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			status = false;
			setVendorList(null);
		}
	}

	public boolean getStatus()
	{
		return status;
	}

	public void setStatus(boolean status)
	{
		this.status = status;
	}

	public List<String> getVendorList()
	{
		return vendorList;
	}

	public void setVendorList(List<String> vendorList)
	{
		this.vendorList = vendorList;
	}
}
