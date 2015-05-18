package netUtils.request.bank;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import classes.model.BankAccount;
import classes.model.Category;
import netUtils.HttpConnectionCallback;
import netUtils.URLDef;
import netUtils.request.BaseRequest;

public class CreateBankAccountRequest extends BaseRequest
{
    public CreateBankAccountRequest(BankAccount bankAccount)
	{
		super();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("bank_name", bankAccount.getBankName()));
		params.add(new BasicNameValuePair("bank_location", bankAccount.getLocation()));
		params.add(new BasicNameValuePair("cardno", bankAccount.getNumber()));
		params.add(new BasicNameValuePair("account", bankAccount.getName()));
		setParams(params);

		appendUrl(URLDef.URL_BANK);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doPost(callback);
	}
}
