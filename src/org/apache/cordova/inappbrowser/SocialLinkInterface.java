package org.apache.cordova.inappbrowser;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.plus.PlusShare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.webkit.JavascriptInterface;

//added by steven 10-6-2014

public class SocialLinkInterface {
	Context mContext;

	public SocialLinkInterface(Context mContext) {
		super();
		this.mContext = mContext;
	}

	// private static final String ACTION_SHARE_VIA_TWITTER = "viaTwitter";
	// private static final String ACTION_SHARE_VIA_FACEBOOK = "viaFacebook";
	// private static final String ACTION_SHARE_VIA_LINKEDIN = "viaLinkedIn";
	// private static final String ACTION_SHARE_VIA_GOOGLEPLUS =
	// "viaGooglePlus";
	// private static final String ACTION_SHARE_VIA_MAIL = "viaMail";
	// private static final String ACTION_SHARE_VIA_WHATSAPP = "viaWhatsapp";
	// private static final String ACTION_LIKE_VIA_FACEBOOK = "likeViaFacebook";
	// private static final String ACTION_PLUS_VIA_GOOGLEPLUS =
	// "plusViaGooglePlus";

	private static final String SHARE_TEXT = "I'd like to share: ";
	private static final String FACEBOOK_SHARE_PREFIX = "https://www.facebook.com/sharer/sharer.php?u=";

	private static final String PACKAGE_NAME_FACEBOOK = "com.facebook.katana";
	private static final String PACKAGE_NAME_GOOGLEPLUS = "com.google.android.apps.plus";
	private static final String PACKAGE_NAME_GMAIL = "com.google.android.gm";
	private static final String PACKAGE_NAME_TWITTER = "com.twitter.android";
	private static final String PACKAGE_NAME_LINKEDIN = "com.linkedin.android";
	private static final String PACKAGE_NAME_WHATSAPP = "com.whatsapp";

	@JavascriptInterface
	public void shareMessage(String socialAction, String msg, String url,
			String title) {
		// Toast.makeText(mContext, "shareMsg", Toast.LENGTH_SHORT).show();
		//
		//
		// if(socialAction.equals(ACTION_SHARE_VIA_FACEBOOK)){
		// shareVia(msg,url,title, "com.facebook.katana");
		// }else if(socialAction.equals(ACTION_SHARE_VIA_TWITTER)){
		// shareVia(msg,url,title, "twitter");
		// }else if(socialAction.equals(ACTION_SHARE_VIA_GOOGLEPLUS)){
		// shareVia(msg,url,title, "google+");
		// }else if(socialAction.equals(ACTION_SHARE_VIA_LINKEDIN)){
		// shareVia(msg,url,title, "com.linkedin.android");
		// }else if(socialAction.equals(ACTION_LIKE_VIA_FACEBOOK)){
		//
		// }else if(socialAction.equals(ACTION_PLUS_VIA_GOOGLEPLUS)){
		//
		// }else if(socialAction.equals(ACTION_SHARE_VIA_WHATSAPP)){
		//
		// }else if(socialAction.equals(ACTION_SHARE_VIA_MAIL)){
		//
		// }else{
		// shareViaChoser(msg,url,title);
		// }

		if (!shareViaChoser(SHARE_TEXT, url, title)) {
			// choser failed, start browser logic

			// https://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwww.rebonline.com.au%2Fblog%2F7737-are-you-a-sitting-duck-on-material-fact&display=popup

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(FACEBOOK_SHARE_PREFIX + url));

			mContext.startActivity(i);

		}

	}

	private boolean shareViaChoser(String msg, String url, String title) {
		// Intent i = new Intent(Intent.ACTION_SEND);
		// i.setType("text/plain");
		// i.putExtra(Intent.EXTRA_SUBJECT, title);
		// i.putExtra(Intent.EXTRA_TEXT, msg +" "+url);
		// mContext.startActivity(Intent.createChooser(i, "Share URL"));

		// final long time1 = System.currentTimeMillis();

		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		List<ResolveInfo> resInfo = mContext.getPackageManager()
				.queryIntentActivities(shareIntent, 0);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo resolveInfo : resInfo) {
				String packageName = resolveInfo.activityInfo.packageName;
				Intent targetedShareIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				targetedShareIntent.setType("text/plain");
				targetedShareIntent.putExtra(
						android.content.Intent.EXTRA_SUBJECT, title);
				if (packageName.equals(PACKAGE_NAME_FACEBOOK)) {
					targetedShareIntent.putExtra(
							android.content.Intent.EXTRA_TEXT, url);
				} else if (packageName.equals(PACKAGE_NAME_GOOGLEPLUS)) {
					targetedShareIntent = new PlusShare.Builder(mContext)
							.setType("text/plain")
							// .setTitle(title)
							.setText(msg).setContentUrl(Uri.parse(url))
							.getIntent();
				} else {
					targetedShareIntent.putExtra(
							android.content.Intent.EXTRA_TEXT, msg + " " + url);
				}

				// set a filter to customer choser
				if ((packageName.equals(PACKAGE_NAME_FACEBOOK))
						|| (packageName.equals(PACKAGE_NAME_TWITTER))
						|| (packageName.equals(PACKAGE_NAME_LINKEDIN))
						|| (packageName.equals(PACKAGE_NAME_WHATSAPP))
						|| (packageName.equals(PACKAGE_NAME_GMAIL))
						|| (packageName.equals(PACKAGE_NAME_GOOGLEPLUS))) {

					targetedShareIntent.setPackage(packageName);
					targetedShareIntents.add(targetedShareIntent);
				}

			}
			if (targetedShareIntents.size() > 0) {
				Intent chooserIntent = Intent.createChooser(
						targetedShareIntents.remove(0), "Share URL");

				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
						targetedShareIntents.toArray(new Parcelable[] {}));

				// final long time2 = System.currentTimeMillis();

				// Log.i("Choser","the time of choser use: "+ (time2-time1));

				mContext.startActivity(chooserIntent);

				// final long time3 = System.currentTimeMillis();

				// Log.i("Choser","the total time of choser use: "+
				// (time3-time1));

				return true;
			} else
				return false; // none of the target apps is installed, back to
								// browser.
		}

		return false;
	}

	// private boolean shareVia(String msg,String url, String title, String
	// appPackageName) {
	//
	// final String message = msg;
	// final Intent sendIntent = new Intent(Intent.ACTION_SEND);
	// //the logic of google+
	// if(appPackageName.equals("google+")){
	// final int errorCode =
	// GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
	// if (errorCode == ConnectionResult.SUCCESS) {
	//
	// Intent shareIntent = new PlusShare.Builder(mContext)
	// .setType("text/plain")
	// //.setTitle(title)
	// .setText(msg)
	// .setContentUrl(Uri.parse(url))
	// .getIntent();
	//
	// ((Activity) mContext).startActivityForResult(shareIntent, 0);
	//
	// return false;
	//
	// }else{
	// Toast.makeText(mContext,
	// "Please install Google+ to continue current action.",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// }
	// sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	// sendIntent.setType("text/plain");
	// final ActivityInfo activity = getActivity(sendIntent, appPackageName);
	// if (activity != null) {
	//
	// sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
	//
	//
	// sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, message +
	// " "+url);
	// sendIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	// sendIntent.setComponent(new ComponentName(
	// activity.applicationInfo.packageName, activity.name));
	// ((Activity) mContext).startActivityForResult(sendIntent, 0);
	// } else {
	// shareViaChoser(msg,url,title);
	// }
	//
	// return false;
	// }
	//
	// private ActivityInfo getActivity(final Intent shareIntent,
	// final String appPackageName) {
	// final PackageManager pm = mContext.getPackageManager();
	// List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent,
	// 0);
	// for (final ResolveInfo app : activityList) {
	// if ((app.activityInfo.packageName).contains(appPackageName)) {
	// return app.activityInfo;
	// }
	// }
	// // no matching app found
	// // callbackContext.sendPluginResult(new
	// // PluginResult(PluginResult.Status.ERROR,
	// // getShareActivities(activityList)));
	// Toast.makeText(mContext, "no matching app found", Toast.LENGTH_SHORT)
	// .show();
	// return null;
	// }
}

// add end
