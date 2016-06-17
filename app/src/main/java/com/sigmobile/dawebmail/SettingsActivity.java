package com.sigmobile.dawebmail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sigmobile.dawebmail.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rish on 6/10/15.
 */
public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.settings_networkcell_switch)
    Switch switch_mobile;
    @Bind(R.id.settings_toolbar)
    Toolbar toolbar;

    boolean toggleMobileData = true, toggleWifi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        toggleMobileData = prefs.getBoolean(Constants.TOGGLE_MOBILEDATA, true);
        toggleWifi = prefs.getBoolean(Constants.TOGGLE_WIFI, true);

        switch_mobile.setChecked(toggleMobileData);
        switch_mobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (!switch_mobile.isChecked()) {
                    editor.putBoolean(Constants.TOGGLE_MOBILEDATA, false).commit();
                } else {
                    editor.putBoolean(Constants.TOGGLE_MOBILEDATA, true).commit();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
