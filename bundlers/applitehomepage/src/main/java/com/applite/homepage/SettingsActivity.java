package com.applite.homepage;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;

import com.applite.sharedpreferences.AppliteSPUtils;

/**
 * Created by android153 on 9/23/15.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private final String TAG = "homepage_PagerFragment";

    private ActionBar actionBar;

    private SwitchPreference app_update;
    private SwitchPreference no_image;


    private static final String KEY_APPUPDATE = "appupdate";
    private static final String kEY_NOIMAGE = "noimage";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        initPreference();
//        initActionBar();
    }

    private void initPreference() {
        app_update = (SwitchPreference) findPreference(KEY_APPUPDATE);
        if (null != app_update) {
            app_update.setOnPreferenceChangeListener(this);
        }


        no_image = (SwitchPreference) findPreference(kEY_NOIMAGE);
        if (null != no_image) {
            no_image.setOnPreferenceChangeListener(this);
        }

    }

//
//    private void initActionBar() {
//        if (null == actionBar) {
//            actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
//        }
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(this.getResources().getString(R.string.setting));
//        actionBar.setDisplayShowCustomEnabled(false);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.show();
//    }

    /**
     * Called when a Preference has been changed by the user. This is
     * called before the state of the Preference is about to be updated and
     * before the state is persisted.
     *
     * @param preference The changed Preference.
     * @param newValue   The new value of the Preference.
     * @return True to update the state of the Preference with the new value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean check = (Boolean) newValue;
        if (preference.getKey().equals(KEY_APPUPDATE)) {
            if (check) {
                AppliteSPUtils.put(this, AppliteSPUtils.WIFI_UPDATE_SWITCH, true);
            } else {
                AppliteSPUtils.put(this, AppliteSPUtils.WIFI_UPDATE_SWITCH, false);
            }
        } else if (preference.getKey().equals(kEY_NOIMAGE)) {
            if (check) {
                AppliteSPUtils.put(this, AppliteSPUtils.NO_PICTURE, true);
            } else {
                AppliteSPUtils.put(this, AppliteSPUtils.NO_PICTURE, false);
            }
        }


        return true;
    }
}
