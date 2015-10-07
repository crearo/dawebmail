package rish.crearo.dawebmaillite.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import com.sigmobile.utils.Constants;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by rish on 26/7/15.
 */
public class StudentDetails implements Serializable{

    public String username = "";
    public String blue = "";
    public String regTime = "";
    Context context;

    public StudentDetails(){
        username = "";
        blue = "";
        regTime = "";
        context = null;
    }

    public void setValues(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        this.username = settings.getString(Constants.BUNDLE_USERNAME, "none");
        this.blue = settings.getString(Constants.BUNDLE_PWD, "none");
        this.regTime = DateFormat.getDateTimeInstance().format(new Date());
        this.context = context;
    }

    public void addStudentDetails(StudentDetails details) {
//        new ServerLoader(context).addStudentDetails(details);
    }
}
