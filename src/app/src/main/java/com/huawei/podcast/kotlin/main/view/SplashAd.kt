/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.kotlin.main.view
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.AudioFocusType
import com.huawei.hms.ads.splash.SplashAdDisplayListener
import com.huawei.hms.ads.splash.SplashView.SplashAdLoadListener
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.podcast.R
import com.huawei.podcast.databinding.ActivitySplashBinding

class SplashAd : AppCompatActivity() {

    private lateinit var activitySplashBinding: ActivitySplashBinding
    private val TAG = SplashAd::class.java.simpleName
    private var instance: HiAnalyticsInstance? = null

    /**
     * Pause flag.
     * On the splash ad screen:
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     * Set this parameter to false when returning to the splash ad screen from another screen to ensure that the app home screen can be displayed properly.
     */
    private var hasPaused = false

    // Callback handler used when the ad display timeout message is received.
    private val timeoutHandler = Handler {
        if (this.hasWindowFocus()) {
            jump()
        }
        false
    }

    private val splashAdLoadListener: SplashAdLoadListener = object : SplashAdLoadListener() {
        override fun onAdLoaded() {
            // Call this method when an ad is successfully loaded.
            Log.i(TAG, getString(R.string.splash_ad))
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            // Call this method when an ad fails to be loaded.
            Toast.makeText(this@SplashAd, getString(R.string.status_load_ad_fail) + errorCode, Toast.LENGTH_SHORT).show()
            jump()
        }

        override fun onAdDismissed() {
            // Call this method when the ad display is complete.
            jump()
        }
    }

    private val adDisplayListener: SplashAdDisplayListener = object : SplashAdDisplayListener() {
        override fun onAdShowed() {
            // Call this method when an ad is displayed.
        }

        override fun onAdClick() {
            // Call this method when an ad is clicked.
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_splash)
        loadAd()
    }

    private fun loadAd() {
        val adParam = AdParam.Builder().build()
        activitySplashBinding.splashAdView.setAdDisplayListener(adDisplayListener)
        instance = HiAnalytics.getInstance(this)
        // Set a default app launch image.
        activitySplashBinding.splashAdView.setSloganResId(R.drawable.defaultlogo)
        // Set a logo image.
        activitySplashBinding.splashAdView.setLogoResId(R.mipmap.ic_launcher)
        // Set logo description.
        activitySplashBinding.splashAdView.setMediaNameResId(R.string.media_name)
        // Set the audio focus type for a video splash ad.
        activitySplashBinding.splashAdView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE)
        activitySplashBinding.splashAdView.load(getString(R.string.ad_id_splash), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, adParam, splashAdLoadListener)
        Log.i(TAG, "End to load ad")
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT.toLong())
    }

    /**
     * Switch from the splash ad screen to the app home screen when the ad display is complete.
     */
    private fun jump() {
        if (!hasPaused) {
            hasPaused = true
            startActivity(Intent(this@SplashAd, ChooseInterest::class.java))
            val mainHandler = Handler()
            mainHandler.postDelayed({ finish() }, THOUSAND)
        }
    }

    /**
     * Set this parameter to true when exiting the app to ensure that the app home screen is not displayed.
     */
    override fun onStop() {
        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        hasPaused = true
        super.onStop()
    }

    /**
     * Call this method when returning to the splash ad screen from another screen to access the app home screen.
     */
    override fun onRestart() {
        super.onRestart()
        hasPaused = false
        jump()
    }

    override fun onDestroy() {
        super.onDestroy()
        activitySplashBinding.splashAdView.destroyView()
    }

    override fun onPause() {
        super.onPause()
        activitySplashBinding.splashAdView.pauseView()
    }

    override fun onResume() {
        super.onResume()
        activitySplashBinding.splashAdView.resumeView()
    }

    companion object {
        // Ad display timeout interval, in milliseconds.
        private const val AD_TIMEOUT = 5000
        // Ad display timeout message flag.
        private const val MSG_AD_TIMEOUT = 1001
        private const val THOUSAND: Long = 1000
    }
}
