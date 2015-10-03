package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Asus1 on 8/23/2015.
 */
public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork!=null && activeNetwork.isConnectedOrConnecting());
    }
}
