package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.User;
import com.zimbra.wsdl.zimbraservice_wsdl.ZcsService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbra.HeaderAccountInfo;
import zimbra.HeaderContext;
import zimbraaccount.AuthRequest;
import zimbraaccount.AuthResponse;
import zimbramail.EmailAddrInfo;
import zimbramail.MimePartInfo;
import zimbramail.MsgToSend;
import zimbramail.SendMsgRequest;
import zimbramail.SendMsgResponse;

/**
 * Created by rish on 18/1/16.
 */
public class SendMail extends AsyncTask<Void, Void, Void> {

    SendMailListener sendMailListener;
    Context context;
    String mailSubject, mailContent, mailToAddress;
    boolean important = false;

    boolean result = false;

    User currentUser;

    public SendMail(User user, SendMailListener sendMailListener, Context context, String mailSubject, String mailContent, String mailToAddress, boolean important) {
        this.sendMailListener = sendMailListener;
        this.context = context;
        this.mailSubject = mailSubject;
        this.mailContent = mailContent;
        this.mailToAddress = mailToAddress;
        this.important = important;
        this.currentUser = user;
    }

    @Override
    protected void onPreExecute() {
        sendMailListener.onPreSend();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sendMsg();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        sendMailListener.onPostSend(result);
        super.onPostExecute(aVoid);
    }

    private void sendMsg() {

        ZcsService zcsService = new ZcsService("https://webmail.daiict.ac.in/service/soap", 2, true);

        AccountSelector accountSelector = new AccountSelector();
        accountSelector.setBy(AccountBy.OPT5_NAME);
        accountSelector.setValue(currentUser.username);
        System.out.println("Att count selector : " + accountSelector.getAttributeCount());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccount(accountSelector);
        authRequest.setCsrfTokenSecured(true);
        authRequest.setPersistAuthTokenCookie(true);
        authRequest.setPassword(currentUser.password);

        try {
            AuthResponse authResponse = zcsService.authRequest(authRequest, null);

            HeaderAccountInfo headerAccountInfo = new HeaderAccountInfo();
            headerAccountInfo.setValue(currentUser.username);
            headerAccountInfo.setBy("name");

            HeaderContext headerContext = new HeaderContext();
            headerContext.setAccount(headerAccountInfo);
            headerContext.setAuthToken(authResponse.getAuthToken());
            headerContext.setCsrfToken(authResponse.getCsrfToken());

            zimbra.Context context = new zimbra.Context();
            context.setContext(headerContext);

            MsgToSend msgToSend = new MsgToSend();

    /* When replying or forwarding
     * For value of rt choose r when replying and w when forwarding
     * Also checkout irt -> in reply to message id header when replying
    msgToSend.setOrigid("<id>");
    msgToSend.setRt("r|w");*/

    /* For setting priority of a mail high (!) or low (?)
    msgToSend.setF("!|?"); */

            EmailAddrInfo[] emailAddrInfos = new EmailAddrInfo[1];
            emailAddrInfos[0] = new EmailAddrInfo();
            emailAddrInfos[0].setA(mailToAddress); //email address
            emailAddrInfos[0].setT("t"); //(f)rom, (t)o, (c)c, (b)cc, (r)eply-to, (s)ender, read-receipt (n)otification, (rf) resent-from
            emailAddrInfos[0].setP(mailToAddress); //The comment/name part of an address

            msgToSend.setE(emailAddrInfos);

            MimePartInfo[] mimePartInfos = new MimePartInfo[3];
            mimePartInfos[0] = new MimePartInfo();
            mimePartInfos[0].setContent(mailContent);
            mimePartInfos[0].setCt("text/plain"); //content type

            mimePartInfos[1] = new MimePartInfo();
            mimePartInfos[1].setContent(mailContent);
            mimePartInfos[1].setCt("text/html");

            Bitmap bm = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.ic_action_mail);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

            mimePartInfos[2] = new MimePartInfo();
            mimePartInfos[2].setContent(encodedImage);
            mimePartInfos[2].setCt("image/jpeg");

            MimePartInfo mimePartInfo = new MimePartInfo();
            mimePartInfo.setCt("multipart/mixed"); //content type
            mimePartInfo.setMp(mimePartInfos);

            msgToSend.setMp(mimePartInfo);
            msgToSend.setSu(mailSubject);
            if (important)
                msgToSend.setF("!");

            System.out.println("Subject : " + msgToSend.getSu());
            System.out.println("Att Count : " + msgToSend.getAttributeCount());

            SendMsgRequest sendMsgRequest = new SendMsgRequest();
            sendMsgRequest.setM(msgToSend);

            sendMsgRequest.setSuid(String.valueOf(new Date().getTime())); //Timestamp as uid

            System.out.println("Message : " + sendMsgRequest.getM());

            SendMsgResponse sendMsgResponse = zcsService.sendMsgRequest(sendMsgRequest, context);

            System.out.println("RESPONSE : " + sendMsgRequest.getSuid());
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            result = false;
        }
    }
}