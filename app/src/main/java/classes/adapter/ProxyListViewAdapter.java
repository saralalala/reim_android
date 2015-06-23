package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import classes.model.Category;
import classes.model.Item;
import classes.model.Proxy;
import classes.model.Tag;
import classes.model.User;
import classes.utils.ReimApplication;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class ProxyListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Proxy> proxyList;

    public ProxyListViewAdapter(Context context, List<Proxy> proxies)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.proxyList = new ArrayList<>(proxies);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_proxy, parent, false);
        }

        Proxy proxy = this.getItem(position);

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
}