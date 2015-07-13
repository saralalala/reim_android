package netUtils.response.bank;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import netUtils.response.common.BaseResponse;

public class CreateBankAccountResponse extends BaseResponse
{
    private int accountID;

    public CreateBankAccountResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        try
        {
            JSONObject jObject = getDataObject();
            setAccountID(Integer.valueOf(jObject.getString("id")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public int getAccountID()
    {
        return accountID;
    }

    public void setAccountID(int accountID)
    {
        this.accountID = accountID;
    }
}
