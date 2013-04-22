package edu.washington.cs.cellasecure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveCollection {

    public static final Drive NEW_DRIVE = new Drive("NEW DEVICE");
    
    private static DriveCollection sSingleton = null;
    private final List<Drive> mDrives = new ArrayList<Drive>();
    
    private DriveCollection() {
        mDrives.add(NEW_DRIVE);
    }
    
    public static synchronized DriveCollection getCollection() {
        if (sSingleton == null)
            sSingleton = new DriveCollection();
        return sSingleton;
    }
    
    public synchronized boolean addDrive(Drive d) {
        return mDrives.add(d);
    }
    
    public synchronized boolean removeDrive(Drive d) {
        return mDrives.remove(d);
    }
    
    public synchronized List<Drive> getList() {
        return Collections.unmodifiableList(mDrives);
    }
}
