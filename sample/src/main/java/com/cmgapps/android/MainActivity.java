/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android;

import android.os.Bundle;
import android.view.View;

import com.cmgapps.android.apprater.AppRater;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppRater mAppRater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppRater = new AppRater.Builder(this).build();
        mAppRater.incrementUseCount();
    }

    public void showDialogClicked(View v) {
        mAppRater.show(this);
    }
}
