package classes;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

public class Vendor
{
	private String name = "";
	private String address = "";
	private int distance = -1;
	private int photoResID;
	private String photoURL;
	private Bitmap photo;
	
	public Vendor()
	{
		
	}
	
	public Vendor(String name)
	{
		this.name = name;
	}
	
	public Vendor(JSONObject jObject)
	{
		try
		{
			setName(jObject.getString("name"));
			setAddress(jObject.getString("address"));
			setDistance(jObject.getInt("distance"));
			setPhotoURL(jObject.getString("s_photo_url"));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}

	public int getDistance()
	{
		return distance;
	}
	public void setDistance(int distance)
	{
		this.distance = distance;
	}

	public int getPhotoResID()
	{
		return photoResID;
	}
	public void setPhotoResID(int photoResID)
	{
		this.photoResID = photoResID;
	}

	public String getPhotoURL()
	{
		return photoURL;
	}
	public void setPhotoURL(String photoURL)
	{
		this.photoURL = photoURL;
	}

	public Bitmap getPhoto()
	{
		return photo;
	}
	public void setPhoto(Bitmap photo)
	{
		this.photo = photo;
	}
}
