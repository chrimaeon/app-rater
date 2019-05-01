/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

import com.cmgapps.android.apprater.store.GooglePlayStore;
import com.cmgapps.android.apprater.store.Store;

import androidx.annotation.NonNull;

/**
 * <p>
 * Class that utilizes usage count and time to open a rate dialog.
 * </p>
 * <p>
 * Use {@link #incrementUseCount()} on your main activity
 * {@link Activity#onCreate(Bundle)} implementation.
 * </p>
 * Then call {@link #checkForRating()} to check if the requirements are met to
 * show the dialog and finally call {@link #show(Activity)} to show the rating dialog
 */
public class AppRater {

    private static final String TAG = "AppRater";

    private final boolean mDebug;
    private final int mLaunchesUntilPrompt;
    private final long mDaysUntilPrompt;
    private final long mDaysUntilRemindAgain;
    private final Store mStore;
    private final PreferenceManager mPreferenceManager;

    private int mVersionCode;

    private AppRater(@NonNull Builder builder) {

        final Context context = builder.mContext;

        mPreferenceManager = new PreferenceManager(builder.mContext);

        try {
            mVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException exc) {
            mVersionCode = 0;
            Log.e(TAG, "PackageName not found: " + context.getPackageName());
        }

        mStore = builder.mStore;
        mLaunchesUntilPrompt = builder.mLaunchesUntilPrompt;
        mDaysUntilPrompt = builder.mDaysUntilPrompt;
        mDaysUntilRemindAgain = builder.mDaysUntilRemindAgain;
        mDebug = builder.mDebug;
    }

    /**
     * <p>
     * Call to check if the requirements to open the rating dialog are met
     * </p>
     *
     * @return true if requirements are met.
     */
    public boolean checkForRating() {

        if (mDebug) {
            Log.i(TAG, "Rater Content:" + toString());
        }

        return !mPreferenceManager.getDeclinedToRate() &&
                !mPreferenceManager.getAppRated() &&
                System.currentTimeMillis() >= (mPreferenceManager.getFirstUsedTimestamp() + mDaysUntilPrompt) &&
                mPreferenceManager.getUseCount() > mLaunchesUntilPrompt &&
                System.currentTimeMillis() >= (mPreferenceManager.getRemindLaterTimeStamp() + mDaysUntilRemindAgain);
    }

    /**
     * <p>
     * Increments the usage count.
     * </p>
     */
    public void incrementUseCount() {

        int trackingVersion = mPreferenceManager.getTrackingVersion();

        if (trackingVersion == -1) {
            trackingVersion = mVersionCode;
            mPreferenceManager.setTrackingVersion(trackingVersion);
        }

        if (trackingVersion == mVersionCode) {

            if (mPreferenceManager.getUseCount() == 0L) {
                mPreferenceManager.setFirstUsedTimestamp(System.currentTimeMillis());
            }

            mPreferenceManager.incrementUseCount();
        } else {
            mPreferenceManager.resetNewVersion(mVersionCode);
        }
    }

    /**
     * Shows a default {@link AlertDialog}.
     *
     * @param activity An {@link Activity} to show the dialog from.
     */
    public void show(@NonNull final Activity activity) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(activity, AppRaterActivity.class);
                intent.putExtra(AppRaterActivity.EXTRA_STORE_URI, mStore.getStoreUri(activity));
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    /**
     * Get the {@link SharedPreferences} file contents
     */
    @Override
    @NonNull
    public String toString() {
        return mPreferenceManager.toString();
    }

    @SuppressWarnings("unused")
    public static final class Builder {

        final Context mContext;
        Store mStore = new GooglePlayStore();
        int mLaunchesUntilPrompt = 5;
        long mDaysUntilPrompt = 10 * DateUtils.DAY_IN_MILLIS;
        long mDaysUntilRemindAgain = 5 * DateUtils.DAY_IN_MILLIS;
        boolean mDebug = false;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        @NonNull
        public Builder setStore(@NonNull Store store) {
            mStore = store;
            return this;
        }

        @NonNull
        public Builder setLaunchesUntilPrompt(int launchesUntilPrompt) {
            mLaunchesUntilPrompt = launchesUntilPrompt;
            return this;
        }

        @NonNull
        public Builder setDaysUntilPrompt(int daysUntilPrompt) {
            mDaysUntilPrompt = daysUntilPrompt * DateUtils.DAY_IN_MILLIS;
            return this;
        }

        @NonNull
        public Builder setDaysUntilRemindAgain(int daysUntilRemindAgain) {
            mDaysUntilRemindAgain = daysUntilRemindAgain * DateUtils.DAY_IN_MILLIS;
            return this;
        }

        @NonNull
        public Builder setDebug(boolean debug) {
            mDebug = debug;
            return this;
        }

        @NonNull
        public AppRater build() {
            return new AppRater(this);
        }
    }
}
