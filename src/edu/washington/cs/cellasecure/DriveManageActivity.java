package edu.washington.cs.cellasecure;

import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class DriveManageActivity extends Activity implements
        OnNavigationListener {

    private ArrayAdapter<Drive> mDropDownAdapter;
    private ProgressBar mDriveLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_manage);

        mDriveLoadingProgress = (ProgressBar) findViewById(R.id.drive_loading_progress);
        
        // TODO: Get list of paired drives and populate mDropDownAdapter
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
        return false;
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

//    @Override
//    public void onDatabaseLoad(Cursor cursor) {
//        mDropDownAdapter.swapCursor(cursor);
//
//        ActionBar actionBar = getActionBar();
//        FragmentManager fragmentManager = getFragmentManager();
//        mDriveLoadingProgress.setVisibility(View.GONE);
//        FragmentTransaction trans = fragmentManager.beginTransaction();
//        Fragment frag;
//        if (mDropDownAdapter.isEmpty()) {
//            // Display no-devices fragment
//            frag = new DriveNoDevicesFragment();
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//            actionBar.setDisplayShowTitleEnabled(true);
//        } else {
//            // Display management fragment
//            frag = new DriveManageFragment();
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setListNavigationCallbacks(mDropDownAdapter, this);
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//        }
//        trans.replace(R.id.drive_manage_fragment_container, frag);
//        trans.commit();
//    }

}
