package com.myappfood.appfood.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import com.myappfood.appfood.Model.User;

public class Common {
    public static User currentUser;

    public static String PHONE_TEXT = "userPhone";

    public static final String INTENT_FOOD_ID = "FoodId";

    public static String convertCodeToStatus(String status){
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }

    public static final String DELETE ="Delete";
    public static boolean isConnectedToInterner(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
    if (info != null) {
        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
    }
    return false;
}
}
