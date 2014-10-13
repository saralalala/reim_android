package classes.Adapter;

import classes.AppPreference;
import classes.User;

import com.rushucloud.reim.ChangePasswordActivity;
import com.rushucloud.reim.ProfileActivity;
import com.rushucloud.reim.R;
import database.DBManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileListViewAdapater extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private ProfileActivity activity;
	private AppPreference appPreference;
	private DBManager dbManager;
	private User user;
	
	public ProfileListViewAdapater(Context context)
	{
		layoutInflater = LayoutInflater.from(context);
		activity = (ProfileActivity)context;
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		user = dbManager.getUser(appPreference.getCurrentUserID());				
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
				textView.setText(activity.getString(R.string.companyName));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(dbManager.getGroup(appPreference.getCurrentGroupID()).getName());
				editText.setHint(activity.getString(R.string.inputCompanyName));
				if (user.getPrivilege() == 0)
				{
					editText.setEnabled(false);
				}
				break;
			}
			case 3:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.email));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(user.getNickname());
				editText.setHint(activity.getString(R.string.inputNickname));
				break;
			}
			case 4:
			{
				view = layoutInflater.inflate(R.layout.list_item_button, null);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.changePassword));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						activity.startActivity(new Intent(activity.getBaseContext(), ChangePasswordActivity.class));
					}
				});
				break;
			}
			default:
				break;
		}
		return view;
	}
	
	public int getCount()
	{
		return 5;
	}

	public Object getItem(int position)
	{
		return null;
	}

	public long getItemId(int position)
	{
		return 0;
	}
}
