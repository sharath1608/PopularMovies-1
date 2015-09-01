package app.sunshine.android.example.com.popmovies;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus1 on 8/28/2015.
 */
public class ReviewListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResource;
    private List<String> mReviewTexts;

    public ReviewListAdapter(Context context, int resource, List<String> reviewTexts) {
        super(context, resource, reviewTexts);
        mContext = context;
        mResource = resource;
        mReviewTexts = new ArrayList<>(reviewTexts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        TextView textView;
        if(view == null) {
            view = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);
        }
        textView = (TextView)view.findViewById(R.id.movie_review_text);
        textView.setText(mReviewTexts.get(position));
        return view;
    }
}
