package com.sigmobile.dawebmail.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sigmobile.dawebmail.R;
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

    @Bind(R.id.settings_networkwifi_switch)
    Switch switch_wifi;

    boolean toggleMobileData = true, toggleWifi = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, rootView);

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
        return rootView;
    }
}
