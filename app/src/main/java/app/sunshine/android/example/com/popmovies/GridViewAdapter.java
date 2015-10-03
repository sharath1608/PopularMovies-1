package app.sunshine.android.example.com.popmovies;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.squareup.picasso.Callback;

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
        final String LOG_TAG = getClass().getSimpleName();
        if(convertView == null){
            convertView = ((Activity)mContext).getLayoutInflater().inflate(mresource,parent,false);
            viewHolder = new ViewHolderItem(convertView);
            convertView.setTag(viewHolder);
        } else{
            viewHolder =(ViewHolderItem) convertView.getTag();
        }
        viewHolder.movieTag.setText(gridViewObjects.get(position).getMovieTag());

        PicassoImageCache
                .getPicassoInstance(mContext)
                .load(gridViewObjects.get(position).getMovieUrl())
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

        return convertView;
    }

}
