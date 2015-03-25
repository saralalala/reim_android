package netUtils.response.user;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

public class VerifyCodeResponse extends BaseResponse
{
	private String verifyCode;
	
	public VerifyCodeResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setVerifyCode(jObject.getString("code"));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}

	public String getVerifyCode()
	{
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode)
	{
		this.verifyCode = verifyCode;
	}
}
