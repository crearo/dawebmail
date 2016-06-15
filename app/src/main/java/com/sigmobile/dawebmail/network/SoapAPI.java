package com.sigmobile.dawebmail.network;

import android.content.Context;
import android.util.Log;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.User;
import com.zimbra.wsdl.zimbraservice_wsdl.ZcsService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;

import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbra.CursorInfo;
import zimbra.HeaderAccountInfo;
import zimbra.HeaderContext;
import zimbraaccount.AuthRequest;
import zimbraaccount.AuthResponse;
import zimbramail.ActionSelector;
import zimbramail.CalTZInfo;
import zimbramail.EmailAddrInfo;
import zimbramail.MimePartInfo;
import zimbramail.MsgActionRequest;
import zimbramail.MsgActionResponse;
import zimbramail.MsgToSend;
import zimbramail.SearchRequest;
import zimbramail.SearchResponse;
import zimbramail.SearchResponseChoice;
import zimbramail.SendMsgRequest;
import zimbramail.SendMsgResponse;

/**
 * Created by rish on 19/1/16.
 */
public class SoapAPI {

    private final String TAG = "SoapAPI";

    private ZcsService zcsService;
    private User zcsServiceOfUser;
    private zimbra.Context zcsServiceContext;

    public SoapAPI() {

    }

    public boolean performMailAction(Context context, User user, String mailAction, String contentID) {
        setupZcsServiceForUser(context, user);

        MsgActionRequest msgActionRequest = new MsgActionRequest();
        ActionSelector actionSelector = new ActionSelector();
        actionSelector.setId(contentID);
        actionSelector.setOp(mailAction);
        msgActionRequest.setAction(actionSelector);

        try {
            MsgActionResponse msgActionResponse = zcsService.msgActionRequest(msgActionRequest, zcsServiceContext);
            if (msgActionResponse.getAction().getId().equals(contentID) && msgActionResponse.getAction().getOp().equals(mailAction))
                return true;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMail(Context context, User user, String mailToAddress, String mailSubject, String mailContent, boolean important) {

        setupZcsServiceForUser(context, user);

        /* When replying or forwarding
         * For value of rt choose r when replying and w when forwarding
         * Also checkout irt -> in reply to message id header when replying
        msgToSend.setOrigid("<id>");
        msgToSend.setRt("r|w");*/

        /* For setting priority of a mail high (!) or low (?)
        msgToSend.setF("!|?"); */

        MsgToSend msgToSend = new MsgToSend();

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
        mimePartInfo.setCt("multipart/mixed"); //content type
        mimePartInfo.setMp(mimePartInfos);

        msgToSend.setMp(mimePartInfo);
        msgToSend.setSu(mailSubject);
        if (important)
            msgToSend.setF("!");

        SendMsgRequest sendMsgRequest = new SendMsgRequest();
        sendMsgRequest.setM(msgToSend);

        sendMsgRequest.setSuid(String.valueOf(new Date().getTime())); //Timestamp as uid

        try {
            SendMsgResponse sendMsgResponse = zcsService.sendMsgRequest(sendMsgRequest, zcsServiceContext);
            if (sendMsgResponse.getM().getId() != null)
                return true;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean fetchEmailsFromTime(Context context, User user, long timeInMillis) {

        setupZcsServiceForUser(context, user);

        SearchRequest searchRequest = new SearchRequest();
        CalTZInfo calTZInfo = new CalTZInfo();
        calTZInfo.setId("Asia/Kolkata");
        searchRequest.setTz(calTZInfo);
        CursorInfo cursorInfo = new CursorInfo();
        cursorInfo.setSortVal(String.valueOf(timeInMillis));
        cursorInfo.setId("3063");
        searchRequest.setCursor(cursorInfo);
        searchRequest.setQuery("in:inbox");
        searchRequest.setTypes("message");
        searchRequest.setOffset(10);
        searchRequest.setNeedExp(true);
        searchRequest.setSortBy("dateDesc");

        try {
            SearchResponse searchResponse = zcsService.searchRequest(searchRequest, zcsServiceContext);
            Log.d(TAG, "SearachResponse is " + searchResponse.getTotal() + " " + searchResponse.toString());
            for (SearchResponseChoice searchResponseChoice : searchResponse.getSearchResponseChoice_type0()) {
                Log.d(TAG, searchResponseChoice.getM().getS() + " " + searchResponseChoice.getM().getSu());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupZcsServiceForUser(Context context, User user) {

        /**
         * This is similar to setting up auth for a user.
         * Once an auth is set for a user, you dont have to keep creating it again and again.
         * Hence, return if zcsService is requested for user whose zcsService is already setup.
         */

        if (zcsServiceOfUser != null && user.equals(zcsServiceOfUser))
            return;

        zcsServiceOfUser = user;
        zcsService = new ZcsService(context.getString(R.string.soap_url), 2, true);

        AccountSelector accountSelector = new AccountSelector();
        accountSelector.setBy(AccountBy.OPT5_NAME);
        accountSelector.setValue(user.username);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccount(accountSelector);
        authRequest.setCsrfTokenSecured(true);
        authRequest.setPersistAuthTokenCookie(true);
        authRequest.setPassword(user.password);

        try {
            AuthResponse authResponse = zcsService.authRequest(authRequest, null);

            HeaderAccountInfo headerAccountInfo = new HeaderAccountInfo();
            headerAccountInfo.setValue(user.username);
            headerAccountInfo.setBy("name");

            HeaderContext headerContext = new HeaderContext();
            headerContext.setAccount(headerAccountInfo);
            headerContext.setAuthToken(authResponse.getAuthToken());
            headerContext.setCsrfToken(authResponse.getCsrfToken());

            zimbra.Context zcsContext = new zimbra.Context();
            zcsContext.setContext(headerContext);

            zcsServiceContext = zcsContext;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}