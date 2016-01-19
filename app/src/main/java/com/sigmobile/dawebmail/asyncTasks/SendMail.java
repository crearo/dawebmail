package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sigmobile.dawebmail.database.User;
import com.zimbra.wsdl.zimbraservice_wsdl.ZcsService;

import org.xmlpull.v1.XmlPullParserException;

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

    String username, pwd;

    boolean result = false;

    public SendMail(SendMailListener sendMailListener, Context context, String mailSubject, String mailContent, String mailToAddress) {
        this.sendMailListener = sendMailListener;
        this.context = context;
        this.mailSubject = mailSubject;
        this.mailContent = mailContent;
        this.mailToAddress = mailToAddress;
        this.username = User.getUsername(context);
        this.pwd = User.getPassword(context);
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
        accountSelector.setValue(username);
        System.out.println("Att count selector : " + accountSelector.getAttributeCount());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccount(accountSelector);
        authRequest.setCsrfTokenSecured(true);
        authRequest.setPersistAuthTokenCookie(true);
        authRequest.setPassword(pwd);

        try {
            AuthResponse authResponse = zcsService.authRequest(authRequest, null);

            HeaderAccountInfo headerAccountInfo = new HeaderAccountInfo();
            headerAccountInfo.setValue(username);
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

            MimePartInfo[] mimePartInfos = new MimePartInfo[2];
            mimePartInfos[0] = new MimePartInfo();
            mimePartInfos[0].setContent(mailContent);
            mimePartInfos[0].setCt("text/plain"); //content type

            mimePartInfos[1] = new MimePartInfo();
            mimePartInfos[1].setContent(mailContent);
            mimePartInfos[1].setCt("text/html");

            MimePartInfo mimePartInfo = new MimePartInfo();
            mimePartInfo.setCt("multipart/alternative"); //content type
            mimePartInfo.setMp(mimePartInfos);

            msgToSend.setMp(mimePartInfo);
            msgToSend.setSu(mailSubject);
            msgToSend.setF("!");

            System.out.println("Subject : " + msgToSend.getSu());
            System.out.println("Att Count : " + msgToSend.getAttributeCount());

            SendMsgRequest sendMsgRequest = new SendMsgRequest();
            sendMsgRequest.setM(msgToSend);

            sendMsgRequest.setSuid(String.valueOf(new Date().getTime())); //Timestamp as uid

            System.out.println("Message : " + sendMsgRequest.getM());

            SendMsgResponse sendMsgResponse = zcsService.sendMsgRequest(sendMsgRequest, context);

            Log.d("TAG", sendMsgResponse.getName());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}