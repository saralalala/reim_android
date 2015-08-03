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

import classes.model.Message;
import classes.utils.Utils;

public class MessageListViewAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;
    private List<Message> messageList;

    public MessageListViewAdapter(Context context, List<Message> messages)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.messageList = new ArrayList<>(messages);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.list_message, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            viewHolder.tipImageView = (ImageView) convertView.findViewById(R.id.tipImageView);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Message message = messageList.get(position);

        viewHolder.messageTextView.setText(message.getTitle());
        viewHolder.dateTextView.setText(Utils.secondToStringUpToDay(message.getUpdateTime()));

        int visibility = message.hasBeenRead() ? View.GONE : View.VISIBLE;
        viewHolder.tipImageView.setVisibility(visibility);

        return convertView;
    }

    public int getCount()
    {
        return messageList.size();
    }

    public Message getItem(int position)
    {
        return messageList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void setMessages(List<Message> messages)
    {
        messageList.clear();
        messageList.addAll(messages);
    }

    private static class ViewHolder
    {
        TextView messageTextView ;
        TextView dateTextView ;
        ImageView tipImageView ;
    }
}
