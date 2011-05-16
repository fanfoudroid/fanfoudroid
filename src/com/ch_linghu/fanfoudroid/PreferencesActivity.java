/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.fanfoudroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.http.HttpClient;
import com.ch_linghu.fanfoudroid.ui.module.MyTextView;

public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: is this a hack?
        setResult(RESULT_OK);

        addPreferencesFromResource(R.xml.preferences);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        
        if ( key.equalsIgnoreCase(Preferences.NETWORK_TYPE) ) {
            HttpClient httpClient = TwitterApplication.mApi.getHttpClient();
            String type =  sharedPreferences.getString(Preferences.NETWORK_TYPE, "");
            
            if (type.equalsIgnoreCase(getString(R.string.pref_network_type_cmwap))) {
                Log.d("LDS", "Set proxy for cmwap mode.");
                httpClient.setProxy("10.0.0.172", 80, "http");
            } else {
                Log.d("LDS", "No proxy.");
                httpClient.removeProxy();
            }
        } else if ( key.equalsIgnoreCase(Preferences.UI_FONT_SIZE)) {
            MyTextView.setFontSizeChanged(true);
        }
        
    }
    

}
