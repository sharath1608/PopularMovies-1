package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.popmovies.app.data.MoviesContract;
import com.squareup.picasso.Callback;

/**
 * Created by Asus1 on 9/30/2015.
 */
public class CustomFavViewAdapter extends CursorAdapter {

    private final String LOG_TAG = getClass().getSimpleName();
    public CustomFavViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_layout,parent,false);
        ViewHolderItem viewHolder = new ViewHolderItem(convertView);
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolderItem viewHolder;
        String imageUrl;
        String tagName;

        viewHolder = (ViewHolderItem) view.getTag();
        tagName = cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE));
        imageUrl = cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER));
        viewHolder.movieTag.setText(tagName);

        PicassoImageCache
                .getPicassoInstance(mContext)
                .load(imageUrl)
                .resize(500, 750)
                .error(R.drawable.user_placeholder_image)
                .into(viewHolder.movieImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "Error in loading images");
                    }
                });
    }
}
