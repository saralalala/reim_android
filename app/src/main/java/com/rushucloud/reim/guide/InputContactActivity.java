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

public class InputContactActivity extends Activity
{
	private EditText contactEditText;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_input_contact);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InputContactActivity");
		MobclickAgent.onResume(this);
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
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});

        TextView completeTextView = (TextView) findViewById(R.id.completeTextView);
        completeTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String contactString = contactEditText.getText().toString();
                SpannableString text = new SpannableString(contactString);
                contactString = contactString.replace("，", ",");
                contactString = contactString.replace("　", ",");
                contactString = contactString.replace("\n", ",");
                String[] contacts = TextUtils.split(contactString, ",");
                ArrayList<String> contactList = new ArrayList<String>();
                for (String contact : contacts)
                {
                    if (!contact.trim().isEmpty())
                    {
                        contactList.add(contact.trim());
                    }
                }

                int count = 0;
                boolean contactsInvalid = false;
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
                        count += contact.length();
                    }
                }

                if (contactsInvalid)
                {
                    contactEditText.setText(text);
                    Spannable spanText = contactEditText.getText();
                    Selection.setSelection(spanText, spanText.length());
                    ViewUtils.showToast(InputContactActivity.this, R.string.error_username_wrong_format);
                }
                else
                {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("inputList", contactList);
                    ViewUtils.goBackWithResult(InputContactActivity.this, intent);
                }
            }
        });

        contactEditText = (EditText) findViewById(R.id.contactEditText);
        contactEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        contactEditText.setText(getIntent().getStringExtra("inviteList"));
        ViewUtils.requestFocus(this, contactEditText);
        contactEditText.setText("123123，1231231　1231231");

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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(contactEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}