/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cmgapps.android.apprater.AppRater;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppRater appRater = AppRater.getInstance(this);
        appRater.incrementUseCount();
    }

    public void showDialogClicked(View v) {
        AppRater.getInstance(this).show(this);

    }
}
