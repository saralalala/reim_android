package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import classes.utils.CharacterParser;
import classes.utils.Constant;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;

public class LocationListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter
{
    private LayoutInflater layoutInflater;
    private View hotCityView;
    private List<String> cityList;
    private HashMap<String, Integer> selector = new HashMap<>();
    private ArrayList<Integer> indexList = new ArrayList<>();

    public LocationListViewAdapter(Context context, View hotCityView, List<String> cityList)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.hotCityView = hotCityView;
        this.cityList = new ArrayList<>(cityList);
        initData();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (position == 0)
        {
            View view = layoutInflater.inflate(R.layout.list_header, parent, false);
            TextView headerTextView = (TextView) view.findViewById(R.id.headerTextView);
            headerTextView.setText(R.string.hot_cities);
            return view;
        }
        else if (position == 1)
        {
            return hotCityView;

        }
        else if (indexList.contains(position))
        {
            HeaderViewHolder headerViewHolder;
            if(convertView == null || convertView.getTag() == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_header, parent, false);

                headerViewHolder = new HeaderViewHolder();
                headerViewHolder.headerTextView = (TextView) convertView.findViewById(R.id.headerTextView);

                convertView.setTag(headerViewHolder);
            }
            else
            {
                headerViewHolder = (HeaderViewHolder) convertView.getTag();
            }

            headerViewHolder.headerTextView.setText(cityList.get(position - 2));

            return convertView;
        }
        else
        {
            LocationViewHolder locationViewHolder;
            if(convertView == null || convertView.getTag() == null)
            {
                convertView = layoutInflater.inflate(R.layout.list_location, parent, false);
                locationViewHolder = new LocationViewHolder();
                locationViewHolder.locationTextView = (TextView) convertView.findViewById(R.id.locationTextView);
                convertView.setTag(locationViewHolder);
            }
            else
            {
                locationViewHolder = (LocationViewHolder) convertView.getTag();
            }

            locationViewHolder.locationTextView.setText(cityList.get(position - 2));

            return convertView;
        }
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

    public int getViewTypeCount()
    {
        return 2;
    }

    public int getItemViewType(int position)
    {
        return indexList.contains(position) ? Constant.VIEW_TYPE_HEADER : Constant.VIEW_TYPE_CONTENT;
    }

    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == 1;
    }

    private void initData()
    {
        TreeMap<String, ArrayList<String>> indexMap = new TreeMap<>(new Comparator<String>()
        {
            public int compare(String s, String s2)
            {
                if (s.equals(s2))
                {
                    return 0;
                }
                else if (s.equals("#"))
                {
                    return 1;
                }
                else if (s2.equals("#"))
                {
                    return -1;
                }
                else
                {
                    return s.compareTo(s2);
                }
            }
        });

        for (String city : cityList)
        {
            String initLetter = CharacterParser.getInitLetter(city);
            ArrayList<String> letterCityList = indexMap.get(initLetter);
            if (letterCityList == null)
            {
                letterCityList = new ArrayList<>();
            }
            letterCityList.add(city);
            indexMap.put(initLetter, letterCityList);
        }

        int count = 2;
        selector.clear();
        selector.put(ViewUtils.getString(R.string.hot), 0);
        indexList.add(0);
        cityList.clear();
        for (Map.Entry<String, ArrayList<String>> entry : indexMap.entrySet())
        {
            String key = entry.getKey();
            ArrayList<String> values = entry.getValue();
            selector.put(key, count);
            indexList.add(count);
            cityList.add(key);
            cityList.addAll(values);
            count += values.size() + 1;
        }
    }

    public List<String> getCityList()
    {
        return cityList;
    }

    public void setCityList(List<String> cityList)
    {
        this.cityList = cityList;
        initData();
    }

    public HashMap<String, Integer> getSelector()
    {
        return selector;
    }

    public boolean isLocation(int position)
    {
        return !indexList.contains(position);
    }

    private static class HeaderViewHolder
    {
        TextView headerTextView;
    }

    private static class LocationViewHolder
    {
        TextView locationTextView;
    }
}
