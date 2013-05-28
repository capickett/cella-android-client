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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.washington.cs.cellasecure.Drive.OnConnectedListener;
import edu.washington.cs.cellasecure.bluetooth.Connection;
import edu.washington.cs.cellasecure.bluetooth.Connection.OnLockQueryListener;
import edu.washington.cs.cellasecure.fragments.DriveUnlockFragment;

public class DriveManageActivity extends Activity implements OnConnectedListener, OnLockQueryListener {

    private static Drive mDrive;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_manage);
        Bundle args = getIntent().getExtras();
        mDrive = (Drive) args.get(Drive.KEY_BUNDLE_DRIVE);
        mDrive.connect(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
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
    public void onConnected(Connection c) {
        if (c == null) {
            // TODO: what to do here?
            throw new IllegalArgumentException("Connection is null");
        } else {
            mDrive.setConnection(c);
            c.setOnLockQueryListener(this);
            c.getLockStatus();
        }
    }
    
    @Override
    public void isLocked(boolean status) {
        mDrive.setLockStatus(status);
        FragmentManager fragman = getFragmentManager();
        FragmentTransaction fragtrans = fragman.beginTransaction();
        DriveUnlockFragment dufrag = new DriveUnlockFragment();
        Bundle args = new Bundle();
        args.putParcelable(Drive.KEY_BUNDLE_DRIVE, mDrive);
        dufrag.setArguments(args);
        fragtrans.replace(R.id.drive_manage_fragment_container, dufrag);
        fragtrans.commit();
    }
}
