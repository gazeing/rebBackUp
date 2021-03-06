/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.inappbrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import com.reb.rebDemo.GlobalData;


import java.util.HashMap;
import java.util.StringTokenizer;

@SuppressLint("SetJavaScriptEnabled")
public class InAppBrowser extends CordovaPlugin{

    private static final String NULL = "null";
    protected static final String LOG_TAG = "InAppBrowser";
    private static final String SELF = "_self";
    private static final String SYSTEM = "_system";
    // private static final String BLANK = "_blank";
    private static final String EXIT_EVENT = "exit";
    private static final String LOCATION = "location";
    private static final String HIDDEN = "hidden";
    private static final String LOAD_START_EVENT = "loadstart";
    private static final String LOAD_STOP_EVENT = "loadstop";
    private static final String LOAD_ERROR_EVENT = "loaderror";
    private static final String CLOSE_BUTTON_CAPTION = "closebuttoncaption";
    private static final String CLEAR_ALL_CACHE = "clearcache";
    private static final String CLEAR_SESSION_CACHE = "clearsessioncache";
    
    
    private static final int LAYOUT_ID_TOOLBAR 											= 10;
    private static final int LAYOUT_ID_ACTION_BUTTON 									= 1;
    private static final int LAYOUT_ID_BACK_BUTTON 										= 2;
    private static final int LAYOUT_ID_FORWARD_BUTTON									= 3;
    private static final int LAYOUT_ID_TEXT_BOX											= 4;
    private static final int LAYOUT_ID_CLOSE_BUTTON										= 5;
    private static final int LAYOUT_ID_MAIN_WEBVIEW 									= 6;
    
    private InAppChromeClient mAppChromeClient = null;

    private Dialog dialog;
    private WebView inAppWebView;
    private EditText edittext;
    private CallbackContext callbackContext;
    private boolean showLocationBar = true;
    private boolean openWindowHidden = false;
    private String buttonLabel = "Done";
    private boolean clearAllCache= false;
    private boolean clearSessionCache=false;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    	System.out.println("Coming to execute");
        if (action.equals("open")) {
            this.callbackContext = callbackContext;
            final String url = args.getString(0);
            
   
            
            Log.d(LOG_TAG, "Coming to execute ======== "+url);
            String t = args.optString(1);
            if (t == null || t.equals("") || t.equals(NULL)) {
                t = SELF;
            }
            final String target = t;
            final HashMap<String, Boolean> features = parseFeature(args.optString(2));
            Log.d(LOG_TAG, "target = " + target);
            
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String result = "";
                    // SELF
                    if (SELF.equals(target)) {
                        Log.d(LOG_TAG, "in self");

                        // load in webview
                        if (url.startsWith("file://") || url.startsWith("javascript:") 
                                || Config.isUrlWhiteListed(url)) {
                        	
                        	webView.loadUrl(url);
                        }
                        //Load the dialer
                        else if (url.startsWith(WebView.SCHEME_TEL))
                        {
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse(url));
                               cordova.getActivity().startActivity(intent);
                            } catch (android.content.ActivityNotFoundException e) {
                                LOG.e(LOG_TAG, "Error dialing " + url + ": " + e.toString());
                            }
                        }
                        // load in InAppBrowser
                        else {
                            if(GlobalData.m_Intent!=null){
                            	String intent_url = GlobalData.m_Website_Prefix+GlobalData.m_Intent.getData().getPath();
                            	GlobalData.m_Intent =null;
                            	Log.i(LOG_TAG,"url from intent = "+intent_url);
                            	result = showWebPage(intent_url, features);
                            }else{
                        	
                            result = showWebPage(url, features);
                            }
                        }
                    }
                    // SYSTEM
                    else if (SYSTEM.equals(target)) {
                        Log.d(LOG_TAG, "in system");
                        result = openExternal(url);
                    }
                    // BLANK - or anything else
                    else {
                        Log.d(LOG_TAG, "in blank");
                        if(GlobalData.m_Intent!=null){
                        	String intent_url = GlobalData.m_Website_Prefix+GlobalData.m_Intent.getData().getPath();
                        	GlobalData.m_Intent =null;
                        	Log.i(LOG_TAG,"url from intent = "+intent_url);
                        	result = showWebPage(intent_url, features);
                        }else{
                    	
                        result = showWebPage(url, features);
                        }
                      
                    }
    
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                }
            });
        }
        else if (action.equals("close")) {
            closeDialog();
        }
        else if (action.equals("injectScriptCode")) {
            String jsWrapper = null;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("prompt(JSON.stringify([eval(%%s)]), 'gap-iab://%s')", callbackContext.getCallbackId());
            }
            System.out.println("poda: "+args.getString(0));
            injectDeferredObject(args.getString(0), jsWrapper);
        }
        else if (action.equals("injectScriptFile")) {
            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('script'); c.src = %%s; c.onload = function() { prompt('', 'gap-iab://%s'); }; d.body.appendChild(c); })(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('script'); c.src = %s; d.body.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        }
        else if (action.equals("injectStyleCode")) {
            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('style'); c.innerHTML = %%s; d.body.appendChild(c); prompt('', 'gap-iab://%s');})(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('style'); c.innerHTML = %s; d.body.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        }
        else if (action.equals("injectStyleFile")) {
            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('link'); c.rel='stylesheet'; c.type='text/css'; c.href = %%s; d.head.appendChild(c); prompt('', 'gap-iab://%s');})(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('link'); c.rel='stylesheet'; c.type='text/css'; c.href = %s; d.head.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        }
        else if (action.equals("show")) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            this.callbackContext.sendPluginResult(pluginResult);
        }
        else {
            return false;
        }
        return true;
    }
   
    
    /**
     * Called when the view navigates.
     */
    @Override
    public void onReset() {
    	System.out.println("Coming to onReset");
        closeDialog();        
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
    	//added by steven
    	GlobalData.m_InAppBrowser = null;
    	
    	
    	
    	System.out.println("Coming to onDestroy");
        closeDialog();
    }
    
    /**
     * Inject an object (script or style) into the InAppBrowser WebView.
     *
     * This is a helper method for the inject{Script|Style}{Code|File} API calls, which
     * provides a consistent method for injecting JavaScript code into the document.
     *
     * If a wrapper string is supplied, then the source string will be JSON-encoded (adding
     * quotes) and wrapped using string formatting. (The wrapper string should have a single
     * '%s' marker)
     *
     * @param source      The source object (filename or script/style text) to inject into
     *                    the document.
     * @param jsWrapper   A JavaScript string to wrap the source string in, so that the object
     *                    is properly injected, or null if the source string is JavaScript text
     *                    which should be executed directly.
     */
    private void injectDeferredObject(String source, String jsWrapper) {
    	System.out.println("Coming to injectDeferredObject");
        String scriptToInject;
        if (jsWrapper != null) {
            org.json.JSONArray jsonEsc = new org.json.JSONArray();
            jsonEsc.put(source);
            String jsonRepr = jsonEsc.toString();
            String jsonSourceString = jsonRepr.substring(1, jsonRepr.length()-1);
            scriptToInject = String.format(jsWrapper, jsonSourceString);
        } else {
            scriptToInject = source;
        }
        final String finalScriptToInject = scriptToInject;
        // This action will have the side-effect of blurring the currently focused element
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inAppWebView.loadUrl("javascript:" + finalScriptToInject);
            }
        });
    }

    /**
     * Put the list of features into a hash map
     * 
     * @param optString
     * @return
     */
    private HashMap<String, Boolean> parseFeature(String optString) {
    	System.out.println("Coming to parseFeature");
        if (optString.equals(NULL)) {
            return null;
        } else {
            HashMap<String, Boolean> map = new HashMap<String, Boolean>();
            StringTokenizer features = new StringTokenizer(optString, ",");
            StringTokenizer option;
            while(features.hasMoreElements()) {
                option = new StringTokenizer(features.nextToken(), "=");
                if (option.hasMoreElements()) {
                    String key = option.nextToken();
                    if (key.equalsIgnoreCase(CLOSE_BUTTON_CAPTION)) {
                        this.buttonLabel = option.nextToken();
                    } else {
                        Boolean value = option.nextToken().equals("no") ? Boolean.FALSE : Boolean.TRUE;
                        map.put(key, value);
                    }
                }
            }
            return map;
        }
    }

    /**
     * Display a new browser with the specified URL.
     *
     * @param url           The url to load.
     * @param usePhoneGap   Load url in PhoneGap webview
     * @return              "" if ok, or error message.
     */
    public String openExternal(String url) {
    	System.out.println("Coming to openExternal");
        try {
            Intent intent = null;
            intent = new Intent(Intent.ACTION_VIEW);
            // Omitting the MIME type for file: URLs causes "No Activity found to handle Intent".
            // Adding the MIME type to http: URLs causes them to not be handled by the downloader.
            Uri uri = Uri.parse(url);
            if ("file".equals(uri.getScheme())) {
                intent.setDataAndType(uri, webView.getResourceApi().getMimeType(uri));
            } else {
                intent.setData(uri);
            }
            this.cordova.getActivity().startActivity(intent);
            return "";
        } catch (android.content.ActivityNotFoundException e) {
            Log.d(LOG_TAG, "InAppBrowser: Error loading url "+url+":"+ e.toString());
            return e.toString();
        }
    }
    
    
    // added on 15/05/2014 - createPopup method to handle exitApp alert popup
    public static void createPopup(Context context, String title, String message) {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

    	alertDialogBuilder.setTitle(title);
    	alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
    	alertDialogBuilder.setMessage(message);
    	alertDialogBuilder.setCancelable(false);
    	alertDialogBuilder.setNegativeButton("Yes",new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
    		}
    	});
    	alertDialogBuilder.setPositiveButton("No",new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			dialog.dismiss();
    		}
    	});

    	AlertDialog alertDialog = alertDialogBuilder.create();
    	alertDialog.show();
    }

    /**
     * Closes the dialog
     */
    public void closeDialog() {
    	
    	
    	
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	System.out.println("Stack Trace: "+stackTraceElements);
    	System.out.println("Coming to closeDialog");
        final WebView childView = this.inAppWebView;
        // The JS protects against multiple calls, so this should happen only when
        // closeDialog() is called by other native code.
        if (childView == null) {
            return;
        }
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                childView.loadUrl("about:blank");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", EXIT_EVENT);
            sendUpdate(obj, false);
        } catch (JSONException ex) {
            Log.d(LOG_TAG, "Should never happen");
        }
        
    }
    

	private void closePopupWebView() {
		if(mAppChromeClient != null)
		mAppChromeClient.closeChild();
		
	}

    /**
     * Checks to see if it is possible to go back one page in history, then does so.
     */
    //added on 15/05/2014 - changed return type from void to boolean 
    private boolean goBack() {
    	System.out.println("Coming to goBack");
    	if(this.mAppChromeClient != null){
	    	if(this.mAppChromeClient.isChildOpen()){
	    		this.mAppChromeClient.closeChild();
	    		return true;
	    	}
    	}
    	if (this.inAppWebView.canGoBack()) {
            this.inAppWebView.goBack();
            //added on 15/05/2014
            return true;
        }
        //added on 15/05/2014
        return false;
    }

    /**
     * Checks to see if it is possible to go forward one page in history, then does so.
     */
    private void goForward() {
    	System.out.println("Coming to goForward");
        if (this.inAppWebView.canGoForward()) {
            this.inAppWebView.goForward();
        }
    }

    /**
     * Navigate to the new page
     *
     * @param url to load
     */
    private void navigate(String url) {
    	System.out.println("Coming to navigate");
    	Log.d(LOG_TAG, "Coming to navigate ======== "+url);
        InputMethodManager imm = (InputMethodManager)this.cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);

        if (!url.startsWith("http") && !url.startsWith("file:")) {
            this.inAppWebView.loadUrl("http://" + url);
        } else {
            this.inAppWebView.loadUrl(url);
        }
        this.inAppWebView.requestFocus();
    }


    /**
     * Should we show the location bar?
     *
     * @return boolean
     */
    private boolean getShowLocationBar() {
    	System.out.println("Coming to getShowLocationBar");
        return this.showLocationBar;
    }

    /**
     * Display a new browser with the specified URL.
     *
     * @param url           The url to load.
     * @param jsonObject
     */
    public String showWebPage(final String url, HashMap<String, Boolean> features) {
    	System.out.println("Coming to showWebPage");
    	Log.d(LOG_TAG, "Coming to showWebPage ======== "+url);
        // Determine if we should hide the location bar.
        //showLocationBar = true;
        // added 07/FEB/2014
    	showLocationBar = false;
        openWindowHidden = false;
        if (features != null) {
            Boolean show = features.get(LOCATION);
            if (show != null) {
                showLocationBar = show.booleanValue();
              //added 07/FEB/2014
                //comment out by steven 24-06-2014
//            	showLocationBar = false;
            }
            Boolean hidden = features.get(HIDDEN);
            if (hidden != null) {
                openWindowHidden = hidden.booleanValue();
            }
            Boolean cache = features.get(CLEAR_ALL_CACHE);
            if (cache != null) {
                clearAllCache = cache.booleanValue();
            } else {
                cache = features.get(CLEAR_SESSION_CACHE);
                if (cache != null) {
                    clearSessionCache = cache.booleanValue();
                }
            }
        }
        
        final CordovaWebView thatWebView = this.webView;
        
        
        //added by steven
        GlobalData.m_InAppBrowser = this;

        // Create dialog in new thread
        Runnable runnable = new Runnable() {
            /**
             * Convert our DIP units to Pixels
             *
             * @return int
             */
            private int dpToPixels(int dipValue) {
                int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                                                            (float) dipValue,
                                                            cordova.getActivity().getResources().getDisplayMetrics()
                );

                return value;
            }

            @SuppressWarnings("deprecation")
			public void run() {
            	// Let's create the main dialog
                dialog = new Dialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
             
                // added 15/05/2014 - commented the setOnDismissListener section
//                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        public void onDismiss(DialogInterface dialog) {     
//                            closeDialog();
//                        }
//                });
                
                //Added on 15/05/2014 
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (event.getAction()!=KeyEvent.ACTION_DOWN)
		                    return true;
						if(keyCode==KeyEvent.KEYCODE_BACK){
							boolean showAppCloseAlert=goBack();
							if(!showAppCloseAlert){	
								createPopup(cordova.getActivity(),  "Close Application",  "Do you want to exit the app?");
							}
							return true;
						} else {
							return false;
						}
					}
				});


                
                
                
                // Main container layout
                RelativeLayout main = new RelativeLayout(cordova.getActivity());
//                main.setOrientation(LinearLayout.VERTICAL);
                
  
                

                // Toolbar layout
               RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
                //Please, no more black! 
                toolbar.setBackgroundColor(android.graphics.Color.parseColor("#282A79"));
                RelativeLayout.LayoutParams toolbar_LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, this.dpToPixels(44));
                toolbar_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                toolbar.setLayoutParams(toolbar_LayoutParams);
                toolbar.setPadding(this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2));
                toolbar.setHorizontalGravity(Gravity.LEFT);
                toolbar.setVerticalGravity(Gravity.BOTTOM);
                toolbar.setId(LAYOUT_ID_TOOLBAR);
               
                
               // added 07/FEB/2014
                //toolbar.setVisibility(toolbar.INVISIBLE);

                // Action Button Container layout
                RelativeLayout actionButtonContainer = new RelativeLayout(cordova.getActivity());
                RelativeLayout.LayoutParams actionButtonContainerLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                actionButtonContainerLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                actionButtonContainer.setLayoutParams(actionButtonContainerLayout);
                actionButtonContainer.setHorizontalGravity(Gravity.LEFT);
                actionButtonContainer.setVerticalGravity(Gravity.CENTER_VERTICAL);
                actionButtonContainer.setId(LAYOUT_ID_ACTION_BUTTON);

                // Back button
                Button back = new Button(cordova.getActivity());
                RelativeLayout.LayoutParams backLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                backLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
                back.setLayoutParams(backLayoutParams);
                back.setBackgroundColor(android.graphics.Color.parseColor("#282A79"));
                back.setContentDescription("Back Button");
                back.setId(LAYOUT_ID_BACK_BUTTON);
                back.setText("<");
                back.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        goBack();
                    }
                });

                // Forward button
                Button forward = new Button(cordova.getActivity());
                RelativeLayout.LayoutParams forwardLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                forwardLayoutParams.addRule(RelativeLayout.RIGHT_OF, LAYOUT_ID_BACK_BUTTON);
                forward.setLayoutParams(forwardLayoutParams);
                forward.setContentDescription("Forward Button");
                forward.setId(LAYOUT_ID_FORWARD_BUTTON);
                forward.setText(">");
                forward.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        goForward();
                    }
                });

                // Edit Text Box
                edittext = new EditText(cordova.getActivity());
                RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                textLayoutParams.addRule(RelativeLayout.RIGHT_OF, LAYOUT_ID_CLOSE_BUTTON);
                textLayoutParams.addRule(RelativeLayout.LEFT_OF, LAYOUT_ID_ACTION_BUTTON);
                edittext.setLayoutParams(textLayoutParams);
                edittext.setId(LAYOUT_ID_TEXT_BOX);
                edittext.setSingleLine(true);
                edittext.setText(url);
                edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                edittext.setImeOptions(EditorInfo.IME_ACTION_GO);
                edittext.setInputType(InputType.TYPE_NULL); // Will not except input... Makes the text NON-EDITABLE
                edittext.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                          navigate(edittext.getText().toString());
                          return true;
                        }
                        return false;
                    }
                });

                // Close button
                Button close = new Button(cordova.getActivity());
                RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                close.setLayoutParams(closeLayoutParams);
                forward.setContentDescription("Close Button");
                close.setId(LAYOUT_ID_CLOSE_BUTTON);
                close.setText(buttonLabel);
                close.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
//                        closeDialog();
                    	closePopupWebView();
                    }

                });
                
                //create popup view container layout
                RelativeLayout browserLayout = new RelativeLayout(cordova.getActivity()); 
                browserLayout.setBackgroundColor(android.graphics.Color.parseColor("#282A79"));
                RelativeLayout.LayoutParams browser_LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                browser_LayoutParams.addRule(RelativeLayout.ABOVE,LAYOUT_ID_TOOLBAR);
                browserLayout.setLayoutParams(browser_LayoutParams);
                
                
                
                // Add the back and forward buttons to our action button container layout
                actionButtonContainer.addView(back);
                actionButtonContainer.addView(forward);
                
                
                //added by steven 25-06-2014
                //add a text "loading" to popupview
                
                TextView loadTextView = new TextView(cordova.getActivity());
                loadTextView.setTextAppearance(cordova.getActivity(), android.R.style.TextAppearance_Large);
                loadTextView.setVisibility(View.GONE);
                loadTextView.setText("Loading...");
                loadTextView.setTextColor(Color.BLACK);
                RelativeLayout.LayoutParams loadTextView_LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                loadTextView_LayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                loadTextView.setLayoutParams(loadTextView_LayoutParams);
                


                // WebView
                inAppWebView = new WebView(cordova.getActivity());
                RelativeLayout.LayoutParams webview_LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                webview_LayoutParams.addRule(RelativeLayout.ABOVE,LAYOUT_ID_TOOLBAR);
                inAppWebView.setLayoutParams(webview_LayoutParams);
                mAppChromeClient = new InAppChromeClient(thatWebView,browserLayout,toolbar,loadTextView,cordova.getActivity());
                inAppWebView.setWebChromeClient(mAppChromeClient);
                WebViewClient client = new InAppBrowserClient(thatWebView, edittext);
                inAppWebView.setWebViewClient(client);
                WebSettings settings = inAppWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setSupportMultipleWindows(true);
                settings.setBuiltInZoomControls(false);
                settings.setPluginState(android.webkit.WebSettings.PluginState.ON);
                


                
                String appCachePath = cordova.getActivity().getApplicationContext().getCacheDir().getAbsolutePath();
//                Log.i(LOG_TAG,"appCachePath = "+cordova.getActivity().getApplicationContext().getCacheDir().getAbsolutePath());
                settings.setAppCacheEnabled(true);
                
                
                settings.setCacheMode(isNetworkAvailable() ?WebSettings.LOAD_DEFAULT:WebSettings.LOAD_CACHE_ELSE_NETWORK);
                settings.setDomStorageEnabled(true);
                settings.setAppCachePath(appCachePath);
                settings.setAllowFileAccess(true);
                Log.i(LOG_TAG,"appCachePath ========>"+appCachePath);

                //Toggle whether this is enabled or not!
                Bundle appSettings = cordova.getActivity().getIntent().getExtras();
                boolean enableDatabase = appSettings == null ? true : appSettings.getBoolean("InAppBrowserStorageEnabled", true);
                if (enableDatabase) {
                    String databasePath = cordova.getActivity().getApplicationContext().getDir("inAppBrowserDB", Context.MODE_PRIVATE).getPath();
                    settings.setDatabasePath(databasePath);
                    settings.setDatabaseEnabled(true);
                }
                settings.setDomStorageEnabled(true);

                if (clearAllCache) {
                    CookieManager.getInstance().removeAllCookie();
                } else if (clearSessionCache) {
                    CookieManager.getInstance().removeSessionCookie();
                }
                

                inAppWebView.setId(LAYOUT_ID_MAIN_WEBVIEW);
                
                //added by steven 10-6-2014
                inAppWebView.addJavascriptInterface(new SocialLinkInterface(cordova.getActivity()), "SocialShare");
                inAppWebView.addJavascriptInterface(new OpenLinkExternallyInterface(cordova.getActivity()), "OpenUrlExternally");
                //add end
                
                //removed by steven 2-7-14 for the 4.1 version not show zoom correctly
//                inAppWebView.getSettings().setLoadWithOverviewMode(true);
                inAppWebView.getSettings().setUseWideViewPort(true);
                inAppWebView.requestFocus();
                inAppWebView.requestFocusFromTouch();

                
               
                
   

                // Add the views to our toolbar
                toolbar.addView(close);
                toolbar.addView(actionButtonContainer);
                toolbar.addView(edittext);
               



   
                
                // Add our webview to our main view/layout
                main.addView(inAppWebView);
                main.addView(toolbar);
                
                // Don't add the toolbar if its been disabled
                if (!getShowLocationBar()) {
                    // Add our toolbar to our main view/layout
                    toolbar.setVisibility(View.GONE);
                }
                main.addView(browserLayout);
                browserLayout.setVisibility(View.INVISIBLE);
                
                main.addView(loadTextView);
                
                // added 04/MAR/2014
                System.out.println("final b4: "+ inAppWebView.getSettings().getUserAgentString());
                
                inAppWebView.getSettings().setUserAgentString(inAppWebView.getSettings().getUserAgentString()+" mobileapp");
                
                System.out.println("final after: "+ inAppWebView.getSettings().getUserAgentString());
                System.out.println("url = : "+ url);
                
                inAppWebView.loadUrl(url);

                
                
 

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.setContentView(main);
                dialog.show();
                dialog.getWindow().setAttributes(lp);
                // the goal of openhidden is to load the url and not display it
                // Show() needs to be called to cause the URL to be loaded
                if(openWindowHidden) {
                	dialog.hide();
                }
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
        return "";
    }
    
    

    


    /**
     * Create a new plugin success result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     */
    private void sendUpdate(JSONObject obj, boolean keepCallback) {
    	System.out.println("Coming to sendUpdate two arguments");
        sendUpdate(obj, keepCallback, PluginResult.Status.OK);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     * @param status the status code to return to the JavaScript environment
     */    
    private void sendUpdate(JSONObject obj, boolean keepCallback, PluginResult.Status status) {
    	System.out.println("Coming to sendUpdate three arguments");
        if (callbackContext != null) {
            PluginResult result = new PluginResult(status, obj);
            result.setKeepCallback(keepCallback);
            callbackContext.sendPluginResult(result);
            if (!keepCallback) {
                callbackContext = null;
            }
        }
    }


    
    /**
     * The webview client receives notifications about appView
     */
    public class InAppBrowserClient extends WebViewClient {
        EditText edittext;
        CordovaWebView webView;
        
        String urlReload; //added by steven. to ensure cache page only be reload once when it does not exist

        AlertDialog alertDialog;
        WebView viewRef;
        /**
         * Constructor.
         *
         * @param mContext
         * @param edittext
         */
        public InAppBrowserClient(CordovaWebView webView, EditText mEditText) {
            this.webView = webView;
            this.edittext = mEditText;
            this.urlReload = "";
            this.alertDialog = new AlertDialog.Builder(cordova.getActivity()).create();
            this.viewRef = null;
           
        }

		/**
         * Give the host application a chance to take over the control when a new url is about to be loaded in the current WebView.
         *
         * @param view          The webview initiating the callback.
         * @param url           The url of the page.
         */
        
//        @Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        	System.out.println("^^^^^^^^^^^^^^^^ shouldOverrideUrlLoading");
//            //added by steven 18-06-2014
//            //if the link is ad, open in external browser
////            if(url.contains("adclick.g.doubleclick.net")){
//////            	view.stopLoading();
//////            	if(view.canGoBack()){
//////            	view.goBack();
//////            	view.goForward();
//////            	}
////            	openExternal(url);
////            	return true;
////            }
//			return super.shouldOverrideUrlLoading(view, url);
//		}



		/**
         * Notify the host application that a page has started loading.
         *
         * @param view          The webview initiating the callback.
         * @param url           The url of the page.
         */
        @Override
        public void onPageStarted(WebView view, String url,  Bitmap favicon) {
        	System.out.println("Coming to onPageStarted");
        	System.out.println(url);
            super.onPageStarted(view, url, favicon);
            String newloc = "";

//            // if link belong to disqus, open in external browser
//            if ((url.startsWith("https://disqus.com/") )||(url.startsWith("http://disqus.com/_ax/")) 
//            		||(url.startsWith("https://www.google.com/account"))
//            		||(url.startsWith("https://accounts.google.com/"))){
//            	
////        		Intent i = new Intent(Intent.ACTION_VIEW);
////        		i.setData(Uri.parse(url));
////
////        		InAppBrowser.this.cordova.getActivity().startActivity(i);
//            	openExternal(url);
//            }else
//            //add end
            if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("file:")) {
                newloc = url;
            } 
            // If dialing phone (tel:5551212)
            else if (url.startsWith(WebView.SCHEME_TEL)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    cordova.getActivity().startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error dialing " + url + ": " + e.toString());
                }
            }

            else if (url.startsWith("geo:") || url.startsWith(WebView.SCHEME_MAILTO) || url.startsWith("market:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    cordova.getActivity().startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error with " + url + ": " + e.toString());
                }
            }
            // If sms:5551212?body=This is the message
            else if (url.startsWith("sms:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Get address
                    String address = null;
                    int parmIndex = url.indexOf('?');
                    if (parmIndex == -1) {
                        address = url.substring(4);
                    }
                    else {
                        address = url.substring(4, parmIndex);

                        // If body, then set sms body
                        Uri uri = Uri.parse(url);
                        String query = uri.getQuery();
                        if (query != null) {
                            if (query.startsWith("body=")) {
                                intent.putExtra("sms_body", query.substring(5));
                            }
                        }
                    }
                    intent.setData(Uri.parse("sms:" + address));
                    intent.putExtra("address", address);
                    intent.setType("vnd.android-dir/mms-sms");
                    cordova.getActivity().startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error sending sms " + url + ":" + e.toString());
                }
            }
            else {
                newloc = "http://" + url;
            }

            if (!newloc.equals(edittext.getText().toString())) {
                edittext.setText(newloc);
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_START_EVENT);
                obj.put("url", newloc);
                Log.d(LOG_TAG, "newloc ======== "+newloc);
    
                sendUpdate(obj, true);
            } catch (JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        }
        
        
        
        public void onPageFinished(WebView view, String url) {
        	
//        	isCacheReloaded = false;
        	
        	
        	System.out.println("Coming to onPageFinished");
        	System.out.println(url);
            super.onPageFinished(view, url);
            
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_STOP_EVENT);
                obj.put("url", url);
    
                sendUpdate(obj, true);
            } catch (JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        }
        
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	System.out.println("Coming to onReceivedError");
        	System.out.println(failingUrl);
        	Log.i(LOG_TAG,"=====Coming to onReceivedError: "+failingUrl+"====errorCode===="+errorCode);
        	//closeDialog();
        	
        	
        	//added by steven 01-07-14 to deal with the error responce, force webview to use cache
        	if(!isNetworkAvailable()){
        	
	        	this.viewRef = view;
	        	
	            try {
	            	view.stopLoading();
	            } catch (Exception e) {
	            }
//	            try {
//	            	view.loadUrl("about:blank") ;
//	            } catch (Exception e) {
//	            }
//	            if (view.canGoBack()) {
//	            	view.goBack();
//	            }

	
	            this.viewRef = view;
	            alertDialog.setTitle("Oops!");
	            alertDialog.setMessage("This page cannot be browsed in offline mode! Please use back key to go Back.");
	            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	
	            	   if(viewRef!=null){
//	            		   while(viewRef.getUrl().equals("about:blank")){
		            		   if(viewRef.canGoBack())
		            			   	viewRef.goBack();
//	            		   }
//	            		   goBackInWebView(viewRef);
	            	   }
	            	   alertDialog.dismiss();
	               }
	            });
	
	            alertDialog.show();
        	}

            super.onReceivedError(view, errorCode, description, failingUrl);
            
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_ERROR_EVENT);
                obj.put("url", failingUrl);
                obj.put("code", errorCode);
                obj.put("message", description);
    
                sendUpdate(obj, true, PluginResult.Status.ERROR);
            } catch (JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        	
        }
    }
    
    public void goBackInWebView(WebView wv){
        WebBackForwardList history = webView.copyBackForwardList();
        int index = -1;
        String url = null;

        try{
	        while (wv.canGoBackOrForward(index)) {
	              if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
	            	  wv.goBackOrForward(index);
	                 url = history.getItemAtIndex(-index).getUrl();
	                 Log.e("tag","first non empty" + url);
	                 break;
	               }
	               index --;
	
	        }
        }catch(Exception e){
//        	Log.i(LOG_TAG,e.getCause().toString());
        }
//       // no history found that is not empty
//       if (url == null) {
//          finish();
//       }

    }


    private boolean isNetworkAvailable() {
    	  boolean isConnected = false;
    	  ConnectivityManager check = (ConnectivityManager)cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    	  if (check != null){ 
    	    NetworkInfo[] info = check.getAllNetworkInfo();
    	    if (info != null){
    	     for (int i = 0; i <info.length; i++){
    	       if (info[i].getState() == NetworkInfo.State.CONNECTED){
    	         isConnected = true;
    	       }
    	     }
    	    }
    	  }
    	    return isConnected;
    	  
    	 }

	public void updateNetworkStatus(String type) {
		Log.i(LOG_TAG,"============>  type ="+type);
		
		if(type.equals("none")){
			this.inAppWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}else{
			this.inAppWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		}
		
	}
}
