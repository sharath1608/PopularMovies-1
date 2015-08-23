package app.sunshine.android.example.com.popmovies;

import android.util.Log;

import java.io.IOException;

/**
 * Created by Asus1 on 8/23/2015.
 */
public class NetworkUtils {

    // A more effective way to check if the network is actually available in contrast to other methods where connectivity does not necessarily mean availability.
    // Credit: StackOverFlow user
    public static boolean isNetworkAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8"); //ping google DNS
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            Log.e("NetworkUtils","Exception in checking for network availability");
        } catch (InterruptedException ie){

        }
        return false;
    }

}
