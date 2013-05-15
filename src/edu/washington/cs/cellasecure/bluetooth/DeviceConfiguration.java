/*
 * Copyright 2013 CellaSecure
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.washington.cs.cellasecure.bluetooth;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to manage configurations for each CellaSecure device that this mobile
 * application is connected to
 * 
 * @author CellaSecure
 */
public class DeviceConfiguration {
    private static final int STRUCT_SIZE = 1;
    private Map<String, String> mConfigurations;

    /**
     * Constructs new empty configuration
     */
    public DeviceConfiguration() {
        mConfigurations = new LinkedHashMap<String, String>();
    }

    /**
     * Constructs a new configuration from the given string The string must
     * follow the following format:
     * 
     *   struct config_st {
     *     uint8_t encryption_level; // (0 - unencrypted, 1 - single factor, 2 - dual factor)
     *   };
     * 
     * @param config Configuration string
     */
    public DeviceConfiguration(String config) {
        mConfigurations = new LinkedHashMap<String, String>();
        
        ByteBuffer buf = ByteBuffer.wrap(config.getBytes());
        
        // BEGIN CONFIGURATION OPTIONS
        short encryption_level = buf.getShort();
        mConfigurations.put("encryption_level", "" + encryption_level);
        
        // additional options here
        
        // END CONFIGURATION OPTIONS
    }

    /**
     * Returns a byte stream containing the values for this configuration.
     * This is to be parsed into a C structure. 
     * @see DeviceConfiguration(String config)
     * @return
     */
    public byte[] configBytes() {
        ByteBuffer buf = ByteBuffer.allocate(STRUCT_SIZE);
        for (String configOption : mConfigurations.keySet()) {
            buf.put(mConfigurations.get(configOption).getBytes());
        }
        return buf.array();
    }

    /**
     * Adds the following fieldName value pair to the configuration, overwriting
     * previous values if fieldName already exists in the configuration
     * 
     * @param fieldName     the name of the configuration field
     * @param value         the value that fieldName will point to
     */
    public void setOption(String fieldName, String value) {
        mConfigurations.put(fieldName, value);
    }

    /**
     * Returns a list of all field names in the current configuration
     * 
     * @return a list of all current configuration field names
     */
    public List<String> listOptions() {
        return new ArrayList<String>(mConfigurations.keySet());
    }

    /**
     * Removes the given field name from the configuration
     * 
     * @param fieldName     the configuration to remove
     * @return the value that fieldName pointed to, else null
     */
    public String removeOption(String fieldName) {
        return mConfigurations.remove(fieldName);
    }

    @Override
    public String toString() {
        return mConfigurations.toString();
    }

}
