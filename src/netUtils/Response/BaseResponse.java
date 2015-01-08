
package netUtils.Response;

import netUtils.HttpConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rushucloud.reim.start.SignInActivity;

import classes.ReimApplication;
import classes.Utils.AppPreference;

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
			status = object.getInt("status") > 0;
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
			case HttpConstant.ERROR_SYSTEM_ERROR:
				result = "系统错误，请稍候尝试";
				break;
			case HttpConstant.ERROR_USER_NOT_EXISTS:
				result = "用户不存在或密码错误";
				break;
			case HttpConstant.ERROR_MAIL_SEND_ERROR:
				result = "邮件发送错误";
				break;
			case HttpConstant.ERROR_PARAMETER_ERROR:
				result = "参数错误";
				break;
			case HttpConstant.ERROR_EMPTY_HEADER:
				result = "非法请求";
				break;
			case HttpConstant.ERROR_AUTH_FAIL:
				result = "认证失败";
				break;
			case HttpConstant.ERROR_USER_EXISTS:
				result = "用户已经存在";
				break;
			case HttpConstant.ERROR_AUTH_TIMEOUT:
				result = "认证超时";
				break;
			case HttpConstant.ERROR_BAD_PERMISSION:
				result = "权限不足本次操作";
				break;
			case HttpConstant.ERROR_ALREAD_BOUND:
				result = "用户已绑定";
				break;
			case HttpConstant.ERROR_USER_AUTH_ERROR:
				result = "用户认证失败";
				break;
			case HttpConstant.ERROR_BAD_ITEMS:
				result = "条目信息不齐全，请重新填写";
				break;
			case HttpConstant.ERROR_EMPTY_BIND:
				result = "尚未绑定账号";
				break;
			case HttpConstant.ERROR_CLOSE_REPORT:
				result = "您提交的报告已经处于关闭状态";
				break;
			case HttpConstant.ERROR_EMPTY_CATEGORY:
				result = "没有选定分类";
				break;
			case HttpConstant.ERROR_ZERO_AMOUNT:
				result = "没有报销额度";
				break;
			case HttpConstant.ERROR_OLDER_COMPANY:
				result = "报销中有条目不属于当前公司";
				break;
			case HttpConstant.ERROR_EMPTY_REPORT:
				result = "报销不存在";
				break;
			case HttpConstant.ERROR_EMPTY_ITEMS:
				result = "没有提交项目";
				break;
			case HttpConstant.ERROR_ITEM_ADDED:
				result = "条目已加入报销";
				break;
			case HttpConstant.ERROR_REPORT_DELETED:
				result = "报告已被删除";
				break;
			case HttpConstant.ERROR_REPORT_NOT_EXISTS:
				result = "报告不存在";
				break;
			default:
				break;
		}
		return result;
	}
}