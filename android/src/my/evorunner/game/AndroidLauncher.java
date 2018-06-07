package my.evorunner.game;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Version 1.0
 * Last Edited: Matthew Phan
 * Changelog 1.0
 * Fixed ad system so that it properly displays
 * Got rid of some unneeded methods for ads
 */
public class AndroidLauncher extends AndroidApplication implements AdHandler {
	//Tag for the Logcat
	private static final String TAG = "AndroidLauncher";
	protected AdView adView;
	//Google unit id for ads
	private static final String BANNER_AD_UNIT_ID = "ca-app-pub-5519384153835422/2811367393";

	//Ints for showing and hiding ads
	private static final int SHOW_ADS = 1;
	private static final int HIDE_ADS = 0;


	@SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
		@Override
		//Takes in a message and switches between turning ads off or on
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SHOW_ADS:
					adView.setVisibility(View.VISIBLE);
					break;
				case HIDE_ADS:
					adView.setVisibility(View.INVISIBLE);			}
		}
	};

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Declare new layout with the current game object
		RelativeLayout layout = new RelativeLayout(this);
		//Initialize a new configuration for the application
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new EvoRunner(this,new AndroidPlatform(this)));
		//View for the current game with the config
		View gameView = initializeForView(new EvoRunner(this,new AndroidPlatform(this)), config);

		//Initialize adView with the current object
		adView = new AdView(this);
		//Adds a listener which will print to log when the ad is visible
		adView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				int visibility = adView.getVisibility();
				adView.setVisibility(AdView.GONE);
				adView.setVisibility(visibility);
				Log.i(TAG, "Ad Loaded...");
			}
		});

		layout.addView(gameView);

		//Sets the ad to be a banner (less config on our part for fitting the ad)
		adView.setAdSize(AdSize.SMART_BANNER); 	//Need to change this based on phone
		adView.setAdUnitId(BANNER_AD_UNIT_ID);

		//New builder for ads
		AdRequest.Builder builder = new AdRequest.Builder();
		RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
				//X and Y coordinates, telling the ad to wrap its content
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);

		//Add the ad and show it
		layout.addView(adView, adParams);
		adView.loadAd(builder.build());
		setContentView(layout);
	}

    @Override
    public void showAds(boolean show) {
		handler.sendEmptyMessage(show ? SHOW_ADS : HIDE_ADS);
    }

	@Override
	public int getBannerHeight() {
		if(Gdx.graphics.getHeight() < 400) {
			return 32;
		} else if (Gdx.graphics.getHeight() <= 720) {
			return 50;
		} else {
			return 90;
		}
	}
}
