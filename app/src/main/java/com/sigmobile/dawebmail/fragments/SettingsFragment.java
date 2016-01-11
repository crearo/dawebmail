package com.sigmobile.dawebmail.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Printer;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rish on 6/10/15.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    @Bind(R.id.settings_networkcell_switch)
    Switch switch_mobile;

    @Bind(R.id.settings_sound_tv)
    TextView soundURI;

    @Bind(R.id.settings_networkwifi_switch)
    Switch switch_wifi;

    boolean toggleMobileData = true, toggleWifi = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.USER_PREFERENCES, getActivity().MODE_PRIVATE);
        toggleMobileData = prefs.getBoolean(Constants.TOGGLE_MOBILEDATA, true);
        toggleWifi = prefs.getBoolean(Constants.TOGGLE_WIFI, true);

        switch_mobile.setChecked(toggleMobileData);
        switch_wifi.setChecked(toggleWifi);

        switch_wifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.USER_PREFERENCES, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (!switch_wifi.isChecked()) {
                    editor.putBoolean(Constants.TOGGLE_WIFI, false).commit();
                    Printer.println("wifi put to false");
                } else {
                    editor.putBoolean(Constants.TOGGLE_WIFI, true).commit();
                    Printer.println("wifi put to true");
                }
            }
        });

        switch_mobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.USER_PREFERENCES, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (!switch_mobile.isChecked()) {
                    editor.putBoolean(Constants.TOGGLE_MOBILEDATA, false).commit();
                    Printer.println("mobile put to false");

                } else {
                    editor.putBoolean(Constants.TOGGLE_MOBILEDATA, true).commit();
                    Printer.println("mobile put to true");
                }
            }
        });

        soundURI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");

                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ("android.resource://" + getActivity().getPackageName() + "/" + R.raw.zoop));

                startActivityForResult(intent, 5);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                User.setNotificationSound(getActivity(), uri.toString());
                Snackbar.make(soundURI, "Updated Notification Sound", Snackbar.LENGTH_LONG).show();
            } else {
            }
        }
    }
}
