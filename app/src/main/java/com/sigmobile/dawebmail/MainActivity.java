package com.sigmobile.dawebmail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.sigmobile.dawebmail.database.CurrentUser;
import com.sigmobile.dawebmail.fragments.FolderFragment;
import com.sigmobile.dawebmail.fragments.InboxFragment;
import com.sigmobile.dawebmail.fragments.SmartBoxFragment;
import com.sigmobile.dawebmail.network.AnalyticsAPI;
import com.sigmobile.dawebmail.services.NotificationMaker;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Settings;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    @Bind(R.id.main_tool_bar)
    Toolbar toolbar;
    @Bind(R.id.main_frame_layout)
    FrameLayout frameLayout;

    private Drawer drawer;
    private AccountHeader accountHeader;
    private PrimaryDrawerItem pInbox, pSmartBox, pSentBox, pTrashBox;
    private SecondaryDrawerItem sSettings, sFeedback, sContribute;
    private ArrayList<IProfile> allAccountHeaders;
    private IDrawerItem selectedDrawerItem;
    private Settings settings;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 108;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        settings = new Settings(getApplicationContext());

        setupToolbar();
        setupDrawer();

        selectedDrawerItem = pInbox;
        setSelectedAccountHeader(true);

        showUpdatesDialog();
        checkPermission();

        AnalyticsAPI.sendValueLessAction(AnalyticsAPI.ACTION_APP_OPEN, getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedDrawerItem != null) {
            setToolbarTitle(selectedDrawerItem);
        }
        setSelectedAccountHeader(false);
    }

    private void setupToolbar() {
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbarText));
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        pInbox = new PrimaryDrawerItem().withName(getString(R.string.drawer_inbox)).withIcon(R.drawable.inbox);
        pSmartBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_smartbox)).withIcon(R.drawable.fire_element);
        pSentBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_sent)).withIcon(R.drawable.sent);
        pTrashBox = new PrimaryDrawerItem().withName(getString(R.string.drawer_trash)).withIcon(R.drawable.trash);

        sSettings = (SecondaryDrawerItem) new SecondaryDrawerItem().withName(getString(R.string.drawer_settings)).withIcon(R.drawable.settings);
        sFeedback = (SecondaryDrawerItem) new SecondaryDrawerItem().withName(getString(R.string.drawer_feedback)).withIcon(R.drawable.feedback);
        sContribute = (SecondaryDrawerItem) new SecondaryDrawerItem().withName(getString(R.string.drawer_contribute)).withIcon(R.drawable.github_drawer);

        sSettings.withSelectable(false);
        sFeedback.withSelectable(false);
        sContribute.withSelectable(false);

        setupAllAccountHeaders();
        final String createAccountString = getString(R.string.drawer_new_account);

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withProfiles(allAccountHeaders)
                .withHeaderBackground(new ColorDrawable(getResources().getColor(R.color.primary_dark)))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if (profile.getName().getText().equals(createAccountString)) {
                            CurrentUser.setCurrentUser(null, getApplicationContext());
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            drawer.closeDrawer();
                            AnalyticsAPI.sendValueLessAction(AnalyticsAPI.ACTION_NEW_ACCOUNT, getApplicationContext());
                            return true;
                        } else {
                            CurrentUser.setCurrentUser(User.getUserFromUserName(profile.getName().getText()), getApplicationContext());
                            drawer.closeDrawer();
                            if (selectedDrawerItem == null)
                                selectedDrawerItem = pInbox;
                            else if (!selectedDrawerItem.isSelectable())
                                selectedDrawerItem = pInbox;
                            setDrawerSelection(selectedDrawerItem);
                            setToolbarTitle(selectedDrawerItem);
                            return true;
                        }
                    }
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        pInbox,
                        pSentBox,
                        pTrashBox,
                        new DividerDrawerItem(),
                        pSmartBox,
                        new DividerDrawerItem(),
                        sSettings,
                        sFeedback,
                        sContribute
                ).withDelayOnDrawerClose(200)
                .withCloseOnClick(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Fragment fragment = null;
                        Bundle bundle = null;
                        String fragmentTag = "";
                        drawer.closeDrawer();
                        if (drawerItem == null)
                            drawerItem = pInbox;

                        setToolbarTitle(drawerItem);
                        if (drawerItem.equals(pInbox)) {
                            selectedDrawerItem = (PrimaryDrawerItem) drawerItem;
                            fragment = new InboxFragment();
                            Snackbar.make(frameLayout, getString(R.string.drawer_inbox), Snackbar.LENGTH_SHORT).show();
                            fragmentTag = Constants.FRAGMENT_TAG_INBOX;
                        } else if (drawerItem.equals(pSmartBox)) {
                            selectedDrawerItem = (PrimaryDrawerItem) drawerItem;
                            fragment = new SmartBoxFragment();
                            Snackbar.make(frameLayout, getString(R.string.drawer_smartbox), Snackbar.LENGTH_SHORT).show();
                            fragmentTag = Constants.FRAGMENT_TAG_SMARTBOX;
                        } else if (drawerItem.equals(pSentBox)) {
                            selectedDrawerItem = (PrimaryDrawerItem) drawerItem;
                            fragment = new FolderFragment();
                            bundle = new Bundle();
                            bundle.putString(Constants.FOLDER, Constants.SENT);
                            fragment.setArguments(bundle);
                            fragmentTag = Constants.FRAGMENT_TAG_FOLDER;
                            Snackbar.make(frameLayout, getString(R.string.drawer_sent), Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(pTrashBox)) {
                            selectedDrawerItem = (PrimaryDrawerItem) drawerItem;
                            fragment = new FolderFragment();
                            bundle = new Bundle();
                            bundle.putString(Constants.FOLDER, Constants.TRASH);
                            fragment.setArguments(bundle);
                            fragmentTag = Constants.FRAGMENT_TAG_FOLDER;
                            Snackbar.make(frameLayout, getString(R.string.drawer_trash), Snackbar.LENGTH_SHORT).show();
                        } else if (drawerItem.equals(sSettings)) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        } else if (drawerItem.equals(sFeedback)) {
                            Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        } else if (drawerItem.equals(sContribute)) {
                            Intent intent = new Intent(MainActivity.this, ContributeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        }
                        if (fragment != null) {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.main_frame_layout, fragment, fragmentTag).commit();
                            fragmentManager.popBackStack();
                        }
                        return false;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    private void setSelectedAccountHeader(boolean shouldClick) {
        String currentUsername = CurrentUser.getCurrentUser(this).getUsername();
        for (int i = 0; i < allAccountHeaders.size(); i++) {
            if (allAccountHeaders.get(i).getName().getText().equals(currentUsername))
                accountHeader.setActiveProfile(allAccountHeaders.get(i), shouldClick);
        }
    }

    private void setupAllAccountHeaders() {
        allAccountHeaders = new ArrayList<>();
        List<User> users = User.getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem().withName(users.get(i).getUsername());
            /**
             * A fun image for each profile drawer
             */
            if (i % 3 == 0)
                profileDrawerItem.withIcon(getResources().getDrawable(R.drawable.user_white));
            if (i % 3 == 1)
                profileDrawerItem.withIcon(getResources().getDrawable(R.drawable.user_blue));
            if (i % 3 == 2)
                profileDrawerItem.withIcon(getResources().getDrawable(R.drawable.user_grey));
            allAccountHeaders.add(profileDrawerItem);
        }

        final String createAccount = getString(R.string.drawer_new_account);
        allAccountHeaders.add(new ProfileDrawerItem().withName(createAccount).withIcon(getResources().getDrawable(R.drawable.plus)));
    }

    public void showLogoutDialog(final User currentUser) {
        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setCanceledOnTouchOutside(true);
        materialDialog.setTitle(getString(R.string.dialog_title_logout));
        if (User.getUsersCount() >= 2)
            materialDialog.setMessage(getString(R.string.dialog_msg_logout_multi));
        else
            materialDialog.setMessage(getString(R.string.dialog_msg_logout_single));
        materialDialog.setNegativeButton("", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.setPositiveButton(getString(R.string.dialog_btn_logout), new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                settings.save(Settings.KEY_MOBILE_DATA, false);

                EmailMessage.deleteAllMailsOfUser(currentUser);

                NotificationMaker.cancelNotification(getApplicationContext());
                materialDialog.dismiss();
                Snackbar.make(frameLayout, getString(R.string.snackbar_logging_out), Snackbar.LENGTH_LONG).show();

                /**
                 * Delete the current User and set the next user in line as current user
                 */
                User.deleteUser(currentUser);
                if (User.getAllUsers().size() != 0)
                    CurrentUser.setCurrentUser(User.getAllUsers().get(0), getApplicationContext());
                else
                    CurrentUser.setCurrentUser(null, getApplicationContext());

                AnalyticsAPI.sendValueLessAction(AnalyticsAPI.ACTION_LOGOUT, getApplicationContext());

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

    private void setToolbarTitle(IDrawerItem drawerItem) {
        String currentUserName = User.getUserThreeLetterName(CurrentUser.getCurrentUser(this));
        String toolbarTitle = "";
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pInbox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_inbox) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSentBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_sent) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSmartBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_smartbox) + " : " + currentUserName;
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pTrashBox.getName().getText()))
            toolbarTitle = getString(R.string.drawer_trash) + " : " + currentUserName;
        getSupportActionBar().setTitle(toolbarTitle);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void setDrawerSelection(IDrawerItem drawerItem) {
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pInbox.getName().getText()))
            drawer.setSelection(pInbox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSentBox.getName().getText()))
            drawer.setSelection(pSentBox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pSmartBox.getName().getText()))
            drawer.setSelection(pSmartBox);
        if (((PrimaryDrawerItem) drawerItem).getName().getText().equals(pTrashBox.getName().getText()))
            drawer.setSelection(pTrashBox);
    }

    private void showUpdatesDialog() {
        if (!settings.getBoolean(Settings.KEY_UPDATE_SHOWN)) {
            final MaterialDialog materialDialog = new MaterialDialog(MainActivity.this);
            materialDialog
                    .setTitle(getString(R.string.dialog_title_updates_1))
                    .setMessage(getString(R.string.dialog_msg_updates_1))
                    .setPositiveButton(getString(R.string.dialog_btn_positive_updates_1), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            settings.save(Settings.KEY_UPDATE_SHOWN, true);
                            drawer.openDrawer();
                            materialDialog.dismiss();
                        }
                    })
                    .setCanceledOnTouchOutside(false);
            materialDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(frameLayout, "Great! You can now save images to gallery", Snackbar.LENGTH_LONG).show();
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    final MaterialDialog materialDialog = new MaterialDialog(MainActivity.this);
                    materialDialog.setTitle("STORAGE PERMISSION")
                            .setMessage("If you disable this, you won't be able to download attachments! Are you sure?")
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    checkPermission();
                                    materialDialog.dismiss();
                                }
                            })
                            .setNegativeButton("Disable, I'm Sure", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    materialDialog.dismiss();
                                    Snackbar.make(frameLayout, "You shall not be able to save attachments", Snackbar.LENGTH_LONG).show();
                                }
                            });
                    materialDialog.show();
                }
                return;
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Not Granted. Unable to download Image.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.d(TAG, "Showing shouldShowRequestPermissionRationale");
                final MaterialDialog materialDialog = new MaterialDialog(this);
                materialDialog
                        .setTitle("STORAGE PERMISSION")
                        .setMessage("We need this to save attachments.")
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton("Got It!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                materialDialog.dismiss();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                            }
                        });
                materialDialog.show();
            } else {
                Log.d(TAG, "Requesting Permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        } else {
            Log.d(TAG, "Already Granted");
        }
    }
}