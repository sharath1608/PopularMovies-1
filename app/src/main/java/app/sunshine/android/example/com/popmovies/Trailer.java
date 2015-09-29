package app.sunshine.android.example.com.popmovies;

/**
 * Created by Asus1 on 8/31/2015.
 */
public class Trailer {
    String trailerName;
    String trailerId;
    String source;

    public Trailer(){

    }

    public Trailer(String trailerName, String source, String trailerId) {
        this.trailerName = trailerName;
        this.source = source;
        this.trailerId = trailerId;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public void setTrailerId(String trailerId) {
        this.trailerId = trailerId;
    }

    public String getTrailerName() {
        return trailerName;
    }

    public void setTrailerName(String trailerName) {
        this.trailerName = trailerName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}