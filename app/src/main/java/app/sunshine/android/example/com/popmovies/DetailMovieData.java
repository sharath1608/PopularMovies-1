package app.sunshine.android.example.com.popmovies;

import java.util.ArrayList;

/**
 * Created by Asus1 on 8/13/2015.
 */
public class DetailMovieData {

    private String movieID;
    private String title;
    private String imageUrl;
    private String description;
    private String rating;
    private String year;
    private String duration;
    private Trailer[] trailers;
    private ArrayList<CastViewObject> casts;

    public ArrayList<CastViewObject> getCasts() {
        return casts;
    }

    public void setCasts(ArrayList<CastViewObject> casts) {
        this.casts = casts;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public void setBackdropUrl(String backdropUrl) {
        this.backdropUrl = backdropUrl;
    }

    private String backdropUrl;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {

        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setDuration(String duaration) {
        this.duration = duaration;
    }

    public String getDescription() {

        return description;
    }

    public String getRating() {
        return rating;
    }

    public String getYear() {
        return year;
    }

    public Trailer[] getTrailers() {
        return trailers;
    }

    public void setTrailers(Trailer[] trailers) {
        this.trailers = trailers;
    }

    public String getDuration() {
        return duration;
    }
}
