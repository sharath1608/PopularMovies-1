package app.sunshine.android.example.com.popmovies;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus1 on 8/9/2015.
 */
public class GridViewAdapter extends ArrayAdapter<String>{
    private Context mContext;
    private LayoutInflater layoutInflater;

    private List<String> mImageUrls;

    public GridViewAdapter(Context mContext,List<String> mImageUrls){
        super(mContext,R.layout.grid_item_layout,mImageUrls);
        this.mContext = mContext;
        this.mImageUrls = mImageUrls;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView,ViewGroup parent){
        ImageView imageView = (ImageView)convertView;
        int layoutWidth = 500;
        int layoutHeight = 750;

        if(convertView == null){
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(layoutWidth,layoutHeight)); //Always maintain aspect ratio of 1:1.5
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        Picasso
                .with(mContext)
                .load(mImageUrls.get(position))
                .fit()
                .into(imageView);

        return imageView;
    }

}
