
package netUtils.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rushucloud.reim.start.SignInActivity;

import classes.AppPreference;
import classes.ReimApplication;

import android.content.Intent;

public abstract class BaseResponse
{
	private boolean status;
	private int code;
	private String errorMessage;
	private String serverToken;
	private JSONObject dataObject;
	private JSONArray dataArray;

	public BaseResponse(Object httpResponse)
	{
		try
		{
			String string = (String)httpResponse;
			JSONObject object = new JSONObject(string);
			status = object.getInt("status") > 0 ? true : false;
			code = object.getInt("code");
			serverToken = object.getString("server_token");
			if (status)
			{
				dataObject = object.optJSONObject("data");
				if (dataObject == null)
				{
					setDataArray(object.getJSONArray("data"));
				}
				constructData();
			}
			else 
			{
				dataObject = object.optJSONObject("data");
				if (dataObject == null)
				{
					setDataArray(object.optJSONArray("data"));
				}
				else
				{
					errorMessage = dataObject.getString("msg");					
				}
				
				if (code == -11)
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setCurrentUserID(-1);
					appPreference.setCurrentGroupID(-1);
					appPreference.setUsername("");
					appPreference.setPassword("");
					appPreference.setServerToken("");
					appPreference.setLastSyncTime(0);
					appPreference.saveAppPreference();
					
					ReimApplication.setTabIndex(0);
					ReimApplication.setReportTabIndex(0);
					
					Intent intent = new Intent(ReimApplication.getContext(), SignInActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					ReimApplication.getContext().startActivity(intent);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			status = false;
			code = -1;
			errorMessage = errorCodeToString(code);
			serverToken = "";
			dataObject = null;
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

	public void setDataArray(JSONArray dataArray)
	{
		this.dataArray = dataArray;
	}
	
	public static String errorCodeToString(int code)
	{
		String result = null;
		switch (code)
		{
		case -1:
			result = "系统故障";
			break;
		case -2:
			result = "用户尚未激活";
			break;
		case -3:
			result = "此账号不存在";
			break;
		case -4:
			result = "密码错误，验证失败";
			break;
		case -5:
			result = "邮件发送错误";
			break;
		case -6:
			result = "参数错误";
			break;
		case -7:
			result = "安全校验失败(没有用户信息)";
			break;
		case -8:
			result = "安全校验失败";
			break;
		case -9:
			result = "此用户已存在";
			break;
		case -10:
			result = "用户已在其他设备登录";
			break;
		case -11:
			result = "用户已在其他设备登录";
			break;
		case -12:
			result = "权限不足本次操作";
			break;
		default:
			break;
		}
		return result;
	}
}