/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater.store;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * The Amazon Store
 */
public class AmazonStore implements Store {

    private static final String STORE_URL = "http://www.amazon.com/gp/mas/dl/android?p=";


    @Override
    public Uri getStoreUri(@NonNull Context context) {
        return Uri.parse(STORE_URL + context.getPackageName());
    }
}
