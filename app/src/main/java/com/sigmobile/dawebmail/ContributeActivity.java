package com.sigmobile.dawebmail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.sigmobile.dawebmail.network.AnalyticsAPI;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContributeActivity extends AppCompatActivity {

    @Bind(R.id.contri_email)
    ImageView email;

    @Bind(R.id.contri_github)
    TextView github;

    @OnClick(R.id.contri_email)
    public void emailClick() {
        sendEmail();
    }

    @OnClick(R.id.contri_github)
    public void githubClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(github.getText().toString()));
        startActivity(browserIntent);
        AnalyticsAPI.sendAnalyticsAction(getApplication(), AnalyticsAPI.CATEGORY_ACTION, AnalyticsAPI.ACTION_CONTRIBUTE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);
        ButterKnife.bind(this);
    }

    public void sendEmail() {
        AnalyticsAPI.sendAnalyticsAction(getApplication(), AnalyticsAPI.CATEGORY_ACTION, AnalyticsAPI.ACTION_MAIL_TO_DEV);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        sendIntent.setData(Uri.parse(getString(R.string.developer_email)));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Suggestions/Complaints.\n");
        startActivity(sendIntent);
    }
}