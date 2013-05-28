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

import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import edu.washington.cs.cellasecure.fragments.DriveManageFragment;
import edu.washington.cs.cellasecure.fragments.DriveNoDevicesFragment;
import edu.washington.cs.cellasecure.storage.DeviceUtils;

public class DriveManageActivity extends Activity implements
        OnNavigationListener {

    private ArrayAdapter<Drive> mDropDownAdapter;
    private ProgressBar mDriveLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_manage);

        mDriveLoadingProgress = (ProgressBar) findViewById(R.id.drive_loading_progress);
        mDropDownAdapter = new ArrayAdapter<Drive>(this, android.R.layout.simple_list_item_1);
        new LoadDevicesTask().execute();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.device_list, menu);
        inflator.inflate(R.menu.device_manage, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Drive selectedDrive = mDropDownAdapter.getItem(itemPosition);
        Fragment frag = new DriveManageFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putParcelable(Drive.KEY_BUNDLE_DRIVE, selectedDrive);
        frag.setArguments(args);
        transaction.replace(R.id.drive_manage_fragment_container, frag);
        transaction.commit();
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
            case R.id.menu_connect_drive:
                startActivity(new Intent(this, DriveConnectActivity.class));
                return true;
            default:
                return false;
        }
    }

    private class LoadDevicesTask extends AsyncTask<Void, Void, Map<String, String>> {
        private final DriveManageActivity mActivity = DriveManageActivity.this;

        protected Map<String, String> doInBackground(Void... params) {
            return DeviceUtils.fileToMap(mActivity);
        }

        /**
         * After loading user's saved devices from file, check number of devices.
         * <p/>
         * If devices == 0:
         * Disable the action bar's navigation spinner and show the "add
         * device" dialog fragment.
         * Else:
         * Load the drives into the action bar's navigation spinner and
         * show the first drive's connection status.
         *
         * @param result The result of looking up the user's saved drives.
         */
        protected void onPostExecute(Map<String, String> result) {
            ActionBar actionBar = mActivity.getActionBar();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            Fragment frag;

            assert actionBar != null;

            if (result.isEmpty()) {
                frag = new DriveNoDevicesFragment();
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setDisplayShowTitleEnabled(true);
            } else {
                frag = new DriveManageFragment();
                actionBar.setListNavigationCallbacks(mDropDownAdapter, mActivity);
                actionBar.setDisplayShowTitleEnabled(false);
                for (Map.Entry<String, String> e : result.entrySet())
                    mDropDownAdapter.add(new Drive(e.getValue(), e.getKey()));
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                actionBar.setSelectedNavigationItem(0);
                Bundle args = new Bundle();
                args.putParcelable(Drive.KEY_BUNDLE_DRIVE, mDropDownAdapter.getItem(0));
                frag.setArguments(args);
            }

            transaction.replace(R.id.drive_manage_fragment_container, frag);
            mDriveLoadingProgress.setVisibility(View.GONE);
            transaction.commit();
            Log.e("Foo", "After transaction commit");
        }
    }

}
