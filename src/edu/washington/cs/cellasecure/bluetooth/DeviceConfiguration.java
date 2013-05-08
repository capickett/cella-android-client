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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to manage configurations for each CellaSecure device
 * that this mobile application is connected to
 * 
 * @author CellaSecure
 */
public class DeviceConfiguration {
	private Map<String, String> mConfigurations;	// Map from configuration names to values
	
	/*
	 * Constructs new empty configuration
	 */
	public DeviceConfiguration() {
		mConfigurations = new HashMap<String, String>();
	}
	
	/*
	 * Constructs a new configuration from the given string
	 * The string must follow the following format:
	 * 		OUTLINE FORMAT HERE
	 * @param config	Configuration string
	 */
	public DeviceConfiguration(String config) {
		// TODO:
		mConfigurations = new HashMap<String, String>();
	}
	
	/**
	 * Adds the following fieldName value pair to the configuration,
	 * overwriting previous values if fieldName already exists in the
	 * configuration
	 * 
	 * @param fieldName	the name of the configuration field
	 * @param value		the value that fieldName will point to
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
	 * @param fieldName	the configuration to remove
	 * @return			the value that fieldName pointed to,
	 * 					else null
	 */
	public String removeOption(String fieldName) {
		return mConfigurations.remove(fieldName);
	}
	
	@Override
	public String toString() {
		return mConfigurations.toString();
	}
	
}
