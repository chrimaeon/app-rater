/*
 * Copyright (c) 2016. Christian Grach <christian.grach@cmgapps.com
 */

package com.cmgapps.android.apprater.store;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * A Store to open for rating
 */
public interface Store {

    /**
     * @param context the application context
     * @return the store uri to open for rating
     */
    Uri getStoreUri(@NonNull Context context);
}
