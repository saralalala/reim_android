package netUtils.Request.Item;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetVendorsRequest extends BaseRequest
{
	public GetVendorsRequest(String category, String city, double latitude, double longitude)
	{
		String requestUrl = "http://api.dianping.com/v1/business/find_businesses?appkey=33331153" +
				"&sign=03756DED41775869F69E9AA72EBC43DA007795FA";
		requestUrl += "&category=" + category;
		requestUrl += "&city=" + city;
		requestUrl += "&latitude=" + latitude;
		requestUrl += "&longitude=" + longitude;
		requestUrl += "&sort=1&limit=20&offset_type=1&out_offset_type=1&platform=2*";
		setUrl(requestUrl);
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}
