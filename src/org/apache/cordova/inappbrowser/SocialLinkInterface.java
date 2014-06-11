package org.apache.cordova.inappbrowser;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

//added by steven 10-6-2014

public class SocialLinkInterface {
	Context mContext;

	public SocialLinkInterface(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	private static final String ACTION_SHARE_VIA_TWITTER = "viaTwitter";
	private static final String ACTION_SHARE_VIA_FACEBOOK = "viaFacebook";
	private static final String ACTION_SHARE_VIA_LINKEDIN = "viaLinkedIn";
	private static final String ACTION_SHARE_VIA_GOOGLEPLUS = "viaGooglePlus";
	private static final String ACTION_SHARE_VIA_MAIL = "viaMail";
	private static final String ACTION_SHARE_VIA_WHATSAPP = "viaWhatsapp";
	private static final String ACTION_LIKE_VIA_FACEBOOK = "likeViaFacebook";
	private static final String ACTION_PLUS_VIA_GOOGLEPLUS = "plusViaGooglePlus";

	@JavascriptInterface
	public void shareMessage(String socialAction,String msg,String url, String title) {
		Toast.makeText(mContext, "shareMsg", Toast.LENGTH_SHORT).show();
		
		
		if(socialAction.equals(ACTION_SHARE_VIA_FACEBOOK)){
			shareVia(msg,url,title, "com.facebook.katana");
		}else if(socialAction.equals(ACTION_SHARE_VIA_TWITTER)){
			shareVia(msg,url,title, "twitter");
		}else if(socialAction.equals(ACTION_SHARE_VIA_GOOGLEPLUS)){
			shareVia(msg,url,title, "google+");
		}else if(socialAction.equals(ACTION_SHARE_VIA_LINKEDIN)){
			shareVia(msg,url,title, "com.linkedin.android");
		}else if(socialAction.equals(ACTION_LIKE_VIA_FACEBOOK)){
		
		}else if(socialAction.equals(ACTION_PLUS_VIA_GOOGLEPLUS)){
			
		}else if(socialAction.equals(ACTION_SHARE_VIA_WHATSAPP)){
			
		}else if(socialAction.equals(ACTION_SHARE_VIA_MAIL)){
			
		}else{
			shareViaChoser(msg,url,title);
		}

		
	}

	private boolean shareViaChoser(String msg,String url, String title) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, title);
		i.putExtra(Intent.EXTRA_TEXT, msg +" "+url);
		mContext.startActivity(Intent.createChooser(i, "Share URL"));

		return false;
	}

	private boolean shareVia(String msg,String url, String title, String appPackageName) {

		final String message = msg;
		final Intent sendIntent = new Intent(Intent.ACTION_SEND);
		//the logic of google+
		if(appPackageName.equals("google+")){
			final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
			if (errorCode == ConnectionResult.SUCCESS) {
			
		      Intent shareIntent = new PlusShare.Builder(mContext)
	          .setType("text/plain")
	          //.setTitle(title)
	          .setText(msg)
	          .setContentUrl(Uri.parse(url))
	          .getIntent();

		      ((Activity) mContext).startActivityForResult(shareIntent, 0);
		      
		      return false;
		      
			}else{
				Toast.makeText(mContext, "Please install Google+ to continue current action.", Toast.LENGTH_SHORT).show();
			}

		}
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		 sendIntent.setType("text/plain");
		final ActivityInfo activity = getActivity(sendIntent, appPackageName);
		if (activity != null) {
			
			sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
			
			
			sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, message + " "+url);
			sendIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			sendIntent.setComponent(new ComponentName(
					activity.applicationInfo.packageName, activity.name));
			((Activity) mContext).startActivityForResult(sendIntent, 0);
		} else {
			shareViaChoser(msg,url,title);
		}

		return false;
	}

	private ActivityInfo getActivity(final Intent shareIntent,
			final String appPackageName) {
		final PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent,
				0);
		for (final ResolveInfo app : activityList) {
			if ((app.activityInfo.packageName).contains(appPackageName)) {
				return app.activityInfo;
			}
		}
		// no matching app found
		// callbackContext.sendPluginResult(new
		// PluginResult(PluginResult.Status.ERROR,
		// getShareActivities(activityList)));
		Toast.makeText(mContext, "no matching app found", Toast.LENGTH_SHORT)
				.show();
		return null;
	}
}

// add end
