
package com.rushucloud.reim.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.rushucloud.reim.R;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WeChatUtils.getApi().handleIntent(getIntent(), this);
    }

    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        WeChatUtils.getApi().handleIntent(intent, this);
        finish();
    }

    public void onReq(BaseReq baseReq)
    {

    }

    public void onResp(BaseResp baseResp)
    {
        switch (baseResp.errCode)
        {
            case BaseResp.ErrCode.ERR_OK:
            {
                switch (baseResp.getType())
                {
                    case ConstantsAPI.COMMAND_SENDAUTH:
                    {
                        SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                        Toast.makeText(this, "resp:"+resp.code, Toast.LENGTH_SHORT).show();
//                        WeChatUtils.sendAccessTokenRequest(resp.code);
                        break;
                    }
                    default:
                        break;
                }
            }
            default:
                Toast.makeText(this, "type:"+baseResp.getType() + " code:"+baseResp.errCode, Toast.LENGTH_SHORT).show();
                ViewUtils.showToast(this, R.string.error_wechat_auth);
                break;
        }
        finish();
    }
}