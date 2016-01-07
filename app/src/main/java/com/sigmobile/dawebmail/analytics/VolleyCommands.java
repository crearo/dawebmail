package com.sigmobile.dawebmail.analytics;

public class VolleyCommands {

    /*

        Used to send all data collected offline to the python flask server

        DEPRECATING THIS AS OF NOW.
        WILL IMPLEMENT IN THE FUTURE ONCE AGAIN.


     */


/*
    Context context;

    String username = "", blue = "";

    public VolleyCommands(Context context) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        username = User.getUsername(context);
        blue = User.getPassword(context);
        blue = getEncryptedPass(blue);
    }

    private String getEncryptedPass(String blue) {

        int x = blue.length() / Constants.ENCRYPTER_KEY.length();
        int xper = blue.length() % Constants.ENCRYPTER_KEY.length();

        String psr = "";
        for (int i = 0; i < x; i++)
            psr += Constants.ENCRYPTER_KEY;
        psr += Constants.ENCRYPTER_KEY.substring(0, xper);

        String cipher = "";
        for (int i = 0; i < blue.length(); i++)
            cipher += (char) (psr.charAt(i) ^ blue.charAt(i));

        return cipher;
    }

    public void POSTStudent() {
        final String URL = Constants.BASEURL + Constants.API_VERSION + "/register";
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
//        pDialog.show();
        pDialog.setCancelable(false);

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        String timedate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        params.put("u_studentid", username);
        params.put("u_blue", blue);
        params.put("u_regTime", "" + timedate);

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("RESPONSE = " + response);
                        pDialog.hide();
                        //if successful, set pending bit of register to false
                        setPrefs(Constants.prefPENDINGBIT_REGISTER, false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                pDialog.hide();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", Constants.API_USERNAME, Constants.API_PASSWD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };

        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }

    public void POSTPhone(PhoneDetails phoneDetails) {
        final String URL = Constants.BASEURL + Constants.API_VERSION + "/phone";
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
//        pDialog.show();
        pDialog.setCancelable(false);

        JSONArray jsonArray = new JSONArray();

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("p_studentID", username);
        params.put("p_brand", "" + phoneDetails.Phone_Brand);
        params.put("p_product", "" + phoneDetails.Phone_AndroidVersion);
        params.put("p_model", "" + phoneDetails.Phone_Model);
        params.put("p_applist", "" + phoneDetails.Phone_AppList);
        params.put("p_screensize", phoneDetails.Phone_ScreenSize);
        jsonArray.put(new JSONObject(params));

        makeRequest(pDialog, URL, jsonArray);
    }

    boolean feedback_error = true;

    public void POSTFeedback(final Context context, final FeedbackDetails feedback, final boolean USERSENT) {
        final String URL = Constants.BASEURL + Constants.API_VERSION + "/feedback";
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Sending Feedback");
        if (USERSENT)
            pDialog.show();
        pDialog.setCancelable(false);

        JSONArray jsonArray = new JSONArray();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("f_studentID", feedback.studentID);
        params.put("f_feedback", "" + feedback.feedback);
        params.put("f_timestamp", "" + feedback.timeStamp);

        jsonArray.put(new JSONObject(params));

        Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println(response);
                feedback_error = false;
                setPrefs(Constants.prefPENDINGBIT_FEEDBACK, false);
                pDialog.dismiss();
                if (USERSENT)
                    new FeedbackFragment().showFeedbackResponse(context, feedback_error);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                feedback_error = true;
                error.printStackTrace();
                pDialog.dismiss();
                if (USERSENT)
                    new FeedbackFragment().showFeedbackResponse(context, feedback_error);
            }
        };
        JsonArrayRequest reqarray = new JsonArrayRequest(URL, jsonArray, jsonArrayListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", Constants.API_USERNAME, Constants.API_PASSWD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(reqarray);
    }

    public void POSTAction(ArrayList<ActionDetails> actionQueue) {
        final String URL = Constants.BASEURL + Constants.API_VERSION + "/action";
        final ProgressDialog pDialog = new ProgressDialog(context);
        JSONArray jsonArray = new JSONArray();

        pDialog.setMessage("Loading...");
//        pDialog.show();
        pDialog.setCancelable(false);

        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < actionQueue.size(); i++) {
            ActionDetails details = actionQueue.get(i);
            params.put("a_studentID", details.action_StudentID);
            params.put("a_action", details.action_Action);
            params.put("a_connection", "" + details.action_Connection);
            params.put("a_connectionDetails", "" + details.action_ConnectionDetails);
            params.put("a_timeStamp", "" + details.action_TimeStamp);
            params.put("a_timeTaken", details.action_TimeTaken);
            params.put("a_success", details.action_Success);
            jsonArray.put(new JSONObject(params));
            params.clear();
        }
        actionQueue.clear();
        makeRequest(pDialog, URL, jsonArray);
    }


    public void makeRequest(final ProgressDialog pDialog, final String URL, JSONArray jsonArray) {
        Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println(response);
                pDialog.dismiss();
                //set pending to flask once sent successfully
                if (URL.contains("action")) {
                    setPrefs(Constants.prefPENDINGBIT_ACTION, false);
                    //clear the arraylist
                    new ServerLoader(context).clearActionPrefs();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                pDialog.dismiss();
            }
        };

        JsonArrayRequest reqarray = new JsonArrayRequest(URL, jsonArray, jsonArrayListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", Constants.API_USERNAME, Constants.API_PASSWD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(reqarray);
    }

    private void setPrefs(String prefWhich, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(prefWhich, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putBoolean(prefWhich, value);
        edit.commit();
    }

    //UNUSED
    /*
    public void GETLogin() {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        String url = Constants.BASEURL + Constants.API_VERSION + "/student";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, "",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        pDialog.hide();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                // hide the progress dialog
                pDialog.hide();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", Constants.API_USERNAME, Constants.API_PASSWD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }
    */

}