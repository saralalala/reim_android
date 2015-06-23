package netUtils.response.proxy;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import classes.model.Proxy;
import netUtils.response.common.BaseResponse;

public class ModifyProxyResponse extends BaseResponse
{
    private List<Proxy> proxyList;

    public ModifyProxyResponse(Object httpResponse)
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
