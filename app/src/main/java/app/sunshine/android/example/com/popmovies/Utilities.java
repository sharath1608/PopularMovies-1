package app.sunshine.android.example.com.popmovies;

/**
 * Created by Asus1 on 9/28/2015.
 */
public class Utilities {

    public static String snipTextForView(String overviewText){
        int start = 0;
        int end = 300;
        return overviewText.length()>end?(overviewText.substring(start,end) + " ..."):overviewText;
    }
}
