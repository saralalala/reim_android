package netUtils.request.bank;

import classes.model.BankAccount;
import netUtils.common.HttpConnectionCallback;
import netUtils.common.URLDef;
import netUtils.request.common.BaseRequest;

public class CreateBankAccountRequest extends BaseRequest
{
    public CreateBankAccountRequest(BankAccount bankAccount)
    {
        super();

        addParams("bank_name", bankAccount.getBankName());
        addParams("bank_location", bankAccount.getLocation());
        addParams("cardno", bankAccount.getNumber());
        addParams("account", bankAccount.getName());

        appendUrl(URLDef.URL_BANK);
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPost(callback);
    }
}
