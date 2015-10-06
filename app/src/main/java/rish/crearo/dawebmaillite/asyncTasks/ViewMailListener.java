package rish.crearo.dawebmaillite.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface ViewMailListener {

    void onPreView();

    void onPostView(boolean success);

}
