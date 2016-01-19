package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sigmobile.dawebmail.database.User;
import com.zimbra.wsdl.zimbraservice_wsdl.ZcsService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbra.HeaderAccountInfo;
import zimbra.HeaderContext;
import zimbraaccount.AuthRequest;
import zimbraaccount.AuthResponse;
import zimbramail.AutoCompleteMatch;
import zimbramail.AutoCompleteResponse;

/**
 * Created by rish on 19/1/16.
 */
public class AutoCompleteRequest extends AsyncTask<Void, Void, Void> {

    boolean result = false;
    AutoCompleteListener autoCompleteListener;
    String username, pwd;
    Context context;
    String searchText;
    String addresses[];

    public AutoCompleteRequest(Context context, AutoCompleteListener autoCompleteListener, String searchText) {
        this.autoCompleteListener = autoCompleteListener;
        this.context = context;
        this.searchText = searchText;
        this.username = User.getUsername(context);
        this.pwd = User.getPassword(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("ACR", "Trying to fetchContacts");
        fetchContacts();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("ACR", "Fetched all contacts");
        autoCompleteListener.onAutoComplete(addresses);
    }

    private void fetchContacts() {

        ZcsService zcsService = new ZcsService("https://webmail.daiict.ac.in/service/soap", 2, true);

        AccountSelector accountSelector = new AccountSelector();
        accountSelector.setBy(AccountBy.OPT5_NAME);
        accountSelector.setValue(username);

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

            zimbramail.AutoCompleteRequest autoCompleteRequest = new zimbramail.AutoCompleteRequest();
            autoCompleteRequest.setName(searchText);
            autoCompleteRequest.setNeedExp(true);

            AutoCompleteResponse autoCompleteResponse = zcsService.autoCompleteRequest(autoCompleteRequest, context);
            AutoCompleteMatch autoCompleteMatch[] = autoCompleteResponse.getMatch();
            addresses = new String[autoCompleteMatch.length];
            for (int i = 0; i < addresses.length; i++) {
                addresses[i] = autoCompleteMatch[i].getEmail();
            }
            result = true;
        } catch (IOException e) {
            addresses = null;
            e.printStackTrace();
            result = false;
        } catch (XmlPullParserException e) {
            addresses = null;
            e.printStackTrace();
            result = false;
        }
    }
}