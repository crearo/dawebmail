package rish.crearo.dawebmaillite.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface MasterRefreshListener {

    void onPreMasterRefresh();

    void onPostMasterRefresh(boolean success);
}
