/*
 * Copyright 2013 CellaSecure
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.washington.cs.cellasecure;

import java.io.IOException;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.washington.cs.cellasecure.Drive.OnConfigurationListener;
import edu.washington.cs.cellasecure.Drive.OnLockQueryResultListener;
import edu.washington.cs.cellasecure.bluetooth.DeviceConfiguration;
import edu.washington.cs.cellasecure.fragments.DriveConfigureFragment;
import edu.washington.cs.cellasecure.fragments.PasswordInputDialogFragment;

public class DriveManageActivity extends Activity implements 
        Drive.OnConnectListener, OnConfigurationListener, 
        PasswordInputDialogFragment.PasswordInputDialogListener, Drive.OnLockStateChangeListener {


    private static final String TAG = "DriveManageActivity";

    public static final String KEY_BUNDLE_LOCK_STATUS = "locked";
    public static final String KEY_BUNDLE_ENCRYPTION_LEVEL = "encryption_level";

    private static Drive mDrive;
    private int mEncryptionLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_manage);
        Bundle args = getIntent().getExtras();
        if (args == null) {
            throw new IllegalStateException("DriveManageActivity expects a bundled Drive as " +
                    "input" + ".");
        }
        mDrive = (Drive) args.get(Drive.KEY_BUNDLE_DRIVE);

        mDrive.setOnConnectListener(this);
        mDrive.setOnConfigurationListener(this);
        mDrive.setOnLockStateChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.device_manage, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // more cases here
            default:
                return false;
        }
    }

    @Override
    public void onConnect() {
        Log.e("Foo", "in onConnect");
        assert mDrive.isConnected();
        mDrive.readConfiguration();
    }

    @Override
    public void onConnectFailure(IOException connectException) {
        Log.e(TAG, "Failed to connect to drive", connectException);
        finish(); // DIE
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDrive.isConnected()) {
            mDrive.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mDrive.isConnected()) {
            Log.d(TAG, "Begin connect");
            mDrive.connect();
        }
    }

    @Override
    public void onConfigurationRead(DeviceConfiguration config, IOException e) {
        Log.e("Foo", "in onConfigurationRead");
        if (e == null) {
            for (String option : config.listOptions()) {
                mEncryptionLevel = Integer.valueOf(config.getOption(option));
                Log.e("Foo", "Encryption Level: " + mEncryptionLevel);
                if (mEncryptionLevel == 0) {
                    Log.d(TAG, "Unlocking without Password");
                    mDrive.unlock("", "", mEncryptionLevel);
                } else {
                    mDrive.setOnLockQueryResultListener(new OnLockQueryResultListener() {
                        @Override
                        public void onLockQueryResult(boolean status, IOException queryException) {
                            String message = status ? "Locked" : "Unlocked";
                            Log.d(TAG, "Device is " + message);
                            if (!status) {
                                mDrive.unlock("", "", mEncryptionLevel);
                            } else {
                                Log.d(TAG, "Begining Password Fragment");
                                PasswordInputDialogFragment pidFragment = new PasswordInputDialogFragment();
                                pidFragment.show(getFragmentManager(), "fragment_password_input");
                            }
                        }
                    });
                    mDrive.queryLockStatus();
                }
            }
        } else {
            Log.e(TAG, "Configuration read failed", e);
            finish();
            return;
        }
    }

    @Override
    public void onConfigurationWritten(IOException e) {
        if (e != null) {
            Log.e(TAG, "Config failed", e);
            finish();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment df, String password) {
        if (mEncryptionLevel == 1) {
            Log.d(TAG, "Unlocking with Password");
            mDrive.unlock(password, "", mEncryptionLevel);
        } else {
            Log.d(TAG, "Unlocking with Password and UUID");
            TelephonyManager tManager = 
                    (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();
            mDrive.unlock(password, uuid, mEncryptionLevel);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment df) {
        Log.d(TAG, "User clicked cancel");
        finish();
        return;
    }

    @Override
    public void onLockStateChanged(final boolean status, IOException lockStateException) {
        if (lockStateException == null) {
            Log.d(TAG, "Beginning lock state changed on UI thread");
            runOnUiThread(new Runnable() {
                public void run() {
                    if (status) {
                        Log.e(TAG, "Unlock failed");
                    } else {
                        FragmentManager fragman = getFragmentManager();
                        FragmentTransaction trans = fragman.beginTransaction();
                        Bundle args = new Bundle();
                        args.putParcelable(Drive.KEY_BUNDLE_DRIVE, mDrive);
                        args.putInt(KEY_BUNDLE_ENCRYPTION_LEVEL, mEncryptionLevel);
                        DriveConfigureFragment dcfrag = new DriveConfigureFragment();
                        dcfrag.setArguments(args);
                        trans.replace(R.id.drive_manage_fragment_container, dcfrag);
                        trans.commit();
                    }
                }
            });

        } else {
            Log.e(TAG, "Locking failed", lockStateException);
            // failed to unlock, do something fail-y
        }
    }
}
