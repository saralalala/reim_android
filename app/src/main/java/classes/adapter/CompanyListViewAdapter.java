package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Group;
import classes.utils.Utils;
import classes.utils.ViewUtils;

public class CompanyListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Group> companyList;
    private Group company;
    private boolean hasInit = false;

    public CompanyListViewAdapter(Context context, List<Group> companies, Group company)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.companyList = new ArrayList<>(companies);
        this.company = company;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_company, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int color = company != null && !companyList.isEmpty() && companyList.get(position).equals(company) ?
                R.color.list_item_pressed : R.color.list_item_unpressed;
        convertView.setBackgroundResource(color);

        if (companyList.isEmpty())
        {
            convertView.setClickable(true);
            viewHolder.iconImageView.setImageResource(R.drawable.info);
            viewHolder.nameTextView.setText(R.string.search_result_empty);
            viewHolder.timeTextView.setVisibility(View.GONE);
        }
        else
        {
            convertView.setClickable(false);
            Group company = companyList.get(position);
            viewHolder.iconImageView.setImageResource(R.drawable.default_company_icon);
            viewHolder.nameTextView.setText(company.getName());
            viewHolder.timeTextView.setText(Utils.secondToStringUpToDay(company.getCreatedDate()) + ViewUtils.getString(R.string.create));
            viewHolder.timeTextView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public int getCount()
    {
        if (!companyList.isEmpty())
        {
            return companyList.size();
        }
        else if (hasInit)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public Object getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setCompanyList(List<Group> companies)
    {
        companyList.clear();
        companyList.addAll(companies);
    }

    public void setCompany(Group company)
    {
        this.company = company;
    }

    public void setHasInit(boolean hasInit)
    {
        this.hasInit = hasInit;
    }

    private static class ViewHolder
    {
        ImageView iconImageView;
        TextView nameTextView;
        TextView timeTextView;
    }
}