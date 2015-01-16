package classes.widget;

import com.rushucloud.reim.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ReimProgressDialog
{
	private static Dialog progressDialog;
	private static View dialogView;
	private static ImageView imageView;
	private static Animation animation;
	
	private ReimProgressDialog()
	{
		
	}
	
	public static void init(Context context)
	{
		dialogView = View.inflate(context, R.layout.progress_dialog, null);		
		imageView = (ImageView) dialogView.findViewById(R.id.imageView);
		animation = AnimationUtils.loadAnimation(context, R.anim.progress_dialog);
	}
	
	public static void setProgressDialog(Context context)
	{
		ViewGroup viewGroup = (ViewGroup) dialogView.getParent();
		if (viewGroup != null)
		{
			viewGroup.removeView(dialogView);
		}
		
		progressDialog = new Dialog(context, R.style.ProgressDialog);
		progressDialog.setContentView(dialogView);
	}
	
	public static void show()
	{
		imageView.startAnimation(animation);
		progressDialog.show();
	}
	
	public static void dismiss()
	{
		progressDialog.dismiss();
	}
}