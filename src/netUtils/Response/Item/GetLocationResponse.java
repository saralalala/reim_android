package netUtils.Response.Item;

import org.json.JSONObject;

public class GetLocationResponse
{
	private boolean status;
	private String city;
	
	public GetLocationResponse(Object httpResponse)
	{
		try
		{
			String response = new String(((String)httpResponse).getBytes("GBK"), "UTF-8");
			JSONObject jObject = new JSONObject(response);
			status = jObject.getInt("status") == 0 ? true : false;
			city = jObject.getJSONObject("result").getJSONObject("addressComponent").getString("city");			
		}
		catch (Exception e)
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
