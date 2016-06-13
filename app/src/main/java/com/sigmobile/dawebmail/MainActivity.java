package com.sigmobile.dawebmail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.fragments.InboxFragment;
import com.sigmobile.dawebmail.fragments.SentFragment;
import com.sigmobile.dawebmail.fragments.SmartBoxFragment;
import com.sigmobile.dawebmail.fragments.TrashFragment;
import com.sigmobile.dawebmail.services.NotificationMaker;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

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

        sSettings = (SecondaryDrawerItem) new SecondaryDrawerItem().withName("Settings").withIcon(R.drawable.settings);
        sFeedback = (SecondaryDrawerItem) new SecondaryDrawerItem().withName("Feedback").withIcon(R.drawable.feedback);

        ArrayList<IProfile> profileDrawerItems = new ArrayList<>();
        for (User user : User.getAllUsers()) {
            profileDrawerItems.add(new ProfileDrawerItem().withName(user.username).withIcon(getResources().getDrawable(R.drawable.git_user)));
        }

        profileDrawerItems.add(new ProfileDrawerItem().withName("+ Account").withIcon(getResources().getDrawable(R.drawable.plus)));

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withProfiles(profileDrawerItems)
                .withHeaderBackground(new ColorDrawable(getResources().getColor(R.color.primary_dark)))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if (profile.getName().equals("+ Account")) {
                            UserSettings.setCurrentUser(null, getApplicationContext());
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            return true;
                        }
                        UserSettings.setCurrentUser(User.getUserFromUserName(profile.getName().getText()), getApplicationContext());
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
                .withTranslucentStatusBar(false)
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
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
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
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            return false;
                        } else if (drawerItem.equals(sFeedback)) {
                            startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                            return false;
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

    public void showLogoutDialog(final User currentUser) {
        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setCanceledOnTouchOutside(true);
        materialDialog.setTitle(getString(R.string.dialog_title_logout));
        materialDialog.setMessage(getString(R.string.dialog_msg_logout));
        materialDialog.setNegativeButton("", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.setPositiveButton(getString(R.string.dialog_btn_logout), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putBoolean(Constants.TOGGLE_MOBILEDATA, false);
                editor.putBoolean(Constants.TOGGLE_WIFI, false);

                EmailMessage.deleteAllMailsOfUser(currentUser);

                SharedPreferences firstRunPrefs = getSharedPreferences(Constants.ON_FIRST_RUN, Context.MODE_PRIVATE);
                firstRunPrefs.edit().putBoolean(Constants.RUN_EXCEPT_ON_FIRST, false).commit();

                NotificationMaker.cancelNotification(getApplicationContext());
                materialDialog.dismiss();
                Snackbar.make(frameLayout, getString(R.string.snackbar_logging_out), Snackbar.LENGTH_LONG).show();

                /**
                 * Delete the current User and set the next user in line as current user
                 */
                User.deleteUser(currentUser);
                if (User.getAllUsers().size() != 0)
                    UserSettings.setCurrentUser(User.getAllUsers().get(0), getApplicationContext());
                else
                    UserSettings.setCurrentUser(null, getApplicationContext());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }, 1000);
            }
        });
        materialDialog.show();
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