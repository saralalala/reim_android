package netUtils.response;

import android.content.Intent;

import com.rushucloud.reim.start.SignInActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.utils.AppPreference;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import netUtils.NetworkConstant;

public abstract class BaseResponse
{
	private boolean status = false;
	private int code = -1;
	private String errorMessage = "";
	private String serverToken = "";
	private JSONObject dataObject = null;
	private JSONArray dataArray = null;
	private boolean hasPassword = true;

	public BaseResponse(Object httpResponse)
	{
		try
		{
			String string = (String)httpResponse;
			JSONObject object = new JSONObject(string);
			status = object.getInt("status") > 0;
			code = object.getInt("code");
			serverToken = object.getString("server_token");
			if (status)
			{
				dataObject = object.optJSONObject("data");
				if (dataObject == null)
				{
					dataArray = object.getJSONArray("data");
				}
				hasPassword = object.getBoolean("wx");
				constructData();
			}
			else 
			{
				dataObject = object.optJSONObject("data");
				if (dataObject == null)
				{
					dataArray = object.getJSONArray("data");
				}
				else
				{
					errorMessage = dataObject.getString("msg");					
				}
				
				if (code == NetworkConstant.ERROR_AUTH_TIMEOUT)
				{
                    AppPreference appPreference = AppPreference.getAppPreference();
                    String username = appPreference.getUsername();
                    String password = appPreference.getPassword();
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.saveAppPreference();

					Intent intent = new Intent(ReimApplication.getContext(), SignInActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					intent.putExtra("username", username);
					intent.putExtra("password", password);
					ReimApplication.getContext().startActivity(intent);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			status = false;
			errorMessage = NetworkConstant.errorCodeToString(code);
		}
	}

	protected abstract void constructData();
	
	public boolean getStatus()
	{
		return status;
	}

	public void setStatus(boolean status)
	{
		this.status = status;
	}

	public int getCode()
	{
		return code;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	public String getServerToken()
	{
		return serverToken;
	}
	
	protected JSONObject getDataObject()
	{
		return dataObject;
	}

	public JSONArray getDataArray()
	{
		return dataArray;
	}

	public boolean hasPassword()
	{
		return hasPassword;
	}
}