/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cmgapps.android.apprater.AppRater;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppRater mAppRater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppRater = ((SampleApp) getApplication()).getAppRater();

        if (mAppRater.checkForRating()) {
            mAppRater.show(this);
        }
    }


    public void showDialogClicked(View v) {
        mAppRater.show(this);
    }
}
