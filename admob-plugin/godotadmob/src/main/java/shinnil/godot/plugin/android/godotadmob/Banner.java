package shinnil.godot.plugin.android.godotadmob;

import android.app.Activity;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import org.godotengine.godot.GodotLib;

public class Banner {
    private AdView adView = null; // Banner view
    private FrameLayout layout = null;
    private FrameLayout.LayoutParams adParams = null;
    private AdRequest adRequest = null;
    private Activity activity = null;

    public Banner(final String id, final AdRequest adRequest, final Activity activity, final int instanceId, final boolean isOnTop, final FrameLayout layout) {
        this.activity = activity;
        this.layout = layout;
        this.adRequest = adRequest;

        adParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        if (isOnTop) adParams.gravity = Gravity.TOP;
        else adParams.gravity = Gravity.BOTTOM;

        if (adView != null) {
            layout.removeView(adView); // Remove the old view
        }

        adView = new AdView(activity);
        adView.setAdUnitId(id);

        adView.setBackgroundColor(Color.TRANSPARENT);

        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.w("godot", "AdMob: onAdLoaded");
                GodotLib.calldeferred(instanceId, "_on_admob_ad_loaded", new Object[]{});
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.w("godot", "AdMob: onAdFailedToLoad. errorCode: " + errorCode);
                GodotLib.calldeferred(instanceId, "_on_admob_banner_failed_to_load", new Object[]{errorCode});
            }
        });
        layout.addView(adView, adParams);

        // Request
        adView.loadAd(adRequest);

    }

    public void show() {
        if (adView == null) {
            Log.w("w", "AdMob: showBanner - banner not loaded");
            return;
        }

        if (adView.getVisibility() == View.VISIBLE) {
            return;
        }

        adView.setVisibility(View.VISIBLE);
        adView.resume();
        Log.d("godot", "AdMob: Show Banner");
    }

    public void resize() {
        if (layout == null || adView == null || adParams == null) {
            return;
        }

        layout.removeView(adView); // Remove the old view

        // Extract params

        int gravity = adParams.gravity;
        adParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        adParams.gravity = gravity;
        AdListener adListener = adView.getAdListener();
        String id = adView.getAdUnitId();

        // Create new view & set old params
        adView = new AdView(activity);
        adView.setAdUnitId(id);
        adView.setBackgroundColor(Color.TRANSPARENT);
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.setAdListener(adListener);

        // Add to layout and load ad
        layout.addView(adView, adParams);

        // Request
        adView.loadAd(adRequest);

        Log.d("godot", "AdMob: Banner Resized");
    }


    public void hide() {
        if (adView.getVisibility() == View.GONE) return;
        adView.setVisibility(View.GONE);
        adView.pause();
        Log.d("godot", "AdMob: Hide Banner");
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public int getWidth() {
        return getAdSize().getWidthInPixels(activity);
    }

    public int getHeight() {
        return getAdSize().getHeightInPixels(activity);
    }


}