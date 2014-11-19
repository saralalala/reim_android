package netUtils.Request.Item;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import netUtils.HttpConnectionCallback;
import netUtils.Request.BaseRequest;

public class GetLocationRequest extends BaseRequest
{
	public GetLocationRequest(double latitude, double longitude)
	{
		String apiKey = "isVHfH894tfsLVOVdXlGdlKz";
		String location = Double.toString(latitude) + "," + Double.toString(longitude);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ak", apiKey));
		params.add(new BasicNameValuePair("location", location));
		params.add(new BasicNameValuePair("pois", "0"));
		params.add(new BasicNameValuePair("output", "json"));
		setParams(params);
		
		setUrl("http://api.map.baidu.com/geocoder/v2/");
	}

	public void sendRequest(HttpConnectionCallback callback)
	{
		doGet(callback);
	}
}