package app.sunshine.android.example.com.popmovies;

/**
 * Created by Asus1 on 9/23/2015.
 */
public class Review{

    private String reviewText;
    private String author;

    public Review(){
        super();
    }

    public Review(String reviewText, String author) {
        this.reviewText = reviewText;
        this.author = author;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getAuthor() {
        return author;
    }
}
