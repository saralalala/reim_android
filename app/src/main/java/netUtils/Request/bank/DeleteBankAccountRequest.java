package netUtils.request.bank;

import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class DeleteBankAccountRequest extends BaseRequest
{
	public DeleteBankAccountRequest(int accountID)
	{
		super();

		appendUrl(URLDef.URL_CATEGORY);
        appendUrl(accountID);
	}
	
	public void sendRequest(HttpConnectionCallback callback)
	{
		doDelete(callback);
	}
}
