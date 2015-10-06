package rish.crearo.dawebmaillite.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import rish.crearo.dawebmaillite.R;
import rish.crearo.dawebmaillite.database.EmailMessage;
import rish.crearo.dawebmaillite.ViewEmail;
import rish.crearo.dawebmaillite.utils.ColorScheme;
import rish.crearo.dawebmaillite.utils.Constants;
import rish.crearo.dawebmaillite.utils.Printer;
import rish.crearo.dawebmaillite.utils.TheFont;

/**
 * Created by rish on 6/10/15.
 */
public class MailAdapter extends BaseAdapter {

    ArrayList<EmailMessage> emails;
    Context context;

    public MailAdapter(ArrayList<EmailMessage> emails, Context context) {
        this.context = context;

        this.emails = emails;
    }

    @Override
    public int getCount() {
        return emails.size();
    }

    @Override
    public EmailMessage getItem(int position) {
        return emails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.element_email, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        EmailMessage item = getItem(position);

        holder.tv_name.setTextSize(15);

        if (item.readunread.equals("Unread Message")) {
            if (item.attlink1.equals("isempty"))
                holder.iv_icon.setImageResource(R.drawable.final_unread);
            else
                holder.iv_icon.setImageResource(R.drawable.final_unread_a);

            holder.tv_name.setTypeface(null, Typeface.BOLD);
        } else {
            if (item.attlink1.equals("isempty"))
                holder.iv_icon.setImageResource(R.drawable.final_read);
            else
                holder.iv_icon.setImageResource(R.drawable.final_read_a);
            holder.tv_name.setTypeface(null, Typeface.NORMAL);
        }
        holder.tv_name.setText(item.fromname);
        holder.tv_date.setText(item.date);
        holder.tv_subject.setText(item.subject);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                Printer.println("Long click");
                Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);

                return true;
            }
        });

        holder.iv_icon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Printer.println("Clicked ! " + position);

//                if (emails_ischecked.get(position).getIschecked()) {
//                    emails_ischecked.get(position).setIschecked(false);
//
//                    emails_tobedeleted_pub.remove(emails.get(position));
//                    if (--totalSelected_emails == 0) {
//                        floatingDelete.bringToFront();
//                        AnimationSet set = new AnimationSet(true);
//                        Animation translate = new TranslateAnimation(0, 100, 0, 0);
//                        translate.setDuration(300);
//                        set.addAnimation(translate);
//                        floatingDelete.startAnimation(set);
//                        floatingDelete.setVisibility(View.INVISIBLE);
//                    }
//                } else {
//                    emails_ischecked.get(position).setIschecked(true);
//
//                    if (totalSelected_emails++ == 0) {
//                        floatingDelete.bringToFront();
//                        floatingDelete.setVisibility(View.VISIBLE);
//                        AnimationSet set = new AnimationSet(true);
//                        Animation translate = new TranslateAnimation(100, 0, 0, 0);
//                        translate.setDuration(300);
//                        set.addAnimation(translate);
//                        floatingDelete.startAnimation(set);
//                    }
//                    emails_tobedeleted_pub.add(emails.get(position));
//                }
//                Printer.println(emails_ischecked.get(position).ischecked + ", totalselected =  " + totalSelected_emails);

                notifyDataSetChanged();

            }
        });

        holder.tv_relaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewEmail.class);
                intent.putExtra(Constants.CURRENT_EMAIL_ID, emails.get(position).getId());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name, tv_subject, tv_date;
        LinearLayout tv_relaLayout;

        public ViewHolder(View view) {
            tv_relaLayout = (LinearLayout) view.findViewById(R.id.tv_relativelayout);
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_date = (TextView) view.findViewById(R.id.tv_date);
            tv_subject = (TextView) view.findViewById(R.id.tv_subject);

            Typeface font = TheFont.getFont(context);

            tv_date.setTypeface(font);
            tv_name.setTypeface(font);
            tv_subject.setTypeface(font);

            view.setTag(this);
        }
    }
}
