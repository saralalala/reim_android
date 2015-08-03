package classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;

import java.util.ArrayList;
import java.util.List;

import classes.model.Report;

public class ReportTagGridViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private int[] backgrounds;
    private boolean[] check;

    public ReportTagGridViewAdapter(Context context)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.check = new boolean[5];
        for (int i = 0; i < 5; i++)
        {
            check[i] = false;
        }
        backgrounds = new int[]{R.drawable.tag_draft, R.drawable.tag_submitted, R.drawable.tag_approved,
                R.drawable.tag_rejected, R.drawable.tag_finished};
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.grid_report_tag, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.statusTextView);
            viewHolder.coverLayout = (RelativeLayout) convertView.findViewById(R.id.coverLayout);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.statusTextView.setText(Report.getStatusString(position));
        viewHolder.statusTextView.setBackgroundResource(backgrounds[position]);

        int visibility = check[position] ? View.VISIBLE : View.INVISIBLE;
        viewHolder.coverLayout.setVisibility(visibility);

        return convertView;
    }

    public int getCount()
    {
        return 5;
    }

    public Boolean getItem(int position)
    {
        return check[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setSelection(int position)
    {
        check[position] = !check[position];
    }

    public void setCheck(boolean[] checks)
    {
        System.arraycopy(checks, 0, check, 0, checks.length);
    }

    public List<Integer> getFilterStatusList()
    {
        List<Integer> filterStatusList = new ArrayList<>();
        for (int i = 0; i < check.length; i++)
        {
            if (check[i])
            {
                filterStatusList.add(i);
            }
        }
        return filterStatusList;
    }

    public boolean[] getCheckedTags()
    {
        return check;
    }

    private static class ViewHolder
    {
        TextView statusTextView;
        RelativeLayout coverLayout;
    }
}
