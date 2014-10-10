package netUtils.Response;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseResponse
{
	private Boolean status;
	private int code;
	private String errorMessage;
	private String serverToken;
	private JSONObject dataObject;

	public BaseResponse(Object httpResponse)
	{
		try
		{
			String string = (String)httpResponse;
			JSONObject object = new JSONObject(string);
			if (object.getInt("status") > 0)
			{
				status = true;
			}
			else
			{
				status = false;
			}
			code = object.getInt("code");
			serverToken = object.getString("server_token");
			if (status)
			{
				dataObject = object.getJSONObject("data");
			}
			else 
			{
				dataObject = object.getJSONObject("data");
				errorMessage = dataObject.getString("msg");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	protected abstract void constructData();
	
	public Boolean getStatus()
	{
		return status;
	}

	public int getCode()
	{
		return code;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	public String getServerToken()
	{
		return serverToken;
	}
	
	protected JSONObject getDataObject()
	{
		return dataObject;
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