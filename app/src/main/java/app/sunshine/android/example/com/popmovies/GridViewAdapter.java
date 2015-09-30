package app.sunshine.android.example.com.popmovies;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Asus1 on 8/9/2015.
 */
public class GridViewAdapter extends ArrayAdapter<GridViewObject>{
    private Context mContext;
    private int mresource;
    private List<GridViewObject> gridViewObjects;


    public GridViewAdapter(Context mContext,int resource,List<GridViewObject> gridViewObjects){
        super(mContext,R.layout.grid_item_layout,gridViewObjects);
        this.mContext = mContext;
        this.mresource = resource;
        this.gridViewObjects = gridViewObjects;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent){
        final ViewHolderItem viewHolder;
        TextView textView;
        final String LOG_TAG = getClass().getSimpleName();
        if(convertView == null){
            convertView = ((Activity)mContext).getLayoutInflater().inflate(mresource,parent,false);
            viewHolder = new ViewHolderItem();
            textView = (TextView)convertView.findViewById(R.id.griditem_tag);
            viewHolder.movieTag = textView;
            viewHolder.movieImage = (ImageView)convertView.findViewById(R.id.griditem_image);
            viewHolder.progressBar = (ProgressBar)convertView.findViewById(R.id.prog_bar);
            convertView.setTag(viewHolder);
        } else{
            viewHolder =(ViewHolderItem) convertView.getTag();
        }
        viewHolder.movieTag.setText(gridViewObjects.get(position).getMovieTag());
        //viewHolder.movieTag.setVisibility(View.INVISIBLE);
        //viewHolder.movieImage.setVisibility(View.INVISIBLE);

        PicassoImageCache
                .getPicassoInstance(mContext)
                .load(gridViewObjects.get(position).getMovieUrl())
                .resize(500, 750)
                .error(R.drawable.user_placeholder_image)
                .into(viewHolder.movieImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "Error in loading images");
                    }
                });

        return convertView;
    }

}
