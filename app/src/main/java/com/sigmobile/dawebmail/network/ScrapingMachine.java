package com.sigmobile.dawebmail.network;

import android.content.Context;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.HttpRequest;
import com.jaunt.NotFound;
import com.jaunt.UserAgent;
import com.jaunt.component.Form;
import com.orm.SugarRecord;
import com.orm.query.Select;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Printer;

import java.util.ArrayList;

/*
* @author:Rishabh Bhardwaj
* DEPRECATED CLASS. I USED THIS FOR A GOOD YEAR OR SO
* I ALSO BOUGHT JAUNT-API. IT WAS A BRILLIANT SCRAPER.
* ANURAG AGARWAL, 201201198 HELPED ME A LOT WITH THE REST API AN FIGURING
* OUT A LOT OF ZIMBRA'S UNDOCUMENTED WORK
* WE ENDED UP USING THEIR REST + SOAP API :)
 */

public class ScrapingMachine {

    static UserAgent userAgent = new UserAgent();

    private ArrayList<EmailMessage> allNewEmails = new ArrayList<>();

    private String username, pwd;

    public ScrapingMachine(String username, String password, Context con) {
        this.username = username;
        this.pwd = password;
        userAgent.settings.connectTimeout = 20 * 1000;
    }

    public boolean logIn(String username, String pwd) {
        try {
            Printer.println("Attempting sendMsg with " + username + " " + pwd);
            userAgent.settings.checkSSLCerts = false;
            userAgent.visit(Constants.URL);
            Printer.println("Location is now " + userAgent.getLocation());

            if (userAgent.getLocation().equals(Constants.URL_INBOX)) {
                Printer.println("Already logged in");
                User.setIsLoggedIn(true);
                return true;
            }

            Form form = userAgent.doc.getForm("<form name=loginForm>");
            form.setTextField("username", username);
            form.setPassword("password", pwd);
            form.submit("Log In");

            userAgent.visit(Constants.URL_INBOX);

            Printer.println("location: " + userAgent.getLocation());

            String title = userAgent.doc.findFirst("<title>").getText();
            Printer.println(title);

            if (userAgent.getLocation().trim().equals(Constants.URL_INBOX)) {
                User.setIsLoggedIn(true);
                Printer.println("logged in successfully");
                return true;
            } else {
                User.setIsLoggedIn(false);
                Printer.println("sendMsg unsuccessful");
                return false;
            }
        } catch (Exception e) {
            Printer.println("Error while logging in - " + e.getMessage());
            Printer.println("sendMsg Unsuccessful");
            User.setIsLoggedIn(false);
            return false;
        }
    }

    public boolean scrapeAllMessagesfromInbox() {
        // go to the homepage
        try {
            if (!User.isLoggedIn()) {
                Printer.println("Not Logged in Logging in from scrapeallmsgs.");
                if (!logIn(username, pwd)) {
                    Printer.println("unable to sendMsg");
                    return false;
                }
            }
            //if logged in, or after logging in,visit the link to the url inbox
            userAgent.visit(Constants.URL_INBOX);
        } catch (Exception e) {
            Printer.println("Error visiting homepage, method - scrapeAllMessagesfromInbox = " + e.getMessage());
            return false;
        }
        boolean cont = scrapeMessagesfromPage();
        Printer.println("Go to next page? - " + cont);
        if (cont)
            while (goNextPage()) {
                scrapeMessagesfromPage();
            }
        if (allNewEmails.size() >= 15) {
            masterRefresh();
        }
        return true;
    }

    public boolean scrapeMessagesfromPage() {
        Boolean cont = true;
        Element anchormsglist = userAgent.doc.findEvery("<tbody>");

        Printer.println("Alright. Just before anchrmsglist");

        Printer.println(userAgent.getLocation());

        for (Element anchoreachmsg : anchormsglist.findEvery("<tr>")) {
            // now i have a list of each_msg with their ind tag within.
            // Printer.println("\nMSG\n"+anchoreachmsg.innerHTML());
            // to get senders name, subject, i have to use these inner HTML
            // tags

            Printer.println("Entered anchoreacchmsg");

            try {
                Element fromtag = null;
                Element datetag = null;
                Element subtag = null;
                String contentlink = "";
                String rur = "";
                String fromname = "";
                if (anchoreachmsg.findEach("<td>").size() == 5) {
                    fromtag = anchoreachmsg.findEach("<td>").getElement(2);
                    datetag = anchoreachmsg.findEach("<td>").getElement(3);
                    subtag = anchoreachmsg.findFirst("<span>");
                    contentlink = anchoreachmsg.findFirst("<a href>").getAt("href");
                    contentlink = convertLink(contentlink);
                    rur = anchoreachmsg.findFirst("<img>").getAt("title").trim();
                    fromname = "" + fromtag.getText().trim();
                } else {
                    fromtag = anchoreachmsg.findEach("<td>").getElement(1);
                    datetag = anchoreachmsg.findEach("<td>").getElement(2);
                    subtag = anchoreachmsg.findFirst("<span>");
                    contentlink = anchoreachmsg.findFirst("<a href>").getAt("href");
                    contentlink = convertLink(contentlink);
                    rur = anchoreachmsg.findFirst("<img>").getAt("title").trim();
                    fromname = "" + fromtag.getText().trim();
                    fromname = fromname.substring(0, fromname.length() - 12);
                }

                // if (fromName.contains("@daiict.ac"))
                // fromName.replace("@daiict.ac.in", "");
                String sub = "" + subtag.getText().trim();
                sub = convertText(sub);
                String d = "" + datetag.getText().trim().replace("&nbsp;", "").replace("\n", "");
                String hasatt = "isempty";
                if (anchoreachmsg.innerHTML().contains("title='Attachment'")) {
                    hasatt = "notempty";
                }
                Printer.println("hasatt = " + hasatt);

                Printer.println("sub - " + sub);

                // if the database is not empty, look for an email that matches the most recent email in database
                if (Select.from(EmailMessage.class).count() != 0) {
                    EmailMessage lastemail_in_db = EmailMessage.listAll(EmailMessage.class).get((int) (SugarRecord.count(EmailMessage.class, null, null) - 1));
                    if (fromname.equals(lastemail_in_db.fromName) && sub.equals(lastemail_in_db.subject)) {
                        Printer.println("Found same email!");
                        cont = false;
                        break;
                    }
                }

//                allNewEmails.add(new EmailMessage(fromname, "isempty", sub,
//                        "isempty", d, "isempty", rur, contentlink, "isempty",
//                        hasatt, "isempty");

                // fetchEmailContent(allNewEmails.get(allNewEmails.size()-1));
            } catch (Exception e) {
                Printer.println("Error in scrapeAllMessagesfromPage" + e.getMessage());
            }
        }
        return cont;
    }

    public boolean goNextPage() {
        String div_yesnext = "nonextpageman";
        try {
            Elements npimgs = userAgent.doc.findEvery("<img>");
            for (Element img : npimgs) {
                try {
                    if (img.getAt("title").equals("go to next page")) {
                        div_yesnext = "true";
                        break;
                    }
                } catch (NotFound e) {
                    Printer.println("Error in code - " + e.getMessage());
                }
            }
            Printer.println("Found title - " + div_yesnext);

            if (div_yesnext.equals("true")) {
                String urltonext = userAgent.doc.findFirst("<a id='NEXT_PAGE'>").getAt("href");
                userAgent.visit(convertLink(urltonext));
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            Printer.println("Error in code " + e.getMessage());
        }
        return false;
    }

    public void clearNewEmails() {
        allNewEmails.clear();
    }

    public String convertLink(String link) {
        link = link.replaceAll("&#039;", "'");
        link = link.replaceAll("amp;", "");
        return link;
    }

    public static String convertText(String text) {
        text = text.replaceAll("&#039;", "'");
        text = text.replaceAll("amp;", "");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&#034;", "\"");
        return text;
    }

    public boolean fetchEmailContent(EmailMessage email) {

        Printer.println("-------ALL CONTENT------");
//        String link = email.contentID;

        try {
//            Printer.println("connecting to link " + link);
//            userAgent.visit(link);
            String resp = userAgent.doc.innerHTML();
            Printer.println("Attempt to visit, loc now " + userAgent.getLocation());
            int totalattlinks = 0;

            // for the link to the att
            Elements attlinks = userAgent.doc.findEvery("<a>");
            for (Element attlink : attlinks) {
                if (attlink.innerText().equals("Download")) {
                    totalattlinks++;
                    Printer.println("HAS LINK");
                    String finallink = convertLink(attlink.getAt("href"));
                    Printer.println("download link = " + finallink);
//                    if (totalattlinks == 1)
//                        email.attlink1 = finallink;
//                    if (totalattlinks == 2)
//                        email.attlink2 = finallink;
//                    if (totalattlinks == 3)
//                        email.attlink3 = finallink;
                }
            }

            System.out.println("found all attachments");

            // // for the att name
            // Elements atts = userAgent.doc.findEvery("<img>");
            // for (Element att : atts) {
            // if (att.outerHTML().contains("???application")) {
            // String attname = att.getAt("alt");
            // }
            // }

            String emailtext = "";
            if (resp.contains("noscript")) {
                if (resp.contains("doc.write(")) {
                    emailtext = resp.substring(resp.indexOf("doc.write(") + 10,
                            resp.indexOf("doc.close()") - 4);
                    emailtext = emailtext.substring(1);
                    emailtext = emailtext.substring(0, emailtext.length() - 2);

                    emailtext = resp.substring(resp.indexOf("doc.write(") + 10,
                            resp.indexOf("doc.close()") - 4);

                    emailtext = emailtext.substring(1);
                    emailtext = emailtext.substring(0, emailtext.length() - 2);

                    emailtext = emailtext.replaceAll("\\\\u003C", "<");
                    emailtext = emailtext.replaceAll("\\\\u003E", ">");

                } else
                    emailtext = "This webmail contains pdf/webpage content.\nText for this format of the webmail is currrently unavailable.";
            } else {
                emailtext = resp.substring(resp.indexOf("iframeBody"));
                emailtext = emailtext.substring(emailtext.indexOf(">") + 1, emailtext.indexOf("</td>"));
            }

            char emailchars[] = emailtext.toCharArray();

            for (int i = 0; i < emailchars.length - 1; i++)
                if (emailchars[i] == '\\' && emailchars[i + 1] == 'n') {
                    emailchars[i] = '\0';
                    emailchars[i + 1] = '\0';
                }

            emailtext = String.copyValueOf(emailchars);

            Printer.println("emailtext = " + emailtext);

            email.content = emailtext;
            email.readUnread = "Read Message";

            Elements msghdrvalues = userAgent.doc
                    .findEvery("<td class='MsgHdrValue'>");

            String address = msghdrvalues.getElement(0).getText().toString()
                    .trim().replaceAll("&gt;", "");
            address = address.substring(address.indexOf(";") + 1);
            String subject = msghdrvalues.getElement(1).getText().toString()
                    .trim();
            subject = convertText(subject);

            String datefull = userAgent.doc
                    .findFirst("<td class='MsgHdrSent'>").getText().toString()
                    .trim();

            datefull = datefull.substring(datefull.indexOf(",") + 2);

            email.fromAddress = address;
            email.save();
            System.out.println("Saved Email");
            return true;
        } catch (Exception e) {
            Printer.println("Error in fetchemailcontent" + e.getMessage());
            return false;
        }
    }

    public boolean getValues_forDelete(ArrayList<EmailMessage> emails_tobedeleted) {
        // cloning into this arraylist so that the webmials are deleted from the
        // app only once the request is done on the website
        ArrayList<EmailMessage> emails_tobedeleted_clone = new ArrayList<EmailMessage>();
        for (EmailMessage email : emails_tobedeleted)
            emails_tobedeleted_clone.add(email);

        try {
            ArrayList<String> values_checkboxes = new ArrayList<String>();

            if (!User.isLoggedIn()) {
                Printer.println("Not logged in man");
                if (logIn(username, pwd)) {
                    Printer.println("So logged you in");
                } else {
                    return false;
                }
            }

            userAgent.visit(Constants.URL_INBOX); // for a refresh so that im sure were deleting the correct emails

            Boolean cont = true;
            while (cont) {
                Element anchormsglist = userAgent.doc.findEvery("<tbody>");
                for (Element anchoreachmsg : anchormsglist.findEvery("<tr>")) {
                    try {
                        Element fromtag = anchoreachmsg.findEach("<td>")
                                .getElement(2);
                        Element subtag = anchoreachmsg.findFirst("<span>");
                        Element chckbox = anchoreachmsg.findFirst("<input>");

                        String globalvalue = chckbox.getAt("value");

                        System.out
                                .println("from - " + fromtag.getText().trim());
                        Printer.println("sub - " + subtag.getText().trim());

                        for (int i = 0; i < emails_tobedeleted.size(); i++) {
                            if ((fromtag.getText().toString().trim())
                                    .equals(emails_tobedeleted.get(i).fromName.trim())
                                    && convertText(subtag.getText().toString().trim())
                                    .equals(emails_tobedeleted.get(i).subject
                                            .trim())) {
                                System.out.println("Found!\nEmail to be deleted "
                                        + emails_tobedeleted.get(i).fromName
                                        + " " + globalvalue);
                                values_checkboxes.add(globalvalue);
                                emails_tobedeleted.remove(i);
                                i--;
                            }
                        }

                        if (emails_tobedeleted.size() == 0)
                            break;
                    } catch (Exception e) {
                        Printer.println("Error in code " + e.getMessage());
                        return false;
                    }

                }
                Printer.println("\n\n---\n\nEmailleft = " + emails_tobedeleted.size());
                for (EmailMessage email : emails_tobedeleted)
                    Printer.println(email.fromName + email.subject);
                if (emails_tobedeleted.size() != 0) {
                    if (goNextPage() == false) {
                        System.out.println(emails_tobedeleted.size() + " webmails that are already deleted but exist here");
                        for (EmailMessage email : emails_tobedeleted) {
                            Printer.println(email.getId() + " "
                                    + email.fromName + " " + email.subject);
                            EmailMessage.findById(EmailMessage.class,
                                    email.getId()).delete();
                            Printer.println("--Deleted--");
                        }
                        cont = false;
                        break;
                    }
                } else {
                    cont = false;
                    break;
                }
            }
            Printer.println("sending values to delete function");
            boolean result = deleteMails(values_checkboxes, emails_tobedeleted_clone);
            return result;
        } catch (Exception e) {
            Printer.println(e.getMessage());
            return false;
        }
    }

    public boolean deleteMails(ArrayList<String> values_checkboxes, ArrayList<EmailMessage> emails_tobedeleted) {
        Printer.println("deleting");
        try {
            // for this I will have to refresh the page, get the 'value' tags
            // for each email, and delete those emails that he has selected. so
            // basically here, i will fetch the values in an arraylist as
            // argument, and in another function fetch the corresponding value
            // tags!

            Form form = userAgent.doc.getForm(1);
            HttpRequest request = form.getRequest("Delete");
            for (String value : values_checkboxes) {
                request.addNameValuePair("id", "" + value);
                Printer.println("Value " + value + " added");
            }
            userAgent.send(request);
            Printer.println("Request done on internet, now on app ");
            for (EmailMessage email : emails_tobedeleted) {
                Printer.println(email.getId() + " " + email.fromName + " " + email.subject);
                EmailMessage.findById(EmailMessage.class, email.getId()).delete();
                Printer.println("--Deleted--");
            }
            Printer.println("All Deleted");
            return true;
        } catch (Exception e) {
            Printer.println("Error in deleting- " + e.getMessage());
            return false;
        }
    }

    public void masterRefresh() {
        EmailMessage.deleteAll(EmailMessage.class);
        Printer.println("master refresh, deleted all. now, what is remaining in database -");
        Printer.println(EmailMessage.count(EmailMessage.class, null, null) + "");
    }

    public ArrayList<EmailMessage> getNewEmails() {
        return allNewEmails;
    }
}