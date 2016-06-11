package com.sigmobile.dawebmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.fragments.FeedbackFragment;
import com.sigmobile.dawebmail.fragments.InboxFragment;
import com.sigmobile.dawebmail.fragments.SentFragment;
import com.sigmobile.dawebmail.fragments.SettingsFragment;
import com.sigmobile.dawebmail.fragments.SmartBoxFragment;
import com.sigmobile.dawebmail.fragments.TrashFragment;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_tool_bar)
    Toolbar toolbar;

    @Bind(R.id.main_frame_layout)
    FrameLayout frameLayout;

    Drawer drawer;

    PrimaryDrawerItem pInbox, pSmartBox, pSentBox, pTrashBox;
    SecondaryDrawerItem sSettings, sFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TextPrimaryAlternate));

        setupDrawer();

        drawer.setSelection(pInbox);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupDrawer();
    }

    private void setupDrawer() {

        pInbox = new PrimaryDrawerItem().withName("Inbox").withIcon(R.drawable.inbox);
        pSmartBox = new PrimaryDrawerItem().withName("SmartBox").withIcon(R.drawable.fire_element);
        pSentBox = new PrimaryDrawerItem().withName("SentBox").withIcon(R.drawable.sent);
        pTrashBox = new PrimaryDrawerItem().withName("TrashBox").withIcon(R.drawable.trash);

        sSettings = new SecondaryDrawerItem().withName("Settings").withIcon(R.drawable.settings);
        sFeedback = new SecondaryDrawerItem().withName("Feedback").withIcon(R.drawable.feedback);

        sSettings.setCheckable(false);
        sFeedback.setCheckable(false);

        ArrayList<IProfile> profileDrawerItems = new ArrayList<>();
        for (User user : User.getAllUsers()) {
            profileDrawerItems.add(new ProfileDrawerItem().withName(user.username));
        }

        profileDrawerItems.add(new ProfileDrawerItem().withName("+ Account"));

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.snackbar_background)
                .withProfiles(profileDrawerItems)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if (profile.getName().equals("+ Account")) {
                            UserSettings.setCurrentUser(null, getApplicationContext());
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            return true;
                        }
                        UserSettings.setCurrentUser(User.getUserFromUserName(profile.getName()), getApplicationContext());
                        drawer.closeDrawer();
                        drawer.setSelection(pInbox);
                        return true;
                    }
                })
                .build();

        for (int i = 0; i < profileDrawerItems.size(); i++) {
            if (profileDrawerItems.get(i).getName().equals(UserSettings.getCurrentUser(this).username))
                accountHeader.setActiveProfile(profileDrawerItems.get(i));
        }

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        pInbox,
                        pSmartBox,
                        pSentBox,
                        pTrashBox,
                        new DividerDrawerItem(),
                        sSettings,
                        sFeedback
                ).withDelayOnDrawerClose(200)
                .withCloseOnClick(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        Fragment fragment = null;

                        drawer.closeDrawer();
                        if (drawerItem == null) {
                            drawerItem = pInbox;
                        }

                        if (drawerItem.equals(pInbox)) {
                            fragment = new InboxFragment();
                            Snackbar.make(frameLayout, "Inbox", Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(pSmartBox)) {
                            fragment = new SmartBoxFragment();
                            Snackbar.make(frameLayout, "SmartBox", Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(pSentBox)) {
                            fragment = new SentFragment();
                            Snackbar.make(frameLayout, "SentBox", Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(pTrashBox)) {
                            fragment = new TrashFragment();
                            Snackbar.make(frameLayout, "TrashBox", Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(sSettings)) {
                            fragment = new SettingsFragment();
                            Snackbar.make(frameLayout, "Settings", Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(sFeedback)) {
                            fragment = new FeedbackFragment();
                            Snackbar.make(frameLayout, "Feedback", Snackbar.LENGTH_SHORT).show();
                        }

                        if (fragment != null) {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.main_frame_layout, fragment).commit();
                            fragmentManager.popBackStack();
                        } else {
                            Log.e("MainActivity", "Error in creating fragment");
                        }
                        return false;
                    }
                })
                .build();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}