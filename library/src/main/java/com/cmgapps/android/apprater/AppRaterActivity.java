/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * App Rater Dialog Activity
 * <p>
 * Declare in AndroidManifest.xml with dialog theme
 * <pre>{@code
 *   <activity
 *     android:name="com.cmgapps.android.apprater.AppRaterActivity"
 *     android:theme="@style/Theme.AppCompat.Light.Dialog" />
 * }</pre>
 */
public class AppRaterActivity extends AppCompatActivity {

    private static final String TAG = "AppRaterActivity";
    private boolean mButtonClicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_rater);
        setTitle(R.string.dialog_cmgrate_title);

        PackageManager pm = getPackageManager();

        String appName;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
            appName = (String) pm.getApplicationLabel(ai);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Application name can not be found");
            appName = "App";
        }

        //noinspection ConstantConditions
        ((TextView) findViewById(R.id.dialogMessage)).setText(getString(R.string.dialog_cmgrate_message_fmt, appName));
    }

    public void negativeClick(View view) {
        AppRater.getInstance(this).getPreferences().edit().putBoolean(AppRater.DECLINED_RATE, true).apply();
        mButtonClicked = true;
        finish();
    }

    public void laterClick(View view) {
        AppRater.getInstance(this).getPreferences().edit().putLong(AppRater.REMIND_LATER_DATE, System.currentTimeMillis()).apply();
        mButtonClicked = true;
        finish();
    }

    public void positiveClick(View view) {
        AppRater.getInstance(this).getPreferences().edit().putBoolean(AppRater.APP_RATED, true).apply();
        mButtonClicked = true;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        if (!mButtonClicked) {
            AppRater.getInstance(this).getPreferences().edit().putLong(AppRater.REMIND_LATER_DATE, System.currentTimeMillis()).apply();
        }
        super.finish();
    }
}
