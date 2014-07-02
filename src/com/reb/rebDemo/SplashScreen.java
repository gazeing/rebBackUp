package com.reb.rebDemo;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

public class SplashScreen extends Activity {

	private static final int	SPLASH_DISPLAY_TIME	= 3000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ImageView im = new ImageView(this);
		im.setImageResource(R.drawable.splash);
		setContentView(im);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashScreen.this, REBOnline.class);
				SplashScreen.this.startActivity(intent);
				SplashScreen.this.finish();
			}
		}, SPLASH_DISPLAY_TIME);
	}
	
	@Override
	public void onBackPressed() {
		
	}
}
