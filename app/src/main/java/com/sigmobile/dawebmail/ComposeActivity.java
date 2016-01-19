package com.sigmobile.dawebmail;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import butterknife.Bind;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        ButterKnife.bind(this);

        toolbar.setTitleTextColor(getResources().getColor(R.color.EmailBackground));
        toolbar.setTitle("Compose");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 3) {
//                    fetchContacts(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fetchContacts(String searchText) {
        new AutoCompleteRequest(getApplicationContext(), this, searchText).execute();
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
                sendWebmail();
                return true;
            case R.id.menu_attach:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendWebmail() {
        new SendMail(ComposeActivity.this, getApplication(), et_subject.getText().toString(), et_content.getText().toString(), et_to.getText().toString(), checkBox_imp.isChecked()).execute();
    }

    @Override
    public void onPreSend() {
        Snackbar.make(et_content, "Sending...", Snackbar.LENGTH_LONG).show();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(et_content.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(et_subject.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(et_to.getWindowToken(), 0);
    }

    @Override
    public void onPostSend(boolean success) {
        if (success) {
            Snackbar.make(relativeLayout, "Sent Successfully", Snackbar.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        } else {
            Snackbar.make(relativeLayout, "Unable to send. Make Sure you are connected to the net.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAutoComplete(String[] addressBook) {
        int i = 0;
        if (addressBook != null)
            for (String s : addressBook) {
                if (i++ > 10)
                    break;
                Log.d("Ad", s);
            }
    }
}