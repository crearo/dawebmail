package com.sigmobile.dawebmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rish on 6/10/15.
 */
public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.settings_networkcell_switch)
    Switch switch_mobile;

    @Bind(R.id.settings_sound_tv)
    TextView soundURI;

    @Bind(R.id.settings_toolbar)
    Toolbar toolbar;

    @Bind(R.id.settings_networkwifi_switch)
    Switch switch_wifi;

    boolean toggleMobileData = true, toggleWifi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.TextPrimaryAlternate));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        toggleMobileData = prefs.getBoolean(Constants.TOGGLE_MOBILEDATA, true);
        toggleWifi = prefs.getBoolean(Constants.TOGGLE_WIFI, true);

        switch_mobile.setChecked(toggleMobileData);
        switch_wifi.setChecked(toggleWifi);

        switch_wifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (!switch_wifi.isChecked()) {
                    editor.putBoolean(Constants.TOGGLE_WIFI, false).commit();
                } else {
                    editor.putBoolean(Constants.TOGGLE_WIFI, true).commit();
                }
            }
        });

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

        soundURI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");

                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ("android.resource://" + getPackageName() + "/" + R.raw.zoop));

                startActivityForResult(intent, 5);
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                UserSettings.setNotificationSound(getApplicationContext(), uri.toString());
                Snackbar.make(soundURI, "Updated Notification Sound", Snackbar.LENGTH_LONG).show();
            } else {
            }
        }
    }
}
