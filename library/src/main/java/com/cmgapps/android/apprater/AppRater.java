/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com>
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

package com.cmgapps.android.apprater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cmgapps.android.apprater.store.GooglePlayStore;
import com.cmgapps.android.apprater.store.Store;

public class AppRater {

    private static final String TAG = "AppRater";

    private final boolean mDebug;
    private final int mLaunchesUntilPrompt;
    private final long mDaysUntilPrompt;
    private final long mDaysUntilRemindAgain;
    private final Store mStore;
    private final PreferenceManager mPreferenceManager;

    private long mVersionCode;

    private AppRater(@NonNull Builder builder) {

        final Context context = builder.mContext;

        mPreferenceManager = new PreferenceManager(builder.mContext);

        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                mVersionCode = packageInfo.versionCode;
            } else {
                mVersionCode = packageInfo.getLongVersionCode();
            }
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

        long trackingVersion = mPreferenceManager.getTrackingVersion();

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
        final PackageManager pm = activity.getPackageManager();

        String appName;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(activity.getPackageName(), 0);
            appName = (String) pm.getApplicationLabel(ai);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Application name can not be found");
            appName = "App";
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_cmgrate_title)
                .setMessage(activity.getString(R.string.dialog_cmgrate_message_fmt, appName))
                .setPositiveButton(R.string.dialog_cmgrate_ok, (dialog, which) -> {
                    mPreferenceManager.setAppRated(true);

                    Intent intent = new Intent(Intent.ACTION_VIEW, mStore.getStoreUri(activity));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                })
                .setNeutralButton(R.string.dialog_cmgrate_later, (dialog, which) -> mPreferenceManager.setRemindLaterTimeStamp(System.currentTimeMillis()))
                .setNegativeButton(R.string.dialog_cmgrate_no, (dialog, which) -> mPreferenceManager.setDeclinedToRate(true))
                .setOnCancelListener(dialog -> mPreferenceManager.setRemindLaterTimeStamp(System.currentTimeMillis()))
                .show();
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

        /**
         * Set the store to open for rating
         */
        @NonNull
        public Builder setStore(@NonNull Store store) {
            mStore = store;
            return this;
        }

        /**
         * Sets the minimun app lauched until the dialog is shown
         */
        @NonNull
        public Builder setLaunchesUntilPrompt(int launchesUntilPrompt) {
            mLaunchesUntilPrompt = launchesUntilPrompt;
            return this;
        }

        /**
         * Set the minimul days to pass until the dialig is shown
         */
        @NonNull
        public Builder setDaysUntilPrompt(int daysUntilPrompt) {
            mDaysUntilPrompt = daysUntilPrompt * DateUtils.DAY_IN_MILLIS;
            return this;
        }

        /**
         * Set the days until the dialog is shown again
         */
        @NonNull
        public Builder setDaysUntilRemindAgain(int daysUntilRemindAgain) {
            mDaysUntilRemindAgain = daysUntilRemindAgain * DateUtils.DAY_IN_MILLIS;
            return this;
        }

        /**
         * Enables debug mode with Logcat output id the current state
         */
        @NonNull
        public Builder setDebug(boolean debug) {
            mDebug = debug;
            return this;
        }

        /**
         * creates the App Rater instance
         */
        @NonNull
        public AppRater build() {
            return new AppRater(this);
        }
    }
}
