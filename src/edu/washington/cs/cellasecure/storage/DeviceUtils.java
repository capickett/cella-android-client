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

package edu.washington.cs.cellasecure.storage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import edu.washington.cs.cellasecure.Drive;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DeviceUtils {
    private static final String mFilename = "address_to_name_map.dat";

    private static final ExecutorService sPool = Executors.newSingleThreadExecutor();

    // mapToFile
    public static void mapToFile(Context context, Map<String, String> addrNameMap) throws IOException {
        FileOutputStream outputStream;
        if (addrNameMap != null) {
            outputStream = context.openFileOutput(mFilename, Context.MODE_PRIVATE);
            for (Map.Entry<String, String> e : addrNameMap.entrySet()) {
                String line = e.getKey() + "\t" + e.getValue() + "\n";
                outputStream.write(line.getBytes());
            }
            outputStream.close();
        }
    }

    // fileToMap
    public static Map<String, String> fileToMap(Context context) {
        Map<String, String> addrNameMap = new HashMap<String, String>();
        try {
            Scanner scan = new Scanner(context.openFileInput(mFilename));
            String line;
            String[] splitLine;
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                splitLine = line.split("\t");
                if (splitLine.length != 2)
                    throw new IllegalStateException("Bad file data");
                addrNameMap.put(splitLine[0], splitLine[1]);
            }
        } catch (FileNotFoundException fnfe) {
            // Do nothing
        }
        Log.e("Foo", "fileToMap: " + addrNameMap.toString());
        return addrNameMap;
    }

    public static void loadDrives(Activity activity, OnPairedDrivesLoadListener listener) {
        sPool.submit(new PairedDrivesLoadTask(activity, listener));
    }

    private static class PairedDrivesLoadTask implements Runnable {

        private Activity mActivity;
        private OnPairedDrivesLoadListener mListener;

        public PairedDrivesLoadTask(Activity activity, OnPairedDrivesLoadListener listener) {
            if (activity == null)
                throw new IllegalArgumentException("activity must be non-null!");

            mActivity = activity;
            mListener = listener;
        }

        @Override
        public void run() {
            Map<String, String> pairedDrives = DeviceUtils.fileToMap(mActivity);
            List<Drive> result = new ArrayList<Drive>();
            for (Map.Entry<String, String> e : pairedDrives.entrySet())
                result.add(new Drive(e.getValue(), e.getKey()));

            if (mListener != null)
                mListener.onPairedDrivesLoad(result);
        }
    }

    public interface OnPairedDrivesLoadListener {
        public void onPairedDrivesLoad(List<Drive> pairedDrives);
    }
}
