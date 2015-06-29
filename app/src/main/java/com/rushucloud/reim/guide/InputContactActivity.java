package com.rushucloud.reim.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.InviteRequest;
import netUtils.response.user.InviteResponse;

public class InputContactActivity extends Activity
{
    // Widgets
    private EditText contactEditText;

    // Local Data
    private boolean fromGuide;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_input_contact);
        fromGuide = getIntent().getBooleanExtra("fromGuide", false);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("InputContactActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("InputContactActivity");
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
                hideSoftKeyboard();

                String contactString = contactEditText.getText().toString();
                SpannableString text = new SpannableString(contactString);
                text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.font_major_dark)),
                             0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                contactString = contactString.replace("，", ",");
                contactString = contactString.replace(" ", ",");
                contactString = contactString.replace("　", ",");
                contactString = contactString.replace("\n", ",");
                String[] contacts = TextUtils.split(contactString, ",");
                ArrayList<String> contactList = new ArrayList<>();
                for (String contact : contacts)
                {
                    if (!contact.trim().isEmpty())
                    {
                        contactList.add(contact.trim());
                    }
                }

                int count = 0;
                boolean contactsInvalid = false;
                String inviteList = "";
                for (String contact : contactList)
                {
                    int index = contactString.substring(count).indexOf(contact);
                    if (index != -1)
                    {
                        if (!Utils.isEmailOrPhone(contact))
                        {
                            contactsInvalid = true;
                            text.setSpan(new ForegroundColorSpan(ViewUtils.getColor(R.color.major_dark)),
                                         index + count, index + count + contact.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        inviteList += contact + ",";
                        count += contact.length();
                    }
                }

                contactEditText.setText(text);
                Spannable spanText = contactEditText.getText();
                Selection.setSelection(spanText, spanText.length());

                if (contactsInvalid)
                {
                    ViewUtils.showToast(InputContactActivity.this, R.string.error_username_wrong_format);
                }
                else if (fromGuide)
                {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("inputList", contactList);
                    ViewUtils.goBackWithResult(InputContactActivity.this, intent);
                }
                else if (!inviteList.isEmpty())
                {
                    inviteList = inviteList.substring(0, inviteList.length() - 1);
                    sendInviteRequest(inviteList);
                }
                else
                {
                    goBack();
                }
            }
        });
        if (fromGuide)
        {
            confirmTextView.setText(R.string.complete);
        }

        contactEditText = (EditText) findViewById(R.id.contactEditText);
        contactEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        contactEditText.setText(getIntent().getStringExtra("inviteList"));
        contactEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    String content = contactEditText.getText().toString() + "\n";
                    contactEditText.setText(content);
                    Spannable spanText = contactEditText.getText();
                    Selection.setSelection(spanText, spanText.length());
                }
                return false;
            }
        });
        ViewUtils.requestFocus(this, contactEditText);

        TextView promptTextView = (TextView) findViewById(R.id.promptTextView);
        SpannableString text = new SpannableString(promptTextView.getText());
        text.setSpan(new StyleSpan(Typeface.BOLD), 5, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new UnderlineSpan(), 5, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        promptTextView.setText(text);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(contactEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
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
                            ViewUtils.showToast(InputContactActivity.this, prompt);
                            goBack();
                        }
                        else
                        {
                            ViewUtils.showToast(InputContactActivity.this, R.string.failed_to_send_invite, response.getErrorMessage());
                        }
                    }
                });
            }
        });
    }
}