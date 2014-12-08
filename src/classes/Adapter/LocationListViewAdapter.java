package classes.Adapter;

import java.util.Arrays;
import java.util.List;

import com.rushucloud.reim.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LocationListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<String> cityList;
	private String currentCity;
	private boolean[] check;
	private int selectedColor;
	private int unselectedColor;
	
	public LocationListViewAdapter(Context context, String city)
	{
		layoutInflater = LayoutInflater.from(context);
		currentCity = context.getString(R.string.locationInvalid);
		selectedColor = context.getResources().getColor(R.color.major_dark);
		unselectedColor = context.getResources().getColor(R.color.font_major_dark);

		cityList = Arrays.asList(context.getResources().getStringArray(R.array.cityArray));
		check = new boolean[cityList.size()];
		for (int i = 0; i < cityList.size(); i++)
		{
			check[i] = city.equals(cityList.get(i));
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		switch (position)
		{
			case 0:
			{
				view = layoutInflater.inflate(R.layout.list_current_location, parent, false);
				
				TextView locationTextView = (TextView) view.findViewById(R.id.locationTextView);
				locationTextView.setText(currentCity);
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_header, parent, false);
				
				TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
				headerTextView.setText(R.string.allCities);				
				break;
			}
			default:
			{
				view = layoutInflater.inflate(R.layout.list_location, parent, false);
				
				TextView locationTextView = (TextView) view.findViewById(R.id.locationTextView);
				locationTextView.setText(cityList.get(position - 2));

				int color = check[position - 2] ? selectedColor : unselectedColor;
				locationTextView.setTextColor(color);
				break;
			}
		}
		return view;
	}
	
	public int getCount()
	{
		return cityList.size() + 2;
	}

	public String getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public String getCurrentCity()
	{
		return currentCity;
	}
	
	public void setCurrentCity(String city)
	{
		currentCity = city;
	}
	
	public boolean[] getCheck()
	{
		return check;
	}
	
	public void setCheck(boolean[] checkList)
	{
		check = checkList;
	}
	
	public List<String> getCityList()
	{
		return cityList;
	}
}
