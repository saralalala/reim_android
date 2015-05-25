package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import classes.adapter.ContactListViewAdapter;
import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.group.CreateGroupRequest;
import netUtils.response.group.CreateGroupResponse;

public class ContactActivity extends Activity
{
    private static final int INPUT_CONTACT = 0;

    private ContactListViewAdapter adapter;
    private PinnedSectionListView contactListView;
    private LinearLayout indexLayout;
    private TextView centralTextView;

    private AppPreference appPreference;
    private DBManager dbManager;
    private String companyName;
    private ArrayList<String> inputList = new ArrayList<>();
    private ArrayList<String> inputChosenList = new ArrayList<>();
    private List<User> contactList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();
    private boolean hasInit = false;

    public static String[] indexLetters = {"手动", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_contact);
		initData();
		initView();
        readContacts();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ContactActivity");
		MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        if (contactList.isEmpty() && hasInit)
        {
            readContacts();
        }
        hasInit = true;
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ContactActivity");
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
            goBack();
		}
		return super.onKeyDown(keyCode, event);
	}

    @SuppressWarnings("unchecked")
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case INPUT_CONTACT:
                {
                    inputList.clear();
                    inputList.addAll((ArrayList<String>) data.getSerializableExtra("inputList"));
                    inputChosenList.clear();
                    inputChosenList.addAll((ArrayList<String>) data.getSerializableExtra("inputList"));
                    adapter.setInputList(inputList);
                    adapter.setInputChosenList(inputChosenList);
                    adapter.setContactList(contactList);
                    adapter.initIndex();
                    adapter.notifyDataSetChanged();
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unchecked")
	private void initData()
	{
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
        
        Bundle bundle = getIntent().getExtras();
        companyName = bundle.getString("companyName", "");
        inputList = bundle.getStringArrayList("inputList");
        inputChosenList = bundle.getStringArrayList("inputChosenList");
        contactChosenList = (List<User>) bundle.getSerializable("contactChosenList");
	}
    
	private void initView()
	{
        ReimProgressDialog.setContext(this);

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});

		TextView nextTextView = (TextView) findViewById(R.id.nextTextView);
        nextTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString("companyName", companyName);
                bundle.putStringArrayList("inputList", inputList);
                bundle.putStringArrayList("inputChosenList", inputChosenList);
                bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
                Intent intent = new Intent(ContactActivity.this, ContactActivity.class);
                intent.putExtras(bundle);
                ViewUtils.goForwardAndFinish(ContactActivity.this, intent);


                String inviteList = "";
                for (String contact : inputChosenList)
                {
                    inviteList += contact + ",";
                }
                for (User user : contactChosenList)
                {
                    inviteList += user.getContact() + ",";
                }
                if (!inviteList.isEmpty())
                {
                    inviteList = inviteList.substring(0, inviteList.length() - 1);
                }

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ContactActivity.this, R.string.error_create_network_unavailable);
                }
                else
                {
                    sendCreateGroupRequest(inviteList, inputChosenList.size() + contactChosenList.size());
                }
            }
        });

        adapter = new ContactListViewAdapter(this);
        adapter.setInputList(inputList);
        adapter.setInputChosenList(inputChosenList);
        adapter.setContactList(contactList);
        adapter.setContactChosenList(contactChosenList);
        adapter.initIndex();

        contactListView = (PinnedSectionListView) findViewById(R.id.contactListView);
        contactListView.setAdapter(adapter);
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                {
                    String inviteList = "";
                    for (String contact : inputList)
                    {
                        if (inputChosenList.contains(contact))
                        {
                            inviteList += contact + ", ";
                        }
                    }
                    if (!inviteList.isEmpty())
                    {
                        inviteList = inviteList.substring(0, inviteList.length() - 2);
                    }
                    Intent intent = new Intent(ContactActivity.this, InputContactActivity.class);
                    intent.putExtra("inviteList", inviteList);
                    ViewUtils.goForwardForResult(ContactActivity.this, intent, INPUT_CONTACT);
                }
                else if (position > 0 && position < inputList.size() + 1)
                {
                    String contact = inputList.get(position - 1);
                    if (inputChosenList.contains(contact))
                    {
                        inputChosenList.remove(contact);
                    }
                    else
                    {
                        inputChosenList.add(contact);
                    }
                    adapter.setInputChosenList(inputChosenList);
                    adapter.notifyDataSetChanged();
                }
                else if (position > inputList.size() + 1 && !contactList.isEmpty() && adapter.isContact(position))
                {
                    User user = adapter.getItem(position);
                    int index = User.indexOfContactList(contactChosenList, user);
                    if (index > -1)
                    {
                        contactChosenList.remove(index);
                    }
                    else
                    {
                        contactChosenList.add(user);
                    }
                    adapter.setContactChosenList(contactChosenList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        indexLayout = (LinearLayout) this.findViewById(R.id.indexLayout);
        centralTextView = (TextView) findViewById(R.id.centralTextView);
	}

    public void initIndexLayout()
    {
        final int height = (ViewUtils.getPhoneWindowHeight(this) - ViewUtils.dpToPixel(50) - ViewUtils.getStatusBarHeight(this)) / indexLetters.length;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height);
        for (String string : indexLetters)
        {
            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setTextColor(ViewUtils.getColor(R.color.major_dark));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textView.setText(string);

            indexLayout.addView(textView);
            indexLayout.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View v, MotionEvent event)
                {
                    float y = event.getY();
                    int index = (int) (y / height);
                    if (index > -1 && index < indexLetters.length)
                    {
                        String key = indexLetters[index];
                        centralTextView.setVisibility(View.VISIBLE);
                        centralTextView.setText(key);
                        int fontSize = index == 0? 24 : 30;
                        centralTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                        if (adapter.getSelector().containsKey(key))
                        {
                            int position = adapter.getSelector().get(key);
                            contactListView.setSelection(position + contactListView.getHeaderViewsCount());
                        }
                    }
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            indexLayout.setBackgroundColor(ViewUtils.getColor(R.color.index_layout_selected));
                            break;
                        case MotionEvent.ACTION_UP:
                            indexLayout.setBackgroundColor(ViewUtils.getColor(android.R.color.transparent));
                            centralTextView.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void readContacts()
    {
        contactList.clear();
        ReimProgressDialog.show();

        new Thread(new Runnable()
        {
            public void run()
            {
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "sort_key_alt asc");

                while (cursor.moveToNext())
                {
                    String ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phone;
                    String email;
                    int id = Integer.parseInt(ID);
                    if (id > 0)
                    {
                        Cursor c = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ID, null, null);

                        while (c.moveToNext())
                        {
                            phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            User user = new User();
                            user.setNickname(name);
                            user.setPhone(phone);
                            contactList.add(user);
                        }
                        c.close();

                        c = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                           ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + ID, null, null);

                        while (c.moveToNext())
                        {
                            email = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                            User user = new User();
                            user.setNickname(name);
                            user.setEmail(email);
                            contactList.add(user);
                        }
                        c.close();
                    }
                }
                cursor.close();

                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        adapter.setContactList(contactList);
                        adapter.setNoPermission(contactList.isEmpty());
                        adapter.initIndex();
                        adapter.notifyDataSetChanged();

                        if (!contactList.isEmpty())
                        {
                            initIndexLayout();
                        }

                        ReimProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void sendCreateGroupRequest(String inviteList, final int count)
    {
        ReimProgressDialog.show();
        CreateGroupRequest request = new CreateGroupRequest(companyName, inviteList, 1);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateGroupResponse response = new CreateGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Group group = new Group();
                    group.setName(companyName);
                    group.setServerID(response.getGroupID());
                    group.setLocalUpdatedDate(response.getDate());
                    group.setServerUpdatedDate(response.getDate());

                    User currentUser = appPreference.getCurrentUser();
                    currentUser.setGroupID(group.getServerID());
                    currentUser.setIsAdmin(true);

                    dbManager.insertGroup(group);
                    dbManager.updateUser(currentUser);
                    appPreference.setCurrentGroupID(group.getServerID());
                    appPreference.saveAppPreference();

                    int currentGroupID = response.getGroup().getServerID();

                    // update AppPreference
                    AppPreference appPreference = AppPreference.getAppPreference();
                    appPreference.setCurrentGroupID(currentGroupID);
                    appPreference.saveAppPreference();

                    // update members
                    DBManager dbManager = DBManager.getDBManager();
                    currentUser = response.getCurrentUser();
                    User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
                    if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
                    {
                        currentUser.setAvatarLocalPath(localUser.getAvatarLocalPath());
                    }

                    dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

                    dbManager.syncUser(currentUser);

                    // update categories
                    dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);

                    // update tags
                    dbManager.updateGroupTags(response.getTagList(), currentGroupID);

                    // update group info
                    dbManager.syncGroup(response.getGroup());

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(ContactActivity.this, WeChatShareActivity.class);
                            intent.putExtra("companyName", companyName);
                            intent.putExtra("count", count);
                            ViewUtils.goForwardAndFinish(ContactActivity.this, intent);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ContactActivity.this, R.string.failed_to_create_company, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
    
    private void goBack()
    {
        Bundle bundle = new Bundle();
        bundle.putString("companyName", companyName);
        bundle.putStringArrayList("inputList", inputList);
        bundle.putStringArrayList("inputChosenList", inputChosenList);
        bundle.putSerializable("contactChosenList", (Serializable) contactChosenList);
        Intent intent = new Intent(ContactActivity.this, CreateCompanyActivity.class);
        intent.putExtras(bundle);
        ViewUtils.goBackWithIntent(this, intent);
    }
}