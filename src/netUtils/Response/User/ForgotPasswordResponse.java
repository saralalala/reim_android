package netUtils.Response.User;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.Response.BaseResponse;

public class ForgotPasswordResponse extends BaseResponse
{
	private int verifyCodeID;
	private String verifyCode;
	
	public ForgotPasswordResponse(Object httpResponse)
	{
		super(httpResponse);
	}

	protected void constructData()
	{
		try
		{
			JSONObject jObject = getDataObject();
			setVerifyCodeID(Integer.valueOf(jObject.getString("cid")));
			setVerifyCode(jObject.getString("code"));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}		
	}

	public int getVerifyCodeID()
	{
		return verifyCodeID;
	}

	public void setVerifyCodeID(int verifyCodeID)
	{
		this.verifyCodeID = verifyCodeID;
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
