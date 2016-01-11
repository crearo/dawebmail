package com.sigmobile.dawebmail.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.adapters.MailAdapter;
import com.sigmobile.dawebmail.asyncTasks.DeleteMail;
import com.sigmobile.dawebmail.asyncTasks.DeleteMailListener;
import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.services.NotificationMaker;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by rish on 6/10/15.
 */

public class TrashFragment extends Fragment implements RefreshInboxListener, DeleteMailListener, MailAdapter.DeleteSelectedListener {

    @Bind(R.id.inbox_empty_view)
    LinearLayout emptyLayout;

    @Bind(R.id.inbox_listView)
    ListView listview;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.searchET)
    EditText searchET;

    @Bind(R.id.inbox_delete_fab)
    FloatingActionButton fabDelete;

    MailAdapter mailAdapter;
    ProgressDialog progressDialog, progressDialog2;

    ArrayList<EmailMessage> allEmails;

    public TrashFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        ButterKnife.bind(TrashFragment.this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trash");

        setSwipeRefreshLayout();

        allEmails = new ArrayList<>();

        mailAdapter = new MailAdapter(allEmails, getActivity(), this, Constants.TRASH);
        listview.setAdapter(mailAdapter);

        progressDialog = new ProgressDialog(getActivity());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshAdapter();
            }
        }, new IntentFilter(Constants.BROADCAST_REFRESH_ADAPTERS));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                if (charSequence.length() >= 2) {
                    for (int i = 0; i < allEmails.size(); i++) {
                        EmailMessage email = allEmails.get(i);
                        if (email.fromName.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.fromAddress.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.subject.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.dateInMillis.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())
                                || email.content.toLowerCase()
                                .contains(charSequence.toString().toLowerCase())) {
                        } else {
                            allEmails.remove(email);
                            i--;
                        }
                    }
                    mailAdapter = new MailAdapter(allEmails, getActivity(), TrashFragment.this, Constants.TRASH);
                    listview.setAdapter(mailAdapter);
                    System.out.println("SEARCHED RESULTS COUNT = " + mailAdapter.getCount());
                } else {
                    refreshAdapter();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        new RefreshInbox(getActivity(), TrashFragment.this, Constants.TRASH).execute();

        swipeRefreshLayout.setVisibility(View.GONE);

        return rootView;
    }

    private void setSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshInbox(getActivity(), TrashFragment.this, Constants.TRASH).execute();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.darker_gray,
                android.R.color.holo_blue_dark);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_inbox_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Animation slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
            Animation slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

            if (searchET.getVisibility() == View.GONE) {
                searchET.setVisibility(View.VISIBLE);
                searchET.startAnimation(slide_up);
                searchET.requestFocus();
                item.setIcon(R.drawable.ic_action_close);
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(searchET, InputMethodManager.SHOW_FORCED);
            } else {
                item.setIcon(R.drawable.ic_action_search);
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                searchET.startAnimation(slide_down);
                searchET.setVisibility(View.GONE);
            }
            return true;
        } else if (id == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreRefresh() {
        progressDialog2 = ProgressDialog.show(getActivity(), "", "Please wait while we load your content.", true);
        progressDialog2.setCancelable(false);
        progressDialog2.show();
    }

    @Override
    public void onPostRefresh(boolean success, final ArrayList<EmailMessage> refreshedEmails) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                allEmails = new ArrayList<EmailMessage>(refreshedEmails);
                refreshAdapter();

                if (refreshedEmails.size() == 0)
                    Snackbar.make(swipeRefreshLayout, "No New Webmail", Snackbar.LENGTH_LONG).show();
                else if (refreshedEmails.size() == 1)
                    Snackbar.make(swipeRefreshLayout, "1 New Webmail!", Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(swipeRefreshLayout, refreshedEmails.size() + " New Webmails!", Snackbar.LENGTH_LONG).show();

                progressDialog2.dismiss();

                if (allEmails.size() != 0) {
                    emptyLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.GONE);
                }
            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPreDelete() {
        progressDialog = ProgressDialog.show(getActivity(), "Deleting ... ", "");
        progressDialog.show();
    }

    @Override
    public void onPostDelete(boolean success) {
        if (!success) {
            Snackbar.make(swipeRefreshLayout, "Delete Unsuccessful :(", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(swipeRefreshLayout, "Deleted Successfully", Snackbar.LENGTH_LONG).show();
        }
        progressDialog.dismiss();
        refreshAdapter();
        fabDelete.setVisibility(View.GONE);
    }

    public void refreshAdapter() {
        mailAdapter = new MailAdapter(allEmails, getActivity(), this, Constants.TRASH);
        listview.setAdapter(mailAdapter);
    }

    public void logout() {
        final MaterialDialog materialDialog = new MaterialDialog(getActivity());
        materialDialog.setCanceledOnTouchOutside(true);
        materialDialog.setTitle("Log Out?");
        materialDialog.setMessage("Saying bye bye?");
        materialDialog.setNegativeButton("", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.setPositiveButton("Log out", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User.setUsername("null", getActivity());
                User.setPassword("null", getActivity());

                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.USER_PREFERENCES, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putBoolean(Constants.TOGGLE_MOBILEDATA, false);
                editor.putBoolean(Constants.TOGGLE_WIFI, false);

                EmailMessage.deleteAll(EmailMessage.class);

                SharedPreferences firstRunPrefs = getActivity().getSharedPreferences(Constants.ON_FIRST_RUN, Context.MODE_PRIVATE);
                firstRunPrefs.edit().putBoolean(Constants.RUN_EXCEPT_ON_FIRST, false).commit();

                NotificationMaker.cancelNotification(getActivity());
                materialDialog.dismiss();
                Snackbar.make(swipeRefreshLayout, "Logging Out!", Snackbar.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                        getActivity().finish();
                    }
                }, 2000);
            }
        });
        materialDialog.show();
    }

    @Override
    public void onItemClickedForDelete(final ArrayList<EmailMessage> emailsToDelete) {

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(swipeRefreshLayout, "Deleting ...", Snackbar.LENGTH_LONG).show();
                new DeleteMail(getActivity(), TrashFragment.this, emailsToDelete).execute();
            }
        });

        if (emailsToDelete.size() > 0) {
            if (fabDelete.getVisibility() != View.VISIBLE) {
                fabDelete.setVisibility(View.VISIBLE);
                fabDelete.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom));
            }
        } else {
            if (fabDelete.getVisibility() != View.GONE) {
                fabDelete.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom));
                fabDelete.setVisibility(View.GONE);
            }
        }
    }
}