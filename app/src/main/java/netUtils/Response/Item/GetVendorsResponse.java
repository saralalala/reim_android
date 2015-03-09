package netUtils.Response.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import classes.Vendor;

public class GetVendorsResponse
{
	private boolean status;
	private List<Vendor> vendorList;
	
	public GetVendorsResponse(Object httpResponse)
	{
		try
		{
			JSONObject jObject = new JSONObject((String)httpResponse);
			status = jObject.getString("status").equals("OK");
			JSONArray jsonArray = jObject.getJSONArray("businesses");
			vendorList = new ArrayList<Vendor>();

			int count = jsonArray.length();
			for (int i = 0; i < count; i++)
			{
				vendorList.add(new Vendor(jsonArray.getJSONObject(i))); 
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

	public List<Vendor> getVendorList()
	{
		return vendorList;
	}

	public void setVendorList(List<Vendor> vendorList)
	{
		this.vendorList = vendorList;
	}
}