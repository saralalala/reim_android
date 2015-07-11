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

import classes.model.Category;
import classes.model.ModifyHistory;
import classes.model.User;
import classes.utils.DBManager;
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
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_modify_history, parent, false);
        }

        ModifyHistory history = historyList.get(position);

        CircleImageView avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatarImageView);
        ViewUtils.setImageViewBitmap(history.getUser(), avatarImageView);

        TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
        timeTextView.setText(history.getTime());

        TextView contentTextView = (TextView) convertView.findViewById(R.id.contentTextView);
        contentTextView.setText(history.getContent());

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
}
