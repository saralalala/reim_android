package com.rushucloud.reim.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import classes.adapter.LocationListViewAdapter;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.PinnedSectionListView;

public class PickLocationActivity extends Activity
{
    private static final int INPUT_LOCATION = 0;

    private ClearEditText locationEditText;
	private LocationListViewAdapter adapter;
    private PinnedSectionListView locationListView;
    private LinearLayout indexLayout;
    private TextView centralTextView;

    private List<String> hotCityList;
    private List<String> cityList;
    private List<String> showList = new ArrayList<>();
	private String currentCity;

    public static String[] indexLetters = {"热门", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_reim_location);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PickLocationActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickLocationActivity");
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

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case INPUT_LOCATION:
                {
                    Intent intent = new Intent();
                    intent.putExtra("location", data.getStringExtra("location"));
                    ViewUtils.goBackWithResult(PickLocationActivity.this, intent);
                    break;
                }
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	private void initData()
	{
		currentCity = getIntent().getStringExtra("currentCity");
        currentCity = !currentCity.isEmpty()? currentCity : getString(R.string.no_location);
        hotCityList = Arrays.asList(getResources().getStringArray(R.array.hotCityArray));
        cityList = Arrays.asList(getResources().getStringArray(R.array.cityArray));
	}
	
	private void initView()
	{
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});
		
		TextView addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
                Intent intent = new Intent(PickLocationActivity.this, InputLocationActivity.class);
                ViewUtils.goForwardForResult(PickLocationActivity.this, intent, INPUT_LOCATION);
			}
		});
		
		locationEditText = (ClearEditText) findViewById(R.id.locationEditText);
        locationEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        locationEditText.addTextChangedListener(new TextWatcher()
        {
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (locationEditText.hasFocus())
                {
                    locationEditText.setClearIconVisible(s.length() > 0);
                }
            }

            public void afterTextChanged(Editable s)
            {
                int visibility = s.toString().isEmpty() ? View.VISIBLE : View.GONE;
                indexLayout.setVisibility(visibility);
                filterList();
            }
        });

        TextView locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationTextView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (currentCity.equals(ViewUtils.getString(R.string.no_location)))
                {
                    goBack();
                }
                else
                {
                    hideSoftKeyboard();
                    Intent intent = new Intent();
                    intent.putExtra("location", currentCity);
                    ViewUtils.goBackWithResult(PickLocationActivity.this, intent);
                }
            }
        });
        locationTextView.setText(currentCity);

        initListView();
        initIndexLayout();
	}

    public void initListView()
    {
        View hotCityView = View.inflate(this, R.layout.list_hot_city, null);

        LinearLayout hotCityLayout = (LinearLayout) hotCityView.findViewById(R.id.hotCityLayout);
        int margin = ViewUtils.dpToPixel(18);
        int width = ViewUtils.dpToPixel(120);
        int height = ViewUtils.dpToPixel(48);
        int maxCount = (ViewUtils.getPhoneWindowWidth(this) + margin) / width;
        width = (ViewUtils.getPhoneWindowWidth(this) - margin) / maxCount - margin;

        LinearLayout layout = new LinearLayout(this);
        int hotCityCount = hotCityList.size();
        for (int i = 0; i < hotCityCount; i++)
        {
            if (i % maxCount == 0)
            {
                layout = new LinearLayout(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.topMargin = margin;
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                hotCityLayout.addView(layout);
            }

            final String hotCity = hotCityList.get(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            TextView hotCityTextView = new TextView(this);
            hotCityTextView.setLayoutParams(params);
            hotCityTextView.setBackgroundResource(R.drawable.hot_city_drawable);
            hotCityTextView.setGravity(Gravity.CENTER);
            hotCityTextView.setTextColor(ViewUtils.getColor(R.color.font_major_dark));
            hotCityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            hotCityTextView.setText(hotCity);
            hotCityTextView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    Intent intent = new Intent();
                    intent.putExtra("location", hotCity);
                    ViewUtils.goBackWithResult(PickLocationActivity.this, intent);
                }
            });

            if ((i + 1) % maxCount != 0)
            {
                params.rightMargin = margin;
            }

            layout.addView(hotCityTextView, params);
        }

        adapter = new LocationListViewAdapter(this, hotCityView, cityList);
        locationListView = (PinnedSectionListView) findViewById(R.id.locationListView);
        locationListView.setAdapter(adapter);
        locationListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideSoftKeyboard();

                if (position > 1 && adapter.isLocation(position))
                {
                    Intent intent = new Intent();
                    intent.putExtra("location", adapter.getCityList().get(position - 2));
                    ViewUtils.goBackWithResult(PickLocationActivity.this, intent);
                }
            }
        });
        locationListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView absListView, int i)
            {
                hideSoftKeyboard();
            }

            public void onScroll(AbsListView absListView, int i, int i2, int i3)
            {

            }
        });
    }

    public void initIndexLayout()
    {
        indexLayout = (LinearLayout) this.findViewById(R.id.indexLayout);
        centralTextView = (TextView) findViewById(R.id.centralTextView);

        final int height = (ViewUtils.getPhoneWindowHeight(this) - ViewUtils.dpToPixel(123) - ViewUtils.getStatusBarHeight(this)) / indexLetters.length;

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
                @SuppressLint("NewApi")
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
                            locationListView.setSelection(position + locationListView.getHeaderViewsCount());
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

    private void filterList()
    {
        showList.clear();
        for (String city : cityList)
        {
            if (city.contains(locationEditText.getText().toString()))
            {
                showList.add(city);
            }
        }
        adapter.setCityList(showList);
        adapter.notifyDataSetChanged();
    }

    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(locationEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}