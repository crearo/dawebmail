package com.sigmobile.dawebmail.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sigmobile.dawebmail.ComposeActivity;
import com.sigmobile.dawebmail.MainActivity;
import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.adapters.MailAdapter;
import com.sigmobile.dawebmail.asyncTasks.MultiMailAction;
import com.sigmobile.dawebmail.asyncTasks.MultiMailActionListener;
import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rish on 6/10/15.
 */

public class InboxFragment extends Fragment implements RefreshInboxListener, MultiMailActionListener, MailAdapter.MultiMailActionSelectedListener {

    private static final String TAG = "InboxFragment";

    @Bind(R.id.inbox_empty_view)
    LinearLayout emptyLayout;
    @Bind(R.id.inbox_recycleView)
    RecyclerView recyclerView;
    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.searchET)
    EditText searchET;
    @Bind(R.id.inbox_fab_menu)
    FloatingActionsMenu fabMenu;
    com.getbase.floatingactionbutton.FloatingActionButton fabTrash;
    com.getbase.floatingactionbutton.FloatingActionButton fabDelete;
    com.getbase.floatingactionbutton.FloatingActionButton fabMarkRead;
    com.getbase.floatingactionbutton.FloatingActionButton fabMarkUnread;
    @Bind(R.id.inbox_send_fab)
    FloatingActionButton fabSend;

    @OnClick(R.id.inbox_send_fab)
    public void sendFab() {
        startActivity(new Intent(getActivity(), ComposeActivity.class));
    }

    private MailAdapter mailAdapter;
    private ProgressDialog progressDialog, progressDialog2;
    private ArrayList<EmailMessage> allEmails;
    private User currentUser;

    public InboxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(InboxFragment.this, rootView);

        fabTrash = ButterKnife.findById(fabMenu, R.id.fab_action_trash);
        fabDelete = ButterKnife.findById(fabMenu, R.id.fab_action_delete);
        fabMarkRead = ButterKnife.findById(fabMenu, R.id.fab_action_mark_read);
        fabMarkUnread = ButterKnife.findById(fabMenu, R.id.fab_action_mark_unread);

        currentUser = UserSettings.getCurrentUser(getActivity());
        progressDialog = new ProgressDialog(getActivity());

        if (currentUser == null) {
            // Snackbar.make(recyclerView, getString(R.string.error_something_went_wrong), Snackbar.LENGTH_LONG).show();
            emptyLayout.setVisibility(View.VISIBLE);
            return rootView;
        }

        registerInternalBroadcastReceivers();
        setupMailAdapter();
        setupSwipeRefreshLayout();
        setupSearchBar();
        setupDeleteAndComposeFABs(false);

        emptyLayout.setVisibility(View.GONE);

        return rootView;
    }

    private void setupMailAdapter() {
        allEmails = (ArrayList<EmailMessage>) EmailMessage.getAllMailsOfUser(currentUser);
        Collections.reverse(allEmails);
        mailAdapter = new MailAdapter(allEmails, getActivity(), this, Constants.INBOX);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mailAdapter);

        if (allEmails.size() == 0) {
            new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_LOAD_MORE).execute();
            allEmails = new ArrayList<>();
        }
    }

    private void registerInternalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshAdapter();
            }
        }, new IntentFilter(Constants.BROADCAST_REFRESH_ADAPTERS));
    }

    private void setupSearchBar() {
        /*
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                if (charSequence.length() >= 2) {
                    allEmails = (ArrayList<EmailMessage>) EmailMessage.listAll(EmailMessage.class);
                    Collections.reverse(allEmails);

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
                    mailAdapter = new MailAdapter(allEmails, getActivity(), InboxFragment.this, Constants.INBOX);
                    recyclerView.setAdapter(mailAdapter);
                    System.out.println("SEARCHED RESULTS COUNT = " + mailAdapter.getCount());
                } else {
                    refreshAdapter();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        */
    }

    private void setupDeleteAndComposeFABs(boolean isDeleteVisible) {
        if (!isDeleteVisible) {
            final Animation animation = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_out_bottom);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fabMenu.setVisibility(View.GONE);
                    fabSend.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_in_bottom));
                    fabSend.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (fabMenu.isExpanded()) {
                fabMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fabMenu.startAnimation(animation);
                    }
                }, 300);
            } else {
                fabMenu.startAnimation(animation);
            }
        } else {
            if (fabSend.getVisibility() == View.VISIBLE) {
                final Animation animation = AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_out_bottom);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fabSend.setVisibility(View.GONE);
                        fabMenu.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.support.design.R.anim.abc_slide_in_bottom));
                        fabMenu.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fabSend.startAnimation(animation);
            }
        }
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_REFRESH).execute();
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
        } else if (id == R.id.action_loadmore) {
            new RefreshInbox(currentUser, getActivity(), InboxFragment.this, Constants.INBOX, Constants.REFRESH_TYPE_LOAD_MORE).execute();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreRefresh() {
        progressDialog2 = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_msg_loading), true);
        progressDialog2.setCancelable(false);
        progressDialog2.show();
    }

    @Override
    public void onPostRefresh(boolean success, final ArrayList<EmailMessage> refreshedEmails, User user) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshAdapter();
                if (refreshedEmails.size() == 0)
                    Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_webmail_zero), Snackbar.LENGTH_LONG).show();
                else if (refreshedEmails.size() == 1)
                    Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_webmail_one), Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(swipeRefreshLayout, refreshedEmails.size() + getString(R.string.snackbar_webmail_many), Snackbar.LENGTH_LONG).show();
                progressDialog2.dismiss();
            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPreMultiMailAction() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_msg_attempting_action));
        progressDialog.show();
    }

    @Override
    public void onPostMultiMailAction(boolean success, String mailAction) {

        if (!success) {
            if (mailAction.equals(getString(R.string.msg_action_trash)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trash_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_delete)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_read)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_read_unsuccessful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_unread)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_unread_unsuccessful), Snackbar.LENGTH_LONG).show();
        } else {
            if (mailAction.equals(getString(R.string.msg_action_trash)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trash_successful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_delete)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_delete_successful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_read)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_read_successful), Snackbar.LENGTH_LONG).show();
            else if (mailAction.equals(getString(R.string.msg_action_unread)))
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_unread_successful), Snackbar.LENGTH_LONG).show();
        }

        progressDialog.dismiss();
        refreshAdapter();
        fabMenu.setVisibility(View.GONE);
    }

    public void refreshAdapter() {
        allEmails = (ArrayList<EmailMessage>) EmailMessage.getAllMailsOfUser(currentUser);
        Collections.reverse(allEmails);
        mailAdapter.setEmails(allEmails);
        mailAdapter.notifyDataSetChanged();
    }

    public void logout() {
        ((MainActivity) getActivity()).showLogoutDialog(currentUser);
    }

    @Override
    public void onItemClickedForDelete(final ArrayList<EmailMessage> emailsMarkedForAction) {

        if (emailsMarkedForAction.size() == 0)
            setupDeleteAndComposeFABs(false);
        else
            setupDeleteAndComposeFABs(true);

        fabMenu.setEnabled(true);

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_deleting), Snackbar.LENGTH_LONG).show();
                new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_delete)).execute();
                setupDeleteAndComposeFABs(false);
            }
        });

        fabTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_trashing), Snackbar.LENGTH_LONG).show();
                new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_trash)).execute();
                setupDeleteAndComposeFABs(false);
            }
        });

        fabMarkRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_marking_read), Snackbar.LENGTH_LONG).show();
                new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_read)).execute();
                setupDeleteAndComposeFABs(false);
            }
        });

        fabMarkUnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(swipeRefreshLayout, getString(R.string.snackbar_marking_unread), Snackbar.LENGTH_LONG).show();
                new MultiMailAction(currentUser, getActivity(), InboxFragment.this, emailsMarkedForAction, getString(R.string.msg_action_unread)).execute();
                setupDeleteAndComposeFABs(false);
            }
        });
    }
}