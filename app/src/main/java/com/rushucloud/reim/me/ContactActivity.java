package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.adapter.ContactListViewAdapter;
import classes.model.User;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.PinnedSectionListView;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.InviteRequest;
import netUtils.response.user.InviteResponse;

public class ContactActivity extends Activity
{
    // Widgets
    private ContactListViewAdapter adapter;
    private PinnedSectionListView contactListView;
    private LinearLayout indexLayout;
    private TextView centralTextView;

    // Local Data
    private List<User> contactList = new ArrayList<>();
    private List<User> contactChosenList = new ArrayList<>();
    private boolean hasInit = false;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_invite_list);
        initView();
        readContacts();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ContactActivity");
        MobclickAgent.onResume(this);
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (contactChosenList.isEmpty())
                {
                    goBack();
                }
                else
                {
                    String inviteList = "";
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
                        ViewUtils.showToast(ContactActivity.this, R.string.error_send_invite_network_unavailable);
                    }
                    else
                    {
                        sendInviteRequest(inviteList);
                    }
                }
            }
        });

        adapter = new ContactListViewAdapter(this);
        adapter.setContactList(contactList);
        adapter.setContactChosenList(contactChosenList);
        adapter.initIndex();

        contactListView = (PinnedSectionListView) findViewById(R.id.contactListView);
        contactListView.setAdapter(adapter);
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (adapter.isContact(position))
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

        indexLayout = (LinearLayout) findViewById(R.id.indexLayout);
        centralTextView = (TextView) findViewById(R.id.centralTextView);
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }

    // Data
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
                            ViewUtils.initIndexLayout(ContactActivity.this, 50, adapter.getSelector(),
                                                      contactListView, indexLayout, centralTextView);
                        }

                        ReimProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    // Network
    private void sendInviteRequest(String inviteList)
    {
        ReimProgressDialog.show();
        InviteRequest inviteRequest = new InviteRequest(inviteList);
        inviteRequest.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final InviteResponse response = new InviteResponse(httpResponse);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ReimProgressDialog.dismiss();
                        if (response.getStatus())
                        {
                            int prompt = response.isAllInSameCompany() ? R.string.prompt_all_in_same_company : R.string.succeed_in_sending_invite;
                            ViewUtils.showToast(ContactActivity.this, prompt);
                            goBack();
                        }
                        else
                        {
                            ViewUtils.showToast(ContactActivity.this, R.string.failed_to_send_invite, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }
}