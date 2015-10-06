package rish.crearo.dawebmaillite.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface LoginListener {

    public void onPreLogin();

    public void onPostLogin(boolean loginSuccess, String timeTaken);

}
