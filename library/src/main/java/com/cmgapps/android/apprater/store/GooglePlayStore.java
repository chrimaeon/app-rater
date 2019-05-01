/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater.store;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * The Google Play Store
 */
public class GooglePlayStore implements Store {

    private static final String STORE_URI = "market://details?id=";

    @Override
    public Uri getStoreUri(@NonNull Context context) {
        return Uri.parse(STORE_URI + context.getPackageName());
    }
}
