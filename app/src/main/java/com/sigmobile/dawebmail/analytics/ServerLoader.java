package com.sigmobile.dawebmail.analytics;


public class ServerLoader {

    /*
        Used to load the server with data. Final sending of data occurs in Volley Commands.


        DEPRECATING THIS AS OF NOW.
        WILL IMPLEMENT IN THE FUTURE ONCE AGAIN.

     */

    /*

    private final static String ACTION_PREF_KEY = "ACTION";

    private ArrayList<ActionDetails> ActionQueue;

    private Context context;

    public ServerLoader(Context context) {
        this.context = context;
        ActionQueue = getActionPrefs();
    }

    private ArrayList<ActionDetails> getActionPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(ACTION_PREF_KEY, Context.MODE_PRIVATE);
        ArrayList<ActionDetails> ActionQueue = new ArrayList<>();

        if (prefs.contains(ACTION_PREF_KEY)) {
            String jsonFavorites = prefs.getString(ACTION_PREF_KEY, null);
            try {
                JSONArray jsonArray = new JSONArray(jsonFavorites);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ActionDetails actionDetails = new ActionDetails(jsonObject.getString("action_StudentID"), jsonObject.getString("action_Action"), jsonObject.getString("action_Connection"), jsonObject.getString("action_ConnectionDetails"), jsonObject.getString("action_TimeStamp"), jsonObject.getString("action_TimeTaken"), jsonObject.getString("action_Success"));
                    ActionQueue.add(actionDetails);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ActionQueue;
    }

    private void setActionPrefs(ArrayList<ActionDetails> actionPrefs) {
        SharedPreferences prefs = context.getSharedPreferences(ACTION_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(actionPrefs);

        edit.putString(ACTION_PREF_KEY, json);
        edit.commit();
    }

    public void clearActionPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(ACTION_PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        ArrayList<ActionDetails> empty = new ArrayList<>();
        Gson gson = new Gson();
        String json = gson.toJson(empty);

        edit.putString(ACTION_PREF_KEY, json);
        edit.commit();
    }

    public void addActionDetails(String username, String action, String timeTaken, String success) {
        ActionDetails actionDetails = null;
        String timedate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        if (ConnectionManager.isConnectedByMobileData(context)) {
            actionDetails = new ActionDetails(username, action, Constants.MOBILE_DATA, ConnectionManager.getNetworkClass(context), timedate, timeTaken, success);
        } else if (ConnectionManager.isConnectedByWifi(context)) {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            String wifiname = wifiInfo.getSSID();
            actionDetails = new ActionDetails(username, action, Constants.WIFI, wifiname, timedate, timeTaken, success);
        } else {
            actionDetails = new ActionDetails(username, action, "not connected", "-", timedate, timeTaken, "-");
        }

        System.out.println("Adding action detail - " + actionDetails.action_Action);
        ActionQueue = getActionPrefs();
        ActionQueue.add(actionDetails);
        setActionPrefs(ActionQueue);
        setPrefs(Constants.prefPENDINGBIT_ACTION, true);
    }

    public void sendToServer() {
        ActionQueue = getActionPrefs();
        VolleyCommands volleyCommands = new VolleyCommands(context);
        if (getPrefs(Constants.prefPENDINGBIT_ACTION)) {
            volleyCommands.POSTAction(ActionQueue);
        }
        if (getPrefs(Constants.prefPENDINGBIT_FEEDBACK)) {
            SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            FeedbackDetails feedback = new FeedbackDetails(settings.getString(Constants.BUNDLE_USERNAME, "none"), getPrefs(Constants.FEEDBACK_TEXT, ""), currentDateTimeString);
            new VolleyCommands(context).POSTFeedback(context, feedback, false);
            setPrefs(Constants.prefPENDINGBIT_FEEDBACK, true);
        }
    }

    private void setPrefs(String prefWhich, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(prefWhich, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putBoolean(prefWhich, value);
        edit.commit();
    }

    private boolean getPrefs(String prefWhich) {
        SharedPreferences prefs = context.getSharedPreferences(prefWhich, Context.MODE_PRIVATE);
        return prefs.getBoolean(prefWhich, true);
    }

    private String getPrefs(String prefWhich, String s) {
        SharedPreferences prefs = context.getSharedPreferences(prefWhich, Context.MODE_PRIVATE);
        return prefs.getString(prefWhich, "NONE");
    }
    */
}