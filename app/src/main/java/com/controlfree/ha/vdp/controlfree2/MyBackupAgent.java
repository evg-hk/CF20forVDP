package com.controlfree.ha.vdp.controlfree2;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.controlfree.ha.vdp.controlfree2.utils.SharedPref;

import java.io.IOException;

public class MyBackupAgent extends BackupAgentHelper {
    private static final String TAG = "MyBackupAgent";
    static final String PREFS_BACKUP_KEY = "com.controlfree.ha.vdp.controlfree2";
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, SharedPref.PREF_NAME);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);
        Log.e(TAG, "onBackup: "+data.toString());
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        Log.e(TAG, "onRestore: "+data.toString());
    }
}
