package netUtils.request.bank;

import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

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
