/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * App Rater Dialog Activity
 */
public class AppRaterActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_URI = BuildConfig.APPLICATION_ID + ".extra.STORE_URI";
    private static final String TAG = "AppRaterActivity";
    /*used by inner class*/ boolean mButtonClicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            RaterFragment raterFragment = new RaterFragment();
            raterFragment.setArguments(getIntent().getExtras());
            raterFragment.show(getSupportFragmentManager(), "CMGAppsRaterFragment");
        }
    }

    @Override
    public void finish() {
        if (!mButtonClicked) {
            AppRater.getInstance(this).getPreferences().edit().putLong(AppRater.REMIND_LATER_DATE, System.currentTimeMillis()).apply();
        }
        super.finish();
        overridePendingTransition(0, 0);
    }

    public static class RaterFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            PackageManager pm = getContext().getPackageManager();

            String appName;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(getContext().getPackageName(), 0);
                appName = (String) pm.getApplicationLabel(ai);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Application name can not be found");
                appName = "App";
            }

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_cmgrate_title)
                    .setMessage(getString(R.string.dialog_cmgrate_message_fmt, appName))
                    .setPositiveButton(R.string.dialog_cmgrate_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppRaterActivity activity = (AppRaterActivity) getActivity();
                            AppRater.getInstance(activity).getPreferences().edit().putBoolean(AppRater.APP_RATED, true).apply();
                            activity.mButtonClicked = true;
                            Uri storeUri = getArguments().getParcelable(EXTRA_STORE_URI);
                            Intent intent = new Intent(Intent.ACTION_VIEW, storeUri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            activity.finish();
                        }
                    })
                    .setNeutralButton(R.string.dialog_cmgrate_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppRaterActivity activity = (AppRaterActivity) getActivity();
                            AppRater.getInstance(activity).getPreferences().edit().putLong(AppRater.REMIND_LATER_DATE, System.currentTimeMillis()).apply();
                            activity.mButtonClicked = true;
                            activity.finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cmgrate_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppRaterActivity activity = (AppRaterActivity) getActivity();
                            AppRater.getInstance(activity).getPreferences().edit().putLong(AppRater.REMIND_LATER_DATE, System.currentTimeMillis()).apply();
                            activity.mButtonClicked = true;
                            activity.finish();
                        }
                    })
                    .create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
        }
    }
}
