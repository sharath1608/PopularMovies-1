package app.sunshine.android.example.com.popmovies;

/**
 * Created by Asus1 on 8/13/2015.
 */
public class DetailMovieData {

    private String title;
    private String imageUrl;
    private String description;
    private String rating;
    private String year;
    private String duration;

    public String getBackdropUrl() {
        return backdropUrl;
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

    public String getDuration() {
        return duration;
    }
}
