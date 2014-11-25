package classes.Adapter;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Group.ModifyGroupRequest;
import netUtils.Response.Group.ModifyGroupResponse;
import classes.AppPreference;
import classes.Group;
import classes.ReimApplication;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.R;
import com.rushucloud.reim.me.ProfileActivity;
import database.DBManager;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
				view = layoutInflater.inflate(R.layout.list_edittext, null);
				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.email));
				EditText editText = (EditText) view.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				editText.setText(user.getEmail());
				editText.setHint(activity.getString(R.string.inputEmail));
				break;
			}
			case 1:
			{
				view = layoutInflater.inflate(R.layout.list_edittext, null);
				TextView textView = (TextView) view.findViewById(R.id.textView);
				textView.setText(activity.getString(R.string.phone));
				EditText editText = (EditText) view.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_PHONE);
				editText.setText(user.getPhone());
				editText.setHint(activity.getString(R.string.inputPhone));
				break;
			}
			case 2:
			{
				view = layoutInflater.inflate(R.layout.list_edittext, null);
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
					view = layoutInflater.inflate(R.layout.list_edittext, null);
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
					view = layoutInflater.inflate(R.layout.list_text_button, null);
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
							if (!Utils.isNetworkConnected())
							{
								Utils.showToast(activity, "网络未连接，无法修改");			
							}
							else if (newName.equals(originalName))
							{
								Utils.showToast(activity, "名称与原有相同，无需修改");
							}
							else if (newName.equals(""))
							{
								Utils.showToast(activity, "新名称不可为空");
							}
							else
							{
								ReimApplication.showProgressDialog();
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
													ReimApplication.dismissProgressDialog();
													Utils.showToast(activity, "修改成功");
												}
											});
										}
										else
										{
											activity.runOnUiThread(new Runnable()
											{
												public void run()
												{
													ReimApplication.showProgressDialog();
													Utils.showToast(activity, "修改失败");
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
			default:
				break;
		}
		return view;
	}

	public int getCount()
	{
		return 4;
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
