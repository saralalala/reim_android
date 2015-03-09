package classes.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rushucloud.reim.R;

public class ReimProgressDialog
{
	private static Dialog dialog;
	private static View dialogView;
	private static ImageView imageView;
	private static AnimationDrawable animationDrawable;
	
	private ReimProgressDialog()
	{
		
	}
	
	public static void init(Context context)
	{
		dialogView = View.inflate(context, R.layout.progress_dialog, null);		
		imageView = (ImageView) dialogView.findViewById(R.id.imageView);
		animationDrawable = (AnimationDrawable) imageView.getDrawable();
	}
	
	public static void setProgressDialog(Context context)
	{
		ViewGroup viewGroup = (ViewGroup) dialogView.getParent();
		if (viewGroup != null)
		{
			viewGroup.removeView(dialogView);
		}
		
		dialog = new Dialog(context, R.style.ProgressDialog);
		dialog.setContentView(dialogView);
	}
	
	public static void show()
	{
		if (!animationDrawable.isRunning())
		{
			animationDrawable.start();
		}
		dialog.show();
	}
	
	public static void dismiss()
	{
		dialog.dismiss();
	}
}