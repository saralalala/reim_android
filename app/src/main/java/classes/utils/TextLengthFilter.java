package classes.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextLengthFilter implements InputFilter
{
	int maxLength;
	String regEx = "[\\u4e00-\\u9fa5]";
	
	public TextLengthFilter(int length)
	{
		super();
		maxLength = length * 2;
	}
	
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
	{
		int destCount = dest.toString().length() + getChineseCount(dest.toString());
		int srcCount = source.toString().length() + getChineseCount(source.toString());
		if (destCount + srcCount > maxLength)
		{
			return "";
		}
		return source;
	}
	
	private int getChineseCount(String src)
	{
		int count = 0;
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(src);
		while (matcher.find())
		{
            count += matcher.groupCount() + 1;
		}
		return count;
	}
}