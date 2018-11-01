package com.vuitv.soundrecorder.listener;

/**
 * Created by vuitv on 10/6/2018.
 */

public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
}
