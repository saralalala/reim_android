package netUtils.response.bank;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.response.BaseResponse;

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
