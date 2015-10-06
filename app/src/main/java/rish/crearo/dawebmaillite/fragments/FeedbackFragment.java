package rish.crearo.dawebmaillite.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import rish.crearo.dawebmaillite.R;

/**
 * Created by rish on 6/10/15.
 */
public class FeedbackFragment extends Fragment {

    @Bind(R.id.feedback_send)
    ImageView send;

    @Bind(R.id.feedback_et)
    EditText textbox;

    @Bind(R.id.feedback_rate)
    LinearLayout rate;

    public FeedbackFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);

        ButterKnife.bind(this, rootView);

        send.setVisibility(View.GONE);

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMarket();
            }
        });

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

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        return rootView;
    }

    public void sendEmail() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        sendIntent.setData(Uri.parse("bhardwaj.rish@gmail.com"));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bhardwaj.rish@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DAWebmail");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi Rish!\n\nHere are a few suggestions/complaints about the webmail app : \n\n" + textbox.getText().toString());
        getActivity().startActivity(sendIntent);
        textbox.setText("");
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), " unable to rate :(", Toast.LENGTH_LONG).show();
        }
    }
}