/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

/**
 * App Rater Dialog Activity
 */
public class AppRaterActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_URI = "com.cmgapps.android.apprater.extra.STORE_URI";
    private static final String TAG = "AppRaterActivity";
    boolean mButtonClicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            RaterFragment.newInstance(getIntent().getExtras())
                    .show(getSupportFragmentManager(), "CMGAppsRaterFragment");
        }
    }

    @Override
    public void finish() {
        if (!mButtonClicked) {
            new PreferenceManager(this).setRemindLaterTimeStamp(System.currentTimeMillis());
        }
        super.finish();
        overridePendingTransition(0, 0);
    }

    public static class RaterFragment extends DialogFragment {

        private PreferenceManager mPreferenceManager;

        static RaterFragment newInstance(Bundle extras) {
            RaterFragment raterFragment = new RaterFragment();
            raterFragment.setArguments(extras);
            return raterFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPreferenceManager = new PreferenceManager(requireContext());
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Context context = requireContext();
            final PackageManager pm = context.getPackageManager();

            String appName;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
                appName = (String) pm.getApplicationLabel(ai);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Application name can not be found");
                appName = "App";
            }

            return new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_cmgrate_title)
                    .setMessage(getString(R.string.dialog_cmgrate_message_fmt, appName))
                    .setPositiveButton(R.string.dialog_cmgrate_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppRaterActivity activity = (AppRaterActivity) getActivity();

                            if (activity == null) {
                                return;
                            }

                            mPreferenceManager.setAppRated(true);
                            activity.mButtonClicked = true;
                            Uri storeUri = null;

                            if (getArguments() != null) {
                                storeUri = getArguments().getParcelable(EXTRA_STORE_URI);
                            }

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

                            if (activity == null) {
                                return;
                            }
                            mPreferenceManager.setRemindLaterTimeStamp(System.currentTimeMillis());
                            activity.mButtonClicked = true;
                            activity.finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cmgrate_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppRaterActivity activity = (AppRaterActivity) getActivity();

                            if (activity == null) {
                                return;
                            }

                            mPreferenceManager.setDeclinedToRate(true);
                            activity.mButtonClicked = true;
                            activity.finish();
                        }
                    })
                    .create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            Activity activity = getActivity();

            if (activity != null) {
                activity.finish();
            }
        }
    }
}
