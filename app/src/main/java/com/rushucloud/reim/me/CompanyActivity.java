package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.PickCompanyActivity;
import com.umeng.analytics.MobclickAgent;

import classes.model.Group;
import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

public class CompanyActivity extends Activity
{
    // Widgets
    private TextView companyTextView;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_company);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("CompanyActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
        loadCompanyName();
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("CompanyActivity");
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

        TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CompanyActivity.this, EditCompanyActivity.class);
                intent.putExtra("newCompany", true);
                ViewUtils.goForward(CompanyActivity.this, intent);
            }
        });

        companyTextView = (TextView) findViewById(R.id.companyTextView);

        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        Group currentGroup = AppPreference.getAppPreference().getCurrentGroup();
        if (currentUser.isAdmin() || currentGroup == null)
        {
            LinearLayout companyLayout = (LinearLayout) findViewById(R.id.companyLayout);
            companyLayout.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    ViewUtils.goForward(CompanyActivity.this, EditCompanyActivity.class);
                }
            });
        }

        LinearLayout changeCompanyLayout = (LinearLayout) findViewById(R.id.changeCompanyLayout);
        changeCompanyLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForward(CompanyActivity.this, PickCompanyActivity.class);
            }
        });
    }

    private void loadCompanyName()
    {
        Group currentGroup = AppPreference.getAppPreference().getCurrentGroup();
        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        if (currentGroup != null)
        {
            companyTextView.setText(currentGroup.getName());
        }
        else if (!currentUser.getAppliedCompany().isEmpty())
        {
            companyTextView.setText(currentUser.getAppliedCompany() + ViewUtils.getString(R.string.waiting_for_approve));
        }
        else
        {
            companyTextView.setText(R.string.not_joined);
        }
    }

    private void goBack()
    {
        ViewUtils.goBack(this);
    }
}