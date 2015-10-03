package app.sunshine.android.example.com.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Asus1 on 8/13/2015.
 */
public class DetailMovieData implements Parcelable{

    private String movieID;
    private String title;
    private String imageUrl;
    private String description;
    private String rating;
    private String year;
    private String duration;
    private String isFavorite;
    private String isMovieInDB;
    private String isReviewInDB;
    private String isTrailerInDB;
    private String isCastInDB;
    private Trailer[] trailers;
    private Review[] reviews;
    private ArrayList<CastViewObject> casts;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.movieID,
                this.title,
                this.imageUrl,
                this.description,
                this.rating,
                this.year,
                this.duration,
                this.isFavorite
        });
    }

    public Boolean getFavorite() {
        return Boolean.valueOf(isFavorite);
    }

    public void setFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Boolean getMovieInDB() {
        return Boolean.valueOf(isMovieInDB);
    }

    public void setMovieInDB(String isMovieInDB) {
        this.isMovieInDB = isMovieInDB;
    }

    public Boolean getTrailerInDB() {
        return Boolean.valueOf(isTrailerInDB);
    }

    public void setTrailerInDB(String isTrailerInDB) {
        this.isTrailerInDB = isTrailerInDB;
    }

    public Boolean getReviewInDB() {
        return Boolean.valueOf(isReviewInDB);
    }

    public void setReviewInDB(String isReviewInDB) {
        this.isReviewInDB = isReviewInDB;
    }

    public Boolean getCastInDB() {
        return Boolean.valueOf(isCastInDB);
    }

    public void setCastInDB(String isCastInDB) {
        this.isCastInDB = isCastInDB;
    }


    public ArrayList<CastViewObject> getCasts() {
        return casts;
    }

    public Review[] getReviews() {
        return reviews;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
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
