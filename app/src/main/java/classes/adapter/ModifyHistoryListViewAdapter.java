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

import classes.model.ModifyHistory;
import classes.utils.ViewUtils;
import classes.widget.CircleImageView;

public class ModifyHistoryListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<ModifyHistory> historyList;

    public ModifyHistoryListViewAdapter(Context context, List<ModifyHistory> histories)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.historyList = new ArrayList<>(histories);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_modify_history, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.contentTextView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ModifyHistory history = historyList.get(position);

        ViewUtils.setImageViewBitmap(history.getUser(), viewHolder.avatarImageView);
        viewHolder.timeTextView.setText(history.getTime());
        viewHolder.contentTextView.setText(history.getContent());

        return convertView;
    }

    public int getCount()
    {
        return historyList.size();
    }

    public ModifyHistory getItem(int position)
    {
        return historyList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setCategory(List<ModifyHistory> histories)
    {
        historyList.clear();
        historyList.addAll(histories);
    }

    private static class ViewHolder
    {
        CircleImageView avatarImageView;
        TextView timeTextView;
        TextView contentTextView;
    }
}
