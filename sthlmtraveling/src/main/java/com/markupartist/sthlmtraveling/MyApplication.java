package com.markupartist.sthlmtraveling;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.markupartist.sthlmtraveling.service.DataMigrationService;
import com.markupartist.sthlmtraveling.utils.ErrorReporter;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    static String TAG = "StApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(getApplicationContext());

        reloadLocaleForApplication();

        DataMigrationService.startService(this);
    }

    /* (non-Javadoc)
     * @see android.app.Application#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadLocaleForApplication();
    }

    public void reloadLocaleForApplication() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String language = sharedPreferences
                .getString("prefered_language_preference", "system");
        Log.d(TAG, "Preferred language: " + language);

        Locale locale = null;
        switch (language) {
            case "sv":
                locale = new Locale("sv", "SE");
                break;
            case "es":
                locale = new Locale("es", "ES");
                break;
            case "nl":
                locale = new Locale("nl", "NL");
                break;
            case "en":
                locale = Locale.ENGLISH;
                break;
        }

        if (locale != null) {
            Log.d(TAG, "setting locale " + locale);
            Resources res = getResources(); 
            DisplayMetrics dm = res.getDisplayMetrics(); 
            Configuration conf = res.getConfiguration();
            conf.locale = locale;
            res.updateConfiguration(conf, dm);
        }
    }

    /**
     * Get analytics tracker.
     *
     * @return A Tracker
     */
    public synchronized Tracker getTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setDryRun(BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        }
        return analytics.newTracker(AppConfig.ANALYTICS_PROPERTY_KEY);
    }
}
