package wxapi;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity
{
	public void onReq(BaseReq req)
	{
		super.onReq(req);
		System.out.println("onReq");
	}

	public void onResp(BaseResp resp)
	{
		super.onResp(resp);
		System.out.println("onResp");
	}	
}
