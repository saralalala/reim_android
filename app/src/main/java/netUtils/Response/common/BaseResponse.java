package netUtils.response.common;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rushucloud.reim.start.SignInActivity;

import classes.utils.AppPreference;
import classes.utils.ReimApplication;
import netUtils.common.NetworkConstant;

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
            String string = (String) httpResponse;
            JSONObject object = JSON.parseObject(string);
            status = object.getInteger("status") > 0;
            code = object.getInteger("code");
            serverToken = object.getString("server_token");
            if (status)
            {
                Object obj = object.get("data");
                if (obj instanceof JSONObject)
                {
                    dataObject = (JSONObject) obj;
                }
                else
                {
                    dataArray = (JSONArray) obj;
                }
                hasPassword = object.getBoolean("wx");
                constructData();
            }
            else
            {
                dataObject = object.getJSONObject("data");
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
                    errorMessage = NetworkConstant.errorCodeToString(code);

                    AppPreference appPreference = AppPreference.getAppPreference();
                    String username = appPreference.getUsername();
                    String password = appPreference.getPassword();
                    appPreference.setUsername("");
                    appPreference.setPassword("");
                    appPreference.setProxyUserID(-1);
                    appPreference.saveAppPreference();

                    Intent intent = new Intent(ReimApplication.getContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    ReimApplication.resetTabIndices();
                    ReimApplication.getContext().startActivity(intent);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            status = false;
            code = NetworkConstant.ERROR_SYSTEM_ERROR;
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