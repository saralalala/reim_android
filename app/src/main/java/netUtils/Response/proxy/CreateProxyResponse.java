package netUtils.response.proxy;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;

import classes.model.Proxy;
import netUtils.response.common.BaseResponse;

public class CreateProxyResponse extends BaseResponse
{
    private List<Proxy> proxyList;

    public CreateProxyResponse(Object httpResponse)
    {
        super(httpResponse);
    }

    protected void constructData()
    {
        JSONArray jsonArray = getDataArray();
        proxyList = new ArrayList<>(Proxy.parse(jsonArray, false));
    }

    public List<Proxy> getProxyList()
    {
        return proxyList;
    }
}
