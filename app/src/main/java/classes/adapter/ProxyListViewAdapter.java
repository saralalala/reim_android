package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Proxy;

public class ProxyListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Proxy> proxyList;
    private List<Proxy> chosenList;

    public ProxyListViewAdapter(Context context, List<Proxy> proxies, List<Proxy> chosens)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.proxyList = new ArrayList<>(proxies);
        this.chosenList = new ArrayList<>();
        if (chosens != null)
        {
            chosenList.addAll(chosens);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_proxy, parent, false);
        }

        Proxy proxy = this.getItem(position);

        int color = chosenList.contains(proxy) ? R.color.list_item_pressed : R.color.list_item_unpressed;
        convertView.setBackgroundResource(color);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView scopeTextView = (TextView) convertView.findViewById(R.id.scopeTextView);

        nameTextView.setText(proxy.getUser().getNickname());
        scopeTextView.setText(proxy.getPermissionString());

        return convertView;
    }

    public int getCount()
    {
        return proxyList.size();
    }

    public Proxy getItem(int position)
    {
        return proxyList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setProxyList(List<Proxy> proxies)
    {
        proxyList.clear();
        proxyList.addAll(proxies);
    }

    public void setChosenList(List<Proxy> chosenList)
    {
        this.chosenList = chosenList;
    }
}