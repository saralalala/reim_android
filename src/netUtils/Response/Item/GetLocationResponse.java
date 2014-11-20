package netUtils.Response.Item;

import org.json.JSONException;
import org.json.JSONObject;

public class GetLocationResponse
{
	private boolean status;
	private String city;
	
	public GetLocationResponse(Object httpResponse)
	{
		try
		{
			JSONObject jObject = new JSONObject((String)httpResponse);
			status = jObject.getInt("status") == 0 ? true : false;
			city = jObject.getJSONObject("result").getJSONObject("addressComponent").getString("city");			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			status = false;
			city = "";
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


	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}
}
