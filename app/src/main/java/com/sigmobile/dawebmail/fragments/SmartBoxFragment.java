package com.sigmobile.dawebmail.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rish on 6/10/15.
 */
public class SmartBoxFragment extends Fragment {


    @Bind(R.id.smart_total_avg_length)
    TextView smart_total_avg_length;

    @Bind(R.id.smart_total_emails)
    TextView smart_total_emails;

    @Bind(R.id.smart_total_last_refresh)
    TextView smart_total_last_refresh;

    @Bind(R.id.smart_total_max_sender1)
    TextView smart_total_max_sender1;

    @Bind(R.id.smart_total_max_sender2)
    TextView smart_total_max_sender2;

    @Bind(R.id.smart_total_max_sender3)
    TextView smart_total_max_sender3;

    @Bind(R.id.smart_total_no_of_opened)
    TextView smart_total_no_of_opened;

    @Bind(R.id.smart_total_no_of_opened_with_app)
    TextView smart_total_no_of_opened_with_app;

    @Bind(R.id.smart_total_per_att)
    TextView smart_total_per_att;

    public SmartBoxFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_smartbox, container, false);

        ButterKnife.bind(this, rootView);

        ArrayList<EmailMessage> emails = (ArrayList<EmailMessage>) EmailMessage.listAll(EmailMessage.class);
        HashMap<String, Integer> senderCount = new HashMap<>();

        double totalLength = 0;
        int emailsWithAttachment = 0;
        int openedWithApp = 0;
        int readEmails = 0;
        for (int i = 0; i < emails.size(); i++) {

            String subject = emails.get(i).subject;
            if (subject.contains(" "))
                totalLength += subject.split(" ").length;
            else
                totalLength++;

            if (!emails.get(i).content.equals("isempty")) {
                openedWithApp++;
            }
            if (emails.get(i).readUnread.equals("Unread Message")) {
                readEmails++;
            }

            if (senderCount.containsKey(emails.get(i).fromName)) {
                int count = (senderCount.get(emails.get(i).fromName) + 1);
                senderCount.remove(emails.get(i).fromName);
                senderCount.put(emails.get(i).fromName, count);
            } else
                senderCount.put(emails.get(i).fromName, 1);
        }

        smart_total_emails.setText("" + emails.size());
        double avgLength = Math.round((totalLength / (double) (emails.size())) * 100) / 100.0;
        smart_total_avg_length.setText("" + avgLength + " words");
        smart_total_per_att.setText("" + (100 * emailsWithAttachment) / emails.size() + " %");
        smart_total_no_of_opened_with_app.setText("" + openedWithApp);
        smart_total_no_of_opened.setText("" + ((100 * readEmails) / emails.size()) + " %");

        String date = getDateFromMillis(User.getLastRefreshed(getActivity()));
        smart_total_last_refresh.setText("Last refreshed - " + date);

        sortHashMap(senderCount);

        return rootView;
    }

    private void sortHashMap(HashMap<String, Integer> map) {
        List list = new ArrayList(map.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                return ((Comparable) ((Map.Entry) (obj1)).getValue()).compareTo(((Map.Entry) (obj2)).getValue());
            }
        });

        smart_total_max_sender1.setText("Your inbox needs more webmails");
        smart_total_max_sender2.setText("Your inbox needs more webmails");
        smart_total_max_sender3.setText("Your inbox needs more webmails");

        if (list.size() >= 1)
            smart_total_max_sender1.setText(((Map.Entry) (list.get(list.size() - 1))).getValue() + " - " + ((Map.Entry) (list.get(list.size() - 1))).getKey());
        if (list.size() >= 2)
            smart_total_max_sender2.setText(((Map.Entry) (list.get(list.size() - 2))).getValue() + " - " + ((Map.Entry) (list.get(list.size() - 2))).getKey());
        if (list.size() >= 3)
            smart_total_max_sender3.setText(((Map.Entry) (list.get(list.size() - 3))).getValue() + " - " + ((Map.Entry) (list.get(list.size() - 3))).getKey());
    }

    private String getDateFromMillis(String millis) {
        long milliSeconds = Long.parseLong(millis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return DateFormat.getDateTimeInstance().format(calendar.getTime());
    }
}