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

package com.reb.rebDemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import org.apache.cordova.*;

import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;

public class REBOnline extends CordovaActivity
{
	// declare the original size of the iPad app
	 protected float ORIG_APP_W = 1536;
	 protected float ORIG_APP_H = 2048;
    @SuppressWarnings({ "deprecation", "static-access" })
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
      
        super.init();
        super.loadUrl("file:///android_asset/www/index.html");
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        System.out.println("AppCache --> "+ appCachePath);
        try {
			System.out.println("AppCache full path --> "+ getApplicationContext().getFilesDir());
		} catch (Exception e) {
			// TODO Auto-generated catch block00
			e.printStackTrace();
		}
        
        //set some defaults
        super.appView.setBackgroundColor(Color.rgb(108, 196, 23));
        super.appView.setHorizontalScrollBarEnabled(false);
        super.appView.setHorizontalScrollbarOverlay(false);
        super.appView.setVerticalScrollBarEnabled(false);
        super.appView.setVerticalScrollbarOverlay(false);
        
        int deviceOrientation=getResources().getConfiguration().orientation;
        
        // get actual screen size
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        
        // calculate target scale (only dealing with portrait orientation)
        double globalScale_Width = Math.floor( ( width / ORIG_APP_W ) * 100 );
        double globalScale_Height = Math.floor( ( height / ORIG_APP_H ) * 100 );
        
        //Logging
        System.out.println( "ORIG_APP_W ="+ ORIG_APP_W );
        System.out.println( "ORIG_APP_H = "  + ORIG_APP_H );
        System.out.println( "super.appView.getMeasuredWidth() = " + width);
        System.out.println( "super.appView.getMeasuredHeight() =" + height );
        System.out.println( "globalScale_Width = " + (int)globalScale_Width );
        System.out.println( "globalScale_Height = " + (int)globalScale_Height);
        
        //set some defaults on the web view
        super.appView.getSettings().setBuiltInZoomControls( false );
        super.appView.getSettings().setSupportZoom(false);
        super.appView.getSettings().setLightTouchEnabled(true);
        
        // set the scale based on orientation
        if(deviceOrientation==getResources().getConfiguration().ORIENTATION_PORTRAIT){
        	super.appView.setInitialScale( (int)globalScale_Height);
        }else if(deviceOrientation==getResources().getConfiguration().ORIENTATION_LANDSCAPE){
        	super.appView.setInitialScale( (int)globalScale_Width);
        }
        super.appView.getSettings().setAppCacheEnabled(true);
        super.appView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        super.appView.getSettings().setDomStorageEnabled(true);
        super.appView.getSettings().setAppCachePath(appCachePath);
        super.appView.getSettings().setAllowFileAccess(true);
    }
}
