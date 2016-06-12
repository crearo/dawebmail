package com.sigmobile.dawebmail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rish on 6/10/15.
 */
public class FeedbackActivity extends AppCompatActivity {

    @Bind(R.id.feedback_send)
    ImageView send;

    @Bind(R.id.feedback_et)
    EditText textbox;

    @Bind(R.id.feedback_rate)
    LinearLayout rate;

    @Bind(R.id.feedback_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_feedback);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.TextPrimaryAlternate));

        send.setVisibility(View.GONE);

        setupTextWatcher();
    }

    private void setupTextWatcher() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    send.setVisibility(View.VISIBLE);
                else
                    send.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textbox.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.feedback_send)
    public void onSend() {
        sendEmail();
    }

    @OnClick(R.id.feedback_rate)
    public void onRate() {
        launchMarket();
    }

    @OnClick(R.id.feedback_github)
    public void onGitHub() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_repo)));
        startActivity(browserIntent);
    }

    public void sendEmail() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        sendIntent.setData(Uri.parse(getString(R.string.developer_email)));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Suggestions/Complaints.\n" + textbox.getText().toString());
        startActivity(sendIntent);
        textbox.setText("");
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), " unable to rate :(", Toast.LENGTH_LONG).show();
        }
    }
}