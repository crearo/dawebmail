package com.sigmobile.dawebmail.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.ViewEmail;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.DateUtils;
import com.sigmobile.dawebmail.utils.Printer;
import com.sigmobile.dawebmail.utils.TheFont;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public class MailAdapter extends BaseAdapter {

    private ArrayList<EmailMessage> emails;
    private Context context;
    private ArrayList<EmailMessage> emailsToDelete;
    private DeleteSelectedListener deleteSelectedListener;
    private Boolean clickedForDelete[];
    private String EMAIL_TYPE;

    public MailAdapter(ArrayList<EmailMessage> emails, Context context, DeleteSelectedListener deleteSelectedListener, String EMAIL_TYPE) {
        this.context = context;
        emailsToDelete = new ArrayList<>();
        this.emails = emails;
        this.deleteSelectedListener = deleteSelectedListener;
        this.clickedForDelete = new Boolean[emails.size()];
        this.EMAIL_TYPE = EMAIL_TYPE;
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
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final EmailMessage item = getItem(position);

        if (clickedForDelete[position]) {
            System.out.println("" + item.fromName + " " + item.subject + " is marked for delete");
        }

        if (item.readUnread.equals(Constants.WEBMAIL_UNREAD)) {
            if (!clickedForDelete[position]) {
                if (item.totalAttachments >= 1)
                    holder.msgIcon.setImageResource(R.drawable.msg_unread_att);
                else if (item.important)
                    holder.msgIcon.setImageResource(R.drawable.msg_unread_imp);
                else
                    holder.msgIcon.setImageResource(R.drawable.msg_unread);
            } else {
                System.out.println("set holder icon to delete wala");
                holder.msgIcon.setAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_grow_fade_in_from_bottom));
                holder.msgIcon.setImageResource(R.drawable.ic_action_delete_red);
            }

            holder.msgFrom.setTypeface(null, Typeface.BOLD);
        } else {
            if (!clickedForDelete[position]) {
                if (item.totalAttachments >= 1)
                    holder.msgIcon.setImageResource(R.drawable.msg_read_att);
                else if (item.important)
                    holder.msgIcon.setImageResource(R.drawable.msg_read_imp);
                else
                    holder.msgIcon.setImageResource(R.drawable.msg_read);
            } else {
                holder.msgIcon.setImageResource(R.drawable.ic_action_delete_red);
                Printer.println("set holder icon to delete wala");
                holder.msgIcon.setAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_grow_fade_in_from_bottom));
            }
            holder.msgFrom.setTypeface(null, Typeface.NORMAL);
        }

        holder.msgFrom.setText(item.fromName);
        holder.msgDateRecv.setText(DateUtils.getDate(Long.parseLong(item.dateInMillis)));
        holder.msgSubject.setText(item.subject);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                if (clickedForDelete[position]) {
                    clickedForDelete[position] = false;
                    notifyDataSetChanged();
                    emailsToDelete.remove(item);
                    System.out.println("clicked for delete is true, returning to normal");
                } else {
                    clickedForDelete[position] = true;
                    emailsToDelete.add(item);
                    notifyDataSetChanged();
                    Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(20);
                    System.out.println("clicked for delete is false, added to emailsToDelete");
                }
                deleteSelectedListener.onItemClickedForDelete(emailsToDelete);
                return true;
            }
        });

        holder.msgIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (clickedForDelete[position]) {
                    clickedForDelete[position] = false;
                    notifyDataSetChanged();
                    emailsToDelete.remove(item);
                    Printer.println("clicked for delete is true, returning to normal");
                } else {
                    clickedForDelete[position] = true;
                    emailsToDelete.add(item);
                    notifyDataSetChanged();
                    Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(20);
                    Printer.println("clicked for delete is false, added to emailsToDelete");
                }
                deleteSelectedListener.onItemClickedForDelete(emailsToDelete);
            }
        });

        holder.msgContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewEmail.class);
                Bundle bundle = new Bundle();
                /*
                *Sending email type, and the email itself.
                * All operations that change the EmailMessage object must happen there,
                * and it must return from there, the unsaved object.
                * It is then our choice as to whether we want to save it or not.
                * I am not saving SentBox, and TrashBox.
                 */
                bundle.putSerializable(Constants.CURRENT_EMAIL_SERIALIZABLE, emails.get(position));
                bundle.putString(Constants.CURRENT_EMAIL_TYPE, EMAIL_TYPE);
                if (emails.get(position).getId() == null) // isn't a saved object, and hence doesnt have an id
                    bundle.putLong(Constants.CURRENT_EMAIL_ID, -1);
                else
                    bundle.putLong(Constants.CURRENT_EMAIL_ID, emails.get(position).getId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    class ViewHolder {
        ImageView msgIcon;
        TextView msgFrom, msgSubject, msgDateRecv;
        LinearLayout msgContainer;

        public ViewHolder(View view) {
            msgContainer = (LinearLayout) view.findViewById(R.id.element_msg_container);
            msgIcon = (ImageView) view.findViewById(R.id.element_msg_icon);
            msgFrom = (TextView) view.findViewById(R.id.element_msg_from);
            msgDateRecv = (TextView) view.findViewById(R.id.element_msg_date);
            msgSubject = (TextView) view.findViewById(R.id.element_msg_subject);

            Typeface font = TheFont.getFont(context);

            msgDateRecv.setTypeface(font);
            msgFrom.setTypeface(font);
            msgSubject.setTypeface(font);

            view.setTag(this);
        }
    }

    public interface DeleteSelectedListener {
        void onItemClickedForDelete(ArrayList<EmailMessage> emailsToDelete);
    }
}