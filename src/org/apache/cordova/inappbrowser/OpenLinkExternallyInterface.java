package org.apache.cordova.inappbrowser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

public class OpenLinkExternallyInterface {

	Context mContext;

	public OpenLinkExternallyInterface(Context mContext) {
		super();
		this.mContext = mContext;

	}
	
	
	@JavascriptInterface
	public void openUrlLinkExternal(String url){
		if(!url.startsWith("http://")){
			  url = "http://"+url;
			}
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));

		mContext.startActivity(i);
	}
}
