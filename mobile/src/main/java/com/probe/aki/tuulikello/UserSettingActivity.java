package com.probe.aki.tuulikello;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class UserSettingActivity extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}

