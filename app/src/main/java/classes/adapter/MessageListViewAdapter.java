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

import classes.Invite;
import classes.utils.Utils;

public class MessageListViewAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private List<Invite> inviteList;
	
	public MessageListViewAdapter(Context context, List<Invite> invites)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.inviteList = new ArrayList<Invite>(invites);
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.list_message, parent, false);
		}

		TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
		TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
		
		Invite invite = inviteList.get(position);

		messageTextView.setText(invite.getMessage());
		dateTextView.setText(Utils.secondToStringUpToDay(invite.getUpdateTime()));
		
		return convertView;
	}
	
	public int getCount()
	{
		return inviteList.size();
	}

	public Invite getItem(int position)
	{
		return inviteList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}
	
	public void setMessages(List<Invite> invites)
	{
		inviteList.clear();
		inviteList.addAll(invites);
	}
}
