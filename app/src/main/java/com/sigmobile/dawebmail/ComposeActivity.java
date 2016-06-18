package com.sigmobile.dawebmail;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.sigmobile.dawebmail.asyncTasks.AutoCompleteListener;
import com.sigmobile.dawebmail.asyncTasks.AutoCompleteRequest;
import com.sigmobile.dawebmail.asyncTasks.SendMail;
import com.sigmobile.dawebmail.asyncTasks.SendMailListener;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class ComposeActivity extends AppCompatActivity implements SendMailListener, AutoCompleteListener {

    @Bind(R.id.compose_toolbar)
    Toolbar toolbar;
    @Bind(R.id.compose_main_rl)
    RelativeLayout relativeLayout;
    @Bind(R.id.compose_to_id)
    EditText et_to;
    @Bind(R.id.compose_subject)
    EditText et_subject;
    @Bind(R.id.compose_content)
    EditText et_content;
    @Bind(R.id.compose_imp_checkbox)
    CheckBox checkBox_imp;

    private User currentUser;

    private boolean onBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);

        currentUser = UserSettings.getCurrentUser(getApplicationContext());
        setupToolbar();

        et_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.charAt(charSequence.length() - 1) == '@')
                    et_to.setText(charSequence + getString(R.string.webmail_domain));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (onBackPressed)
            super.onBackPressed();
        else {
            showBackDialog();
        }
    }

    private void setupToolbar() {
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbarText));
        toolbar.setTitle(getString(R.string.compose));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchContacts(String searchText) {
        new AutoCompleteRequest(currentUser, getApplicationContext(), this, searchText).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                showConfirmSendMessage();
                return true;
            case R.id.menu_attach:
                Snackbar.make(toolbar, getString(R.string.snackbar_attachment_pressed), Snackbar.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendWebmail() {
        new SendMail(currentUser, ComposeActivity.this, getApplication(), et_subject.getText().toString(), et_content.getText().toString(), et_to.getText().toString(), checkBox_imp.isChecked()).execute();
    }

    @Override
    public void onPreSend() {
        Snackbar.make(et_content, getString(R.string.snackbar_sending), Snackbar.LENGTH_LONG).show();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(et_content.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(et_subject.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(et_to.getWindowToken(), 0);
    }

    @Override
    public void onPostSend(boolean success) {
        if (success) {
            Snackbar.make(relativeLayout, getString(R.string.snackbar_sending_successful), Snackbar.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        } else {
            Snackbar.make(relativeLayout, getString(R.string.snackbar_sending_unsuccesful), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAutoComplete(String[] addressBook) {
        int i = 0;
        if (addressBook != null)
            for (String s : addressBook) {
                if (i++ > 10)
                    break;
            }
    }

    private void showConfirmSendMessage() {
        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setTitle(getString(R.string.dialog_title_confirm_send))
                .setMessage(getString(R.string.dialog_msg_confirm_send))
                .setPositiveButton(getString(R.string.dialog_btn_positive_confirm_send), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendWebmail();
                        materialDialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_btn_negative_confirm_send), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialDialog.dismiss();
                    }
                })
                .setCanceledOnTouchOutside(true);
        materialDialog.show();
    }

    private void showBackDialog() {
        final MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setTitle(getString(R.string.dialog_title_save_draft))
                .setMessage(getString(R.string.dialog_msg_save_draft))
                .setPositiveButton(getString(R.string.dialog_btn_positive_save_draft), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed = true;
                        onBackPressed();
                        materialDialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_btn_negative_save_draft), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialDialog.dismiss();
                    }
                })
                .setCanceledOnTouchOutside(true);
        materialDialog.show();
    }
}