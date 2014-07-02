package org.apache.cordova.inappbrowser;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class InAppChromeClient extends WebChromeClient {

    private CordovaWebView webView;
    private String LOG_TAG = "InAppChromeClient";
    private long MAX_QUOTA = 100 * 1024 * 1024;
    private ViewGroup contentContainer;
    private ViewGroup toolbar;
    private View loadingText;
    private Context mContext;

    public InAppChromeClient(CordovaWebView webView, ViewGroup container,ViewGroup toolb,View load,Context context) {
        super();
        this.webView = webView;
        this.mContext = context;
        this.contentContainer = container;
        this.toolbar = toolb;
        this.loadingText= load;
        
    }
    /**
     * Handle database quota exceeded notification.
     *
     * @param url
     * @param databaseIdentifier
     * @param currentQuota
     * @param estimatedSize
     * @param totalUsedQuota
     * @param quotaUpdater
     */
    @SuppressWarnings("deprecation")
	@Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
            long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
    {
        LOG.d(LOG_TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);
        quotaUpdater.updateQuota(MAX_QUOTA);
    }

    /**
     * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin.
     *
     * @param origin
     * @param callback
     */
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
    }

    /**
     * Tell the client to display a prompt dialog to the user.
     * If the client returns true, WebView will assume that the client will
     * handle the prompt dialog and call the appropriate JsPromptResult method.
     *
     * The prompt bridge provided for the InAppBrowser is capable of executing any
     * oustanding callback belonging to the InAppBrowser plugin. Care has been
     * taken that other callbacks cannot be triggered, and that no other code
     * execution is possible.
     *
     * To trigger the bridge, the prompt default value should be of the form:
     *
     * gap-iab://<callbackId>
     *
     * where <callbackId> is the string id of the callback to trigger (something
     * like "InAppBrowser0123456789")
     *
     * If present, the prompt message is expected to be a JSON-encoded value to
     * pass to the callback. A JSON_EXCEPTION is returned if the JSON is invalid.
     *
     * @param view
     * @param url
     * @param message
     * @param defaultValue
     * @param result
     */
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        // See if the prompt string uses the 'gap-iab' protocol. If so, the remainder should be the id of a callback to execute.
        if (defaultValue != null && defaultValue.startsWith("gap")) {
            if(defaultValue.startsWith("gap-iab://")) {
                PluginResult scriptResult;
                String scriptCallbackId = defaultValue.substring(10);
                if (scriptCallbackId.startsWith("InAppBrowser")) {
                    if(message == null || message.length() == 0) {
                        scriptResult = new PluginResult(PluginResult.Status.OK, new JSONArray());
                    } else {
                        try {
                            scriptResult = new PluginResult(PluginResult.Status.OK, new JSONArray(message));
                        } catch(JSONException e) {
                            scriptResult = new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
                        }
                    }
                    this.webView.sendPluginResult(scriptResult, scriptCallbackId);
                    result.confirm("");
                    return true;
                }
            }
            else
            {
                // Anything else with a gap: prefix should get this message
                LOG.w(LOG_TAG, "InAppBrowser does not support Cordova API calls: " + url + " " + defaultValue); 
                result.cancel();
                return true;
            }
        }
        return false;
    }
    
    private void setShowLoadingView(boolean bShow){
    	if(loadingText != null){
    		if(bShow){
    			loadingText.setVisibility(View.VISIBLE);
    		}else{
    			loadingText.setVisibility(View.GONE);
    		}
    	}
    }
    
    
    @Override
	public boolean onCreateWindow(WebView view, boolean isDialog,
			boolean isUserGesture, Message resultMsg) {
		Log.i(LOG_TAG,"onCreateWindow");
		
		this.toolbar.setVisibility(View.VISIBLE);
		this.contentContainer.setVisibility(View.VISIBLE);
		
        WebView newWebView = new WebView(mContext);
        newWebView.getSettings().setJavaScriptEnabled(true);
        newWebView.setWebChromeClient(this);
        newWebView.setWebViewClient(new WebViewClient(){

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				setShowLoadingView(true);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				setShowLoadingView(false);
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
        	
 
        });
        newWebView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        contentContainer.addView(newWebView);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();
        
		// let's be cool and slide the new web view up into view
//		Animation slideUp = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
        ScaleAnimation slideUp = new ScaleAnimation(0.0f, 0.0f, 0.0f, 1.0f);
        slideUp.setInterpolator(new HesitateInterpolator());
		contentContainer.startAnimation(slideUp);
		
        return true;
//		return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
	}
    
    /**
	 * Lower the popup web view down
	 */
	public void closeChild() {
		Log.v(LOG_TAG, "Closing Child WebView");
//		Animation slideDown = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
        ScaleAnimation slideDown = new ScaleAnimation(0.0f, 0.0f, 1.0f, 0.0f);
        slideDown.setDuration(500);
        slideDown.setInterpolator(new HesitateInterpolator());
		contentContainer.startAnimation(slideDown);
		slideDown.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				
				toolbar.setVisibility(View.GONE);
				contentContainer.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

    
	@Override
	public void onCloseWindow(WebView window) {
		Log.d(LOG_TAG, "Window close");
		this.closeChild();
		super.onCloseWindow(window);
	}
	public boolean isChildOpen() {
		return (toolbar.getVisibility() == View.VISIBLE)&&(contentContainer.getVisibility() == View.VISIBLE);
	}
    
	@Override
    public void onShowCustomView (View view, WebChromeClient.CustomViewCallback callback){
    	Log.i(LOG_TAG, "onShowCustomView"); 
    }
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		// TODO Auto-generated method stub
		super.onProgressChanged(view, newProgress);
	}


	
}
