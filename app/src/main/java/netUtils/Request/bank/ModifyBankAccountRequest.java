package netUtils.request.bank;

import classes.model.BankAccount;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class ModifyBankAccountRequest extends BaseRequest
{
    public ModifyBankAccountRequest(BankAccount bankAccount)
    {
        super();

        addParams("bank_name", bankAccount.getBankName());
        addParams("bank_location", bankAccount.getLocation());
        addParams("cardno", bankAccount.getNumber());
        addParams("account", bankAccount.getName());

        appendUrl(URLDef.URL_BANK);
        appendUrl(bankAccount.getServerID());
    }

    public void sendRequest(HttpConnectionCallback callback)
    {
        doPut(callback);
    }
}
