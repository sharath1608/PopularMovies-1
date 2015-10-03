package app.sunshine.android.example.com.popmovies;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Asus1 on 8/18/2015.
 */
public class ViewHolderItem {
    public ImageView movieImage;
    public TextView movieTag;

    public ViewHolderItem(View view){
        movieImage = (ImageView)view.findViewById(R.id.griditem_image);
        movieTag = (TextView)view.findViewById(R.id.griditem_tag);
    }
}
