package classes.Adapter;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Group.ModifyGroupRequest;
import netUtils.Response.Group.ModifyGroupResponse;
import classes.AppPreference;
import classes.Group;
import classes.ReimApplication;
import classes.User;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.ProfileActivity;
import database.DBManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
		this.activity = (ProfileActivity) context;
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
				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.email));
				EditText editText = (EditText) view.findViewById(R.id.editText);
				editText.setText(user.getEmail());
				editText.setHint(activity.getString(R.string.inputEmail));
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.phone));
				EditText editText = (EditText) view.findViewById(R.id.editText);
				editText.setText(user.getPhone());
				editText.setHint(activity.getString(R.string.inputPhone));
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_item_edittext, null);
				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.nickname));
				EditText editText = (EditText) view.findViewById(R.id.editText);
				editText.setText(user.getNickname());
				editText.setHint(activity.getString(R.string.inputNickname));
				break;
			}
			case 3:
			{
				if (!user.isAdmin())
				{
					view = layoutInflater.inflate(R.layout.list_item_edittext, null);
					TextView textView = (TextView) view.findViewById(R.id.textView);
					textView.setText(activity.getString(R.string.companyName));

					EditText editText = (EditText) view.findViewById(R.id.editText);
					if (appPreference.getCurrentGroupID() != -1)
					{
						editText.setText(dbManager.getGroup(appPreference.getCurrentGroupID())
								.getName());
					}
					else
					{
						editText.setText(R.string.notAvailable);
					}
					editText.setEnabled(false);
				}
				else
				{
					final Group group = dbManager.getGroup(appPreference.getCurrentGroupID());
					view = layoutInflater.inflate(R.layout.list_item_text_button, null);
					TextView textView = (TextView) view.findViewById(R.id.textView);
					textView.setText(activity.getString(R.string.companyName));

					final EditText editText = (EditText) view.findViewById(R.id.editText);
					if (group != null)
					{
						editText.setText(group.getName());
					}
					editText.setHint(activity.getString(R.string.inputCompanyName));

					Button button = (Button) view.findViewById(R.id.button);
					button.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE); 
							imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); 
							
							String originalName = group.getName();
							final String newName = editText.getText().toString();
							if (newName.equals(originalName))
							{
								Toast.makeText(activity, "名称与原有相同，无需修改", Toast.LENGTH_SHORT).show();
							}
							else if (newName.equals(""))
							{
								Toast.makeText(activity, "新名称不可为空", Toast.LENGTH_SHORT).show();
							}
							else
							{
								ReimApplication.pDialog.show();
								ModifyGroupRequest request = new ModifyGroupRequest(newName);
								request.sendRequest(new HttpConnectionCallback()
								{
									public void execute(Object httpResponse)
									{
										ModifyGroupResponse response = new ModifyGroupResponse(
												httpResponse);
										if (response.getStatus())
										{
											group.setName(newName);
											dbManager.updateGroup(group);
											activity.runOnUiThread(new Runnable()
											{
												public void run()
												{
													ReimApplication.pDialog.dismiss();
													Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT)
															.show();
												}
											});
										}
										else
										{
											activity.runOnUiThread(new Runnable()
											{
												public void run()
												{
													ReimApplication.pDialog.dismiss();
													Toast.makeText(activity, "修改失败", Toast.LENGTH_SHORT)
															.show();
												}
											});
										}
									}
								});
							}
						}
					});
				}
				break;
			}
			case 4:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.changePassword));
				break;
			}
			case 5:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.defaultManager));
				break;
			}
			case 6:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setText(activity.getString(R.string.categoryManagement));
				break;
			}
			case 7:
			{
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
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
