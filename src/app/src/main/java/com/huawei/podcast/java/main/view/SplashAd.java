/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.main.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.databinding.DataBindingUtil;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.podcast.R;
import com.huawei.podcast.databinding.ActivitySplashBinding;
import com.huawei.podcast.java.utils.Constants;

public class SplashAd extends Activity {
    private static final String TAG = SplashAd.class.getSimpleName();
    ActivitySplashBinding activitySplashBinding;
    HiAnalyticsInstance instance;



    /**
     * Pause flag.
     * On the splash ad screen:
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     * Set this parameter to false when returning to the splash ad screen from another screen to ensure that the app home screen can be displayed properly.
     */
    private boolean hasPaused = false;

    // Callback handler used when the ad display timeout message is received.
    private final Handler timeoutHandler =
            new Handler(
                    msg -> {
                        if (SplashAd.this.hasWindowFocus()) {
                            jump();
                        }
                        return false;
                    });

    private final SplashView.SplashAdLoadListener splashAdLoadListener =
            new SplashView.SplashAdLoadListener() {
                @Override
                public void onAdLoaded() {
                    // Call this method when an ad is successfully loaded.
                    // Toast.makeText(SplasAdActivity.this, getString(R.string.status_load_ad_success),
                    // Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Call this method when an ad fails to be loaded.
                    // Toast.makeText(SplashAdActivity.this, getString(R.string.status_load_ad_fail) + errorCode,
                    // Toast.LENGTH_SHORT).show();
                    jump();
                }

                @Override
                public void onAdDismissed() {
                    // Call this method when the ad display is complete.
                    // Toast.makeText(SplashAdActivity.this, getString(R.string.status_ad_dismissed),
                    // Toast.LENGTH_SHORT).show();
                    jump();
                }
            };

    private final SplashAdDisplayListener adDisplayListener =
            new SplashAdDisplayListener() {
                @Override
                public void onAdShowed() {
                    // Call this method when an ad is displayed.
                }

                @Override
                public void onAdClick() {
                    // Call this method when an ad is clicked.

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        loadAd();
    }

    private void loadAd() {
        AdParam adParam = new AdParam.Builder().build();
        activitySplashBinding.splashAdView.setAdDisplayListener(adDisplayListener);
        instance = HiAnalytics.getInstance(this);
        // Set a default app launch image.
        activitySplashBinding.splashAdView.setSloganResId(R.drawable.defaultlogo);
        // Set a logo image.
        activitySplashBinding.splashAdView.setLogoResId(R.mipmap.ic_launcher);
        // Set logo description.
        activitySplashBinding.splashAdView.setMediaNameResId(R.string.media_name);
        // Set the audio focus type for a video splash ad.
        activitySplashBinding.splashAdView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
        activitySplashBinding.splashAdView.load(
                getString(R.string.ad_id_splash),
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                adParam,
                splashAdLoadListener);
        Log.i(TAG, "End to load ad");
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(Constants.MSG_AD_TIMEOUT);
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(Constants.MSG_AD_TIMEOUT, Constants.AD_TIMEOUT);
    }

    /**
     * Switch from the splash ad screen to the app home screen when the ad display is complete.
     */
    private void jump() {
        if (!hasPaused) {
            hasPaused = true;
            startActivity(new Intent(SplashAd.this, ChooseInterest.class));
            Handler mainHandler = new Handler();
            mainHandler.postDelayed(this::finish, Constants.THOUSAND);
        }
    }

    /**
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     */
    @Override
    protected void onStop() {
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(Constants.MSG_AD_TIMEOUT);
        hasPaused = true;
        super.onStop();
    }

    /**
     * Call this method when returning to the splash ad screen from another screen to access the app home screen.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        hasPaused = false;
        jump();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activitySplashBinding.splashAdView.destroyView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activitySplashBinding.splashAdView.pauseView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activitySplashBinding.splashAdView.resumeView();
    }
}
