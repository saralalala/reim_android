package classes.Adapter;

import classes.AppPreference;
import classes.User;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.ProfileActivity;
import database.DBManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileListViewAdapater extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private ProfileActivity activity;
	private AppPreference appPreference;
	private DBManager dbManager;
	private User user;
	
	public ProfileListViewAdapater(Context context, User user)
	{
		this.layoutInflater = LayoutInflater.from(context);
		this.activity = (ProfileActivity)context;
		this.appPreference = AppPreference.getAppPreference();
		this.dbManager = DBManager.getDBManager();	
		this.user = user;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;
		switch (position)
		{
			case 0:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.email));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(user.getEmail());
				editText.setHint(activity.getString(R.string.inputEmail));
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.phone));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(user.getPhone());
				editText.setHint(activity.getString(R.string.inputPhone));
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.nickname));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(user.getNickname());
				editText.setHint(activity.getString(R.string.inputNickname));
				break;
			}
			case 3:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.companyName));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				if (appPreference.getCurrentGroupID() != -1)
				{
					editText.setText(dbManager.getGroup(appPreference.getCurrentGroupID()).getName());					
				}
				editText.setHint(activity.getString(R.string.inputCompanyName));
				if (!user.isAdmin())
				{
					editText.setEnabled(false);
				}
				break;
			}
			case 4:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.changePassword));
				break;
			}
			case 5:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.defaultManager));
				break;
			}
			case 6:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.categoryManagement));
				break;
			}
			case 7:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView)view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.tagManagement));
				break;
			}
			default:
				break;
		}
		return view;
	}
	
	public int getCount()
	{
		return user.isAdmin() && user.getGroupID() != -1 ? 8 : 5;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return 0;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
}
