package app.sunshine.android.example.com.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus1 on 8/18/2015.
 */
public class GridViewObject implements Parcelable{

    private String movieUrl;
    private String movieTag;
    private String scrollPosition;


    public GridViewObject() {

    }

    public GridViewObject(Parcel in){
        String[] movieData = new String[3];
        in.readStringArray(movieData);
        this.movieUrl = movieData[0];
        this.movieTag = movieData[1];
        this.scrollPosition = movieData[2];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.movieUrl,
                this.movieTag,
                this.scrollPosition
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new GridViewObject(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new GridViewObject[size];
        }

    };

    public String getMovieUrl() {
        return movieUrl;
    }
    public void setMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
    }

    public String getMovieTag() {
        return movieTag;
    }

    public void setMovieTag(String movieTag) {
        this.movieTag = movieTag;
    }
}
