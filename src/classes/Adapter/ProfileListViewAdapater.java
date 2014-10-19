package classes.Adapter;

import classes.AppPreference;
import classes.User;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.CategoryActivity;
import com.rushucloud.reim.me.ChangePasswordActivity;
import com.rushucloud.reim.me.ProfileActivity;
import com.rushucloud.reim.me.TagActivity;

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
	private User currentUser;
	
	public ProfileListViewAdapater(Context context)
	{
		layoutInflater = LayoutInflater.from(context);
		activity = (ProfileActivity)context;
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();	
		currentUser = dbManager.getUser(appPreference.getCurrentUserID());
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
				editText.setText(currentUser.getEmail());
				editText.setHint(activity.getString(R.string.inputEmail));
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.phone));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(currentUser.getPhone());
				editText.setHint(activity.getString(R.string.inputPhone));
				break;
			}
			case 2:
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
				if (!currentUser.isAdmin())
				{
					editText.setEnabled(false);
				}
				break;
			}
			case 3:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView)view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.nickname));
				EditText editText = (EditText)view.findViewById(R.id.editText);
				editText.setText(currentUser.getNickname());
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
			case 5:
			{
				view = layoutInflater.inflate(R.layout.list_item_button, null);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.defaultManager));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						
					}
				});
				break;
			}
			case 6:
			{
				view = layoutInflater.inflate(R.layout.list_item_button, null);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.categoryManagement));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						activity.startActivity(new Intent(activity.getBaseContext(), CategoryActivity.class));
					}
				});
				break;
			}
			case 7:
			{
				view = layoutInflater.inflate(R.layout.list_item_button, null);
				Button button = (Button)view.findViewById(R.id.button);
				button.setText(activity.getString(R.string.tagManagement));
				button.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						activity.startActivity(new Intent(activity.getBaseContext(), TagActivity.class));
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
		return currentUser.isAdmin() && currentUser.getGroupID() != -1 ? 8 : 5;
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
