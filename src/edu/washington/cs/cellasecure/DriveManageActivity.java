package edu.washington.cs.cellasecure;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import edu.washington.cs.cellasecure.fragments.DriveManageFragment;

public class DriveManageActivity extends Activity implements
        ActionBar.OnNavigationListener {

    private final DriveCollection mDriveCollection = DriveCollection
            .getCollection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_manage);

        FragmentManager fragmentManager = getFragmentManager();
        if (!mDriveCollection.getList().isEmpty()) {
            FragmentTransaction trans = fragmentManager.beginTransaction();
            Fragment frag = new DriveManageFragment();
            trans.replace(R.id.drive_manage_fragment_container, frag);
            trans.commit();
            SpinnerAdapter mDropDownAdapter = new ArrayAdapter<Drive>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    mDriveCollection.getList());
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(mDropDownAdapter, this);
        }
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

}
