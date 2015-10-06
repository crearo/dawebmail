package rish.crearo.dawebmaillite.database;

import com.orm.SugarRecord;

import java.io.Serializable;

public class EmailMessage extends SugarRecord implements Serializable {

    public String fromname = "";
    public String fromaddress = ""; // new
    public String subject = "";
    public String subjectfull = "";
    public String date = "";
    public String dateentire = ""; // new
    public String contentlink = "";
    public String readunread = "";
    public String content = "";
    public String attlink1 = "";
    public String attlink2 = "";
    public String attlink3 = "";

    public String allto = "";
    public String cc = "";
    public String bcc;

    public EmailMessage() {
    }

    public EmailMessage(String f, String fa, String sub, String subfull,
                        String d, String de, String rur, String conlink, String con,
                        String atlink1, String atlink2, String atlink3, String allto,
                        String cc, String bcc) {
        this.fromname = f;
        this.fromaddress = fa;
        this.subject = sub;
        this.subjectfull = subfull;
        this.date = d;
        this.dateentire = de;
        this.readunread = rur;
        this.contentlink = conlink;
        this.content = con;
        this.attlink1 = atlink1;
        this.attlink2 = atlink2;
        this.attlink3 = atlink3;
        this.allto = allto;
        this.cc = cc;
        this.bcc = bcc;
    }

    public String getFromName() {
        return fromname;
    }

    public String getSubject() {
        return subject;
    }
}
