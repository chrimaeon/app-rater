/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that utilizes usage count and time to open a rate dialog.
 * <p></p>
 * Use {@link #incrementUseCount()} on your main activity
 * {@link Activity#onCreate(Bundle)} implementation.
 * <p></p>
 * Then call {@link #checkForRating()} to check if the requirements are met to
 * show the dialog and finally call {@link #show(Activity)} to show the rating dialog
 */
public class AppRater {
    static final String APP_RATED = "rated";
    static final String DECLINED_RATE = "declined_rate";
    static final String REMIND_LATER_DATE = "remind_later_date";
    private static final String TAG = "AppRater";
    private static final String APP_RATE_FILE_NAME = "AppRater";
    private static final int LAUNCHES_UNTIL_PROMPT = 5;
    private static final long DAYS_UNTIL_PROMPT = 5 * DateUtils.DAY_IN_MILLIS;
    private static final long DAYS_UNTIL_REMIND_AGAIN = 2 * DateUtils.DAY_IN_MILLIS;
    private static final String FIRST_USE = "first_use";
    private static final String USE_COUNT = "use_count";
    private static final String TRACKING_VERSION = "tracking_version";
    private static final boolean RATER_DEBUG = false;
    private static AppRater sInstance = null;
    private SharedPreferences mPref;
    private Context mContext;
    private boolean mDebug = false;
    private int mLaunchesUntilPrompt;
    private long mDaysUntilPrompt;
    private long mDaysUntilRemindAgain;

    private AppRater(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        mContext = context.getApplicationContext();
        mPref = mContext.getSharedPreferences(APP_RATE_FILE_NAME, Context.MODE_PRIVATE);
        mLaunchesUntilPrompt = LAUNCHES_UNTIL_PROMPT;
        mDaysUntilPrompt = DAYS_UNTIL_PROMPT;
        mDaysUntilRemindAgain = DAYS_UNTIL_REMIND_AGAIN;
    }

    /**
     * Get a {@link AppRater} instance
     *
     * @param context the Application Context
     * @return The {@link AppRater} instance
     */
    public static AppRater getInstance(Context context) {
        synchronized (AppRater.class) {
            if (sInstance == null) {
                sInstance = new AppRater(context);
            }

            return sInstance;
        }
    }

    /**
     * @param launchesUntilPrompt the launchesUntilPrompt to set
     */
    public void setLaunchesUntilPrompt(int launchesUntilPrompt) {
        mLaunchesUntilPrompt = launchesUntilPrompt;
    }

    /**
     * @param daysUntilPrompt the daysUntilPrompt to set
     */
    public void setDaysUntilPrompt(long daysUntilPrompt) {
        mDaysUntilPrompt = daysUntilPrompt;
    }

    /**
     * @param daysUntilRemindAgain the daysUntilRemindAgain to set
     */
    public void setDaysUntilRemindAgain(long daysUntilRemindAgain) {
        mDaysUntilRemindAgain = daysUntilRemindAgain;
    }

    /**
     * Sets the debug flag to display current <code>CmgAppRater</code> field
     * values on {@link #checkForRating()}
     *
     * @param debug true to display debug output
     */
    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    /**
     * Call to check if the requirements to open the rating dialog are met
     * <p></p>
     * <b>NOTICE:</b> This method is thread safe
     *
     * @return true if requirements are met.
     */
    public synchronized boolean checkForRating() {

        if (mDebug)
            Log.i(TAG, "Rater Content:" + toString());

        return RATER_DEBUG ||
                !mPref.getBoolean(DECLINED_RATE, false) &&
                        !mPref.getBoolean(APP_RATED, false) &&
                        System.currentTimeMillis() >= (mPref.getLong(FIRST_USE, 0L) + mDaysUntilPrompt) &&
                        mPref.getInt(USE_COUNT, 0) > mLaunchesUntilPrompt && System.currentTimeMillis() >= (mPref.getLong(REMIND_LATER_DATE, 0L) + mDaysUntilRemindAgain);

    }

    /**
     * Increments the usage count.
     * <p></p>
     * <b>NOTICE:</b> This method is thread safe
     */
    public synchronized void incrementUseCount() {

        Editor editor = mPref.edit();
        int versionCode;

        try {
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException exc) {
            versionCode = 0;
            Log.e(TAG, "PackageName not found: " + mContext.getPackageName());
        }

        int trackingVersion = mPref.getInt(TRACKING_VERSION, -1);

        if (trackingVersion == -1) {
            trackingVersion = versionCode;
            editor.putInt(TRACKING_VERSION, trackingVersion);
        }

        if (trackingVersion == versionCode) {

            if (mPref.getLong(FIRST_USE, 0L) == 0L)
                editor.putLong(FIRST_USE, System.currentTimeMillis());

            editor.putInt(USE_COUNT, mPref.getInt(USE_COUNT, 0) + 1);
        } else {
            editor.putInt(TRACKING_VERSION, versionCode).putLong(FIRST_USE, System.currentTimeMillis()).putInt(USE_COUNT, 1)
                    .putBoolean(DECLINED_RATE, false).putLong(REMIND_LATER_DATE, 0L).putBoolean(APP_RATED, false);
        }

        editor.apply();
    }

    /**
     * Get the {@link SharedPreferences} used to save state
     *
     * @return The SharedPreferences
     */
    public SharedPreferences getPreferences() {
        return mPref;
    }

    /**
     * Shows a default {@link AlertDialog}. Must be called from main thread
     *
     * @param activity An {@link Activity} to show the dialog from.
     */
    public void show(@NonNull final Activity activity) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("AppRater.show() must be called from main thread");
        }

        activity.startActivity(new Intent(activity, AppRaterActivity.class));
        activity.overridePendingTransition(0, 0);
    }

    private static String ratePreferenceToString(SharedPreferences pref) {
        JSONObject thiz = new JSONObject();
        try {
            thiz.put(DECLINED_RATE, pref.getBoolean(DECLINED_RATE, false));
            thiz.put(APP_RATED, pref.getBoolean(APP_RATED, false));
            thiz.put(TRACKING_VERSION, pref.getInt(TRACKING_VERSION, -1));
            thiz.put(FIRST_USE, SimpleDateFormat.getDateTimeInstance().format(new Date(pref.getLong(FIRST_USE, 0L))));
            thiz.put(USE_COUNT, pref.getInt(USE_COUNT, 0));
            thiz.put(REMIND_LATER_DATE,
                    SimpleDateFormat.getDateTimeInstance().format(new Date(pref.getLong(REMIND_LATER_DATE, 0L))));
        } catch (JSONException exc) {
            Log.e(TAG, "Error creating JSON Object", exc);
        }

        return thiz.toString();
    }

    /**
     * Get the {@link SharedPreferences} file contents
     */
    @Override
    public String toString() {
        return ratePreferenceToString(mPref);
    }
}
